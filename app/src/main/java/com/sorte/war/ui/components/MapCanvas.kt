package com.sorte.war.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.MapData
import kotlin.math.hypot

private const val VW = MapData.VIRTUAL_WIDTH
private const val VH = MapData.VIRTUAL_HEIGHT

@Composable
fun MapCanvas(
    engine: GameEngine,
    refresh: Int,
    selectedTerritory: Int?,
    validTargets: Set<Int>,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    @Suppress("UNUSED_EXPRESSION") refresh

    var userScale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    fun baseScale(size: Size) = minOf(size.width / VW, size.height / VH)

    fun project(size: Size, x: Float, y: Float): Offset {
        val s = baseScale(size) * userScale
        return Offset(
            size.width / 2f + (x - VW / 2f) * s + panX,
            size.height / 2f + (y - VH / 2f) * s + panY
        )
    }

    fun invert(size: Size, p: Offset): Offset {
        val s = baseScale(size) * userScale
        return Offset(
            (p.x - size.width / 2f - panX) / s + VW / 2f,
            (p.y - size.height / 2f - panY) / s + VH / 2f
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0C2947), Color(0xFF061019))))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    userScale = (userScale * zoom).coerceIn(1f, 6f)
                    panX += pan.x
                    panY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val size = Size(this.size.width.toFloat(), this.size.height.toFloat())
                    val v = invert(size, offset)
                    // 1) toque exato dentro do contorno do país
                    var hit = MapData.hitTest(v.x, v.y)
                    // 2) senão, o território cujo centro estiver mais próximo (tolerância)
                    if (hit == null) {
                        var best = -1
                        var bestD = 26f
                        for (t in MapData.territories) {
                            val d = hypot(v.x - t.x, v.y - t.y)
                            if (d < bestD) { bestD = d; best = t.id }
                        }
                        if (best >= 0) hit = best
                    }
                    hit?.let(onTap)
                }
            }
    ) {
        val s = baseScale(size) * userScale
        val proj = { x: Float, y: Float -> project(size, x, y) }

        drawOceanGrid(proj)
        drawRoutes(proj)
        drawLandmasses(engine, selectedTerritory, validTargets, s, proj)
        drawLabels(engine, s, proj)
        drawContinentBadges(s, proj)
    }
}

/** Converte os anéis de um território em um Path projetado. */
private fun territoryPath(id: Int, project: (Float, Float) -> Offset): Path {
    val path = Path()
    for (ring in MapData.shapeOf(id)) {
        if (ring.size < 6) continue
        val p0 = project(ring[0], ring[1])
        path.moveTo(p0.x, p0.y)
        var i = 2
        while (i < ring.size) {
            val p = project(ring[i], ring[i + 1])
            path.lineTo(p.x, p.y)
            i += 2
        }
        path.close()
    }
    return path
}

private fun DrawScope.drawOceanGrid(project: (Float, Float) -> Offset) {
    val grid = Color(0x18AEDCFF)
    var gx = 0f
    while (gx <= VW) {
        drawLine(grid, project(gx, 0f), project(gx, VH), strokeWidth = 1f); gx += 50f
    }
    var gy = 0f
    while (gy <= VH) {
        drawLine(grid, project(0f, gy), project(VW, gy), strokeWidth = 1f); gy += 50f
    }
}

private fun DrawScope.drawRoutes(project: (Float, Float) -> Offset) {
    val dashed = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
    for ((a, b) in MapData.edges) {
        val ta = MapData.territory(a)
        val tb = MapData.territory(b)
        val far = hypot(ta.x - tb.x, ta.y - tb.y) > 150f
        drawLine(
            color = Color(0xFF8FC3EE).copy(alpha = if (far) 0.42f else 0.20f),
            start = project(ta.x, ta.y),
            end = project(tb.x, tb.y),
            strokeWidth = if (far) 1.6f else 1.2f,
            pathEffect = if (far) dashed else null
        )
    }
}

private fun DrawScope.drawLandmasses(
    engine: GameEngine,
    selected: Int?,
    validTargets: Set<Int>,
    s: Float,
    project: (Float, Float) -> Offset
) {
    // 1ª passada: sombra do relevo sob todas as terras
    for (t in MapData.territories) {
        val shadow = territoryPath(t.id) { x, y ->
            project(x, y).let { Offset(it.x + 2.5f, it.y + 3.5f) }
        }
        drawPath(shadow, Color(0x70000000))
    }

    // 2ª passada: terreno + cor do dono (translúcida) + relevo
    for (t in MapData.territories) {
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) Color(engine.players[owner].colorArgb) else Color(0xFF7A7A7A)
        val path = territoryPath(t.id, project)
        val center = project(t.x, t.y)

        clipPath(path) {
            // terreno base (visível por baixo da cor do exército)
            drawRect(landColor(t.continentId))
            // relevo: luz vinda do noroeste
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0x40FFFFFF), Color(0x00FFFFFF)),
                    center = Offset(center.x - 14f * s, center.y - 16f * s),
                    radius = 46f * s
                ),
                radius = 46f * s,
                center = Offset(center.x - 14f * s, center.y - 16f * s)
            )
            // sombra do relevo a sudeste
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0x38000000), Color(0x00000000)),
                    center = Offset(center.x + 16f * s, center.y + 18f * s),
                    radius = 44f * s
                ),
                radius = 44f * s,
                center = Offset(center.x + 16f * s, center.y + 18f * s)
            )
            // cor do exército — translúcida, deixando ver o mapa
            drawRect(ownerColor.copy(alpha = 0.74f))

            // fronteiras internas dos países que compõem o território
            val subs = MapData.subShapesOf(t.id)
            if (subs.isNotEmpty()) {
                val sub = Path()
                for (ring in subs) {
                    if (ring.size < 6) continue
                    val q0 = project(ring[0], ring[1])
                    sub.moveTo(q0.x, q0.y)
                    var i = 2
                    while (i < ring.size) {
                        val q = project(ring[i], ring[i + 1])
                        sub.lineTo(q.x, q.y)
                        i += 2
                    }
                    sub.close()
                }
                drawPath(sub, Color(0x33000000), style = Stroke(width = 0.9f))
            }
        }

        // fronteiras
        drawPath(path, Color(0xFF07121F), style = Stroke(width = 1.6f))
        when {
            t.id == selected -> {
                drawPath(path, Color(0x66FFD98A), style = Stroke(width = 6f))
                drawPath(path, Color(0xFFFFC24B), style = Stroke(width = 2.4f))
            }
            t.id in validTargets -> {
                drawPath(path, Color(0x557CF5A0), style = Stroke(width = 5f))
                drawPath(path, Color(0xFF7CF5A0), style = Stroke(width = 2f))
            }
        }
    }
}

/** Cor de terreno por continente (bioma), visível sob a cor translúcida do dono. */
private fun landColor(continentId: Int): Color = when (continentId) {
    0 -> Color(0xFF7C8557)
    1 -> Color(0xFF5F7E45)
    2 -> Color(0xFF77805F)
    3 -> Color(0xFF9C8A55)
    4 -> Color(0xFF847E52)
    else -> Color(0xFF8A7248)
}

private fun DrawScope.drawLabels(
    engine: GameEngine, s: Float, project: (Float, Float) -> Offset
) {
    val namePaint = Paint().apply {
        color = android.graphics.Color.argb(245, 248, 251, 255)
        textAlign = Paint.Align.CENTER
        textSize = 9.5f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(3.5f, 0f, 1f, android.graphics.Color.argb(230, 0, 0, 0))
    }
    val armyPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 12f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }

    for (t in MapData.territories) {
        val c = project(t.x, t.y)
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) Color(engine.players[owner].colorArgb) else Color.Gray
        val armies = engine.armiesOf[t.id]

        // fortificação conforme o contingente
        val level = when {
            armies >= 12 -> 3
            armies >= 8 -> 2
            armies >= 4 -> 1
            else -> 0
        }
        if (level > 0) drawStructure(level, Offset(c.x, c.y - 13f * s), s, ownerColor)

        drawContext.canvas.nativeCanvas.drawText(t.name, c.x, c.y - 15f * s, namePaint)

        val badgeR = 9.5f * s
        drawCircle(Color(0xF0060F1B), radius = badgeR, center = c)
        drawCircle(ownerColor, radius = badgeR, center = c, style = Stroke(width = 2f))
        drawContext.canvas.nativeCanvas.drawText(armies.toString(), c.x, c.y + 4.2f * s, armyPaint)
    }
}

/** Posto avançado (1), forte (2) ou fortaleza (3). */
private fun DrawScope.drawStructure(level: Int, c: Offset, u: Float, accent: Color) {
    val stone = Color(0xFF59636E)
    val dark = Color(0xFF2F363E)

    fun merlons(x0: Float, top: Float, w: Float, count: Int) {
        val mw = w / (count * 2 - 1)
        var x = x0
        repeat(count) {
            drawRect(stone, topLeft = Offset(x, top - mw), size = Size(mw, mw))
            x += mw * 2
        }
    }
    fun tower(cx: Float, baseY: Float, w: Float, h: Float) {
        val top = baseY - h
        drawRect(stone, topLeft = Offset(cx - w / 2, top), size = Size(w, h))
        drawRect(dark, topLeft = Offset(cx - w / 2, top), size = Size(w, h), style = Stroke(1f))
        merlons(cx - w / 2, top, w, 3)
    }
    fun flag(cx: Float, topY: Float, h: Float) {
        drawLine(Color(0xFFE8ECEF), Offset(cx, topY), Offset(cx, topY - h), strokeWidth = 1.4f)
        val p = Path().apply {
            moveTo(cx, topY - h)
            lineTo(cx + h * 0.75f, topY - h + h * 0.30f)
            lineTo(cx, topY - h + h * 0.56f)
            close()
        }
        drawPath(p, accent)
    }

    when (level) {
        1 -> {
            tower(c.x, c.y + 4f * u, 6f * u, 9f * u)
            flag(c.x, c.y + 4f * u - 9f * u, 7f * u)
        }
        2 -> {
            val ww = 13f * u; val wh = 6.5f * u; val top = c.y + 4f * u - wh
            drawRect(stone, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh))
            drawRect(dark, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh), style = Stroke(1f))
            merlons(c.x - ww / 2, top, ww, 4)
            tower(c.x + ww / 2 - 2f * u, c.y + 4f * u, 6f * u, 12f * u)
            flag(c.x + ww / 2 - 2f * u, c.y + 4f * u - 12f * u, 7f * u)
        }
        else -> {
            val ww = 18f * u; val wh = 7f * u; val top = c.y + 4f * u - wh
            drawRect(stone, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh))
            drawRect(dark, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh), style = Stroke(1f))
            merlons(c.x - ww / 2, top, ww, 5)
            tower(c.x - ww / 2, c.y + 4f * u, 6.5f * u, 13f * u)
            tower(c.x + ww / 2, c.y + 4f * u, 6.5f * u, 13f * u)
            flag(c.x, c.y + 4f * u - wh, 9f * u)
        }
    }
}

private fun DrawScope.drawContinentBadges(s: Float, project: (Float, Float) -> Offset) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(210, 255, 205, 110)
        textAlign = Paint.Align.CENTER
        textSize = 11f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(220, 0, 0, 0))
    }
    for (cont in MapData.continents) {
        val pts = cont.territoryIds.map { MapData.territory(it) }
        val cx = pts.map { it.x }.average().toFloat()
        val top = pts.minOf { it.y } - 26f
        val p = project(cx, top)
        drawContext.canvas.nativeCanvas.drawText(
            "${cont.name.uppercase()}  +${cont.bonus}", p.x, p.y, paint
        )
    }
}
