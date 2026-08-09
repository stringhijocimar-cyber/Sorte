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
import com.sorte.war.model.Territory
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin

private const val VW = MapData.VIRTUAL_WIDTH
private const val VH = MapData.VIRTUAL_HEIGHT
private const val REGION_R = 28f
private const val REGION_POINTS = 14

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
            .background(Brush.verticalGradient(listOf(Color(0xFF0E3350), Color(0xFF071019))))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    userScale = (userScale * zoom).coerceIn(1f, 5f)
                    panX += pan.x
                    panY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val size = Size(this.size.width.toFloat(), this.size.height.toFloat())
                    val v = invert(size, offset)
                    var closest = -1
                    var closestDist = Float.MAX_VALUE
                    for (t in MapData.territories) {
                        val d = hypot(v.x - t.x, v.y - t.y)
                        if (d < REGION_R + 6f && d < closestDist) {
                            closestDist = d; closest = t.id
                        }
                    }
                    if (closest >= 0) onTap(closest)
                }
            }
    ) {
        val s = baseScale(size) * userScale
        drawOcean { x, y -> project(size, x, y) }
        drawEdges { x, y -> project(size, x, y) }
        drawTerritories(engine, selectedTerritory, validTargets, s) { x, y -> project(size, x, y) }
        drawContinentLabels(s) { x, y -> project(size, x, y) }
    }
}

private fun hash01(a: Int, b: Int): Float {
    val x = sin(a * 12.9898 + b * 78.233) * 43758.5453
    return (x - floor(x)).toFloat()
}

private fun buildRegionPath(t: Territory, scaleR: Float, project: (Float, Float) -> Offset): Path {
    val path = Path()
    for (k in 0 until REGION_POINTS) {
        val ang = 2.0 * Math.PI * k / REGION_POINTS
        val rr = REGION_R * scaleR * (0.80f + 0.42f * hash01(t.id + 1, k))
        val vx = t.x + (cos(ang) * rr).toFloat()
        val vy = t.y + (sin(ang) * rr * 0.86f).toFloat()
        val p = project(vx, vy)
        if (k == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    return path
}

/** Cor de terreno (bioma) por continente — visível sob a posse translúcida. */
private fun landColor(continentId: Int): Color = when (continentId) {
    0 -> Color(0xFF6B7A4E) // América do Norte
    1 -> Color(0xFF5E7A45) // América do Sul
    2 -> Color(0xFF6E7658) // Europa
    3 -> Color(0xFF8A7B4E) // África (savana)
    4 -> Color(0xFF77754E) // Ásia (estepe)
    else -> Color(0xFF7A6A46) // Oceania (outback)
}

private fun DrawScope.drawOcean(project: (Float, Float) -> Offset) {
    val grid = Color(0x12BFE3FF)
    var gx = 0f
    while (gx <= VW) {
        drawLine(grid, project(gx, 0f), project(gx, VH), strokeWidth = 1f); gx += 100f
    }
    var gy = 0f
    while (gy <= VH) {
        drawLine(grid, project(0f, gy), project(VW, gy), strokeWidth = 1f); gy += 100f
    }
}

private fun DrawScope.drawEdges(project: (Float, Float) -> Offset) {
    val dashed = PathEffect.dashPathEffect(floatArrayOf(9f, 9f), 0f)
    for ((a, b) in MapData.edges) {
        val ta = MapData.territory(a)
        val tb = MapData.territory(b)
        val isLong = hypot(ta.x - tb.x, ta.y - tb.y) > 320f
        drawLine(
            color = Color(0xFF6E93BE).copy(alpha = if (isLong) 0.38f else 0.6f),
            start = project(ta.x, ta.y), end = project(tb.x, tb.y),
            strokeWidth = if (isLong) 1.5f else 2f,
            pathEffect = if (isLong) dashed else null
        )
    }
}

private fun DrawScope.drawTerritories(
    engine: GameEngine,
    selected: Int?,
    validTargets: Set<Int>,
    s: Float,
    project: (Float, Float) -> Offset
) {
    val namePaint = Paint().apply {
        color = android.graphics.Color.argb(240, 246, 249, 253)
        textAlign = Paint.Align.CENTER
        textSize = 10f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(3f, 0f, 1f, android.graphics.Color.argb(210, 0, 0, 0))
    }
    val armyPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 12.5f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }

    for (t in MapData.territories) {
        val center = project(t.x, t.y)
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) Color(engine.players[owner].colorArgb) else Color.Gray
        val isSelected = t.id == selected
        val isTarget = t.id in validTargets
        val armies = engine.armiesOf[t.id]

        val path = buildRegionPath(t, 1f, project)

        // sombra projetada (efeito de relevo elevado)
        val shadow = buildRegionPath(t, 1f) { x, y -> project(x, y).let { Offset(it.x + 3f, it.y + 4f) } }
        drawPath(shadow, Color(0x55000000))

        // terreno base + relevo (dentro do contorno)
        clipPath(path) {
            drawRect(landColor(t.continentId))
            // colinas iluminadas
            for (i in 0 until 5) {
                val bx = t.x + (hash01(t.id, 100 + i) - 0.5f) * REGION_R * 1.3f
                val by = t.y + (hash01(t.id, 200 + i) - 0.5f) * REGION_R * 1.1f
                val br = REGION_R * (0.30f + 0.35f * hash01(t.id, 300 + i)) * s
                val cc = project(bx, by)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0x3AFFFFFF), Color(0x00FFFFFF)), center = cc, radius = br
                    ), radius = br, center = cc
                )
            }
            // vales sombreados
            for (i in 0 until 3) {
                val bx = t.x + (hash01(t.id, 400 + i) - 0.5f) * REGION_R * 1.2f
                val by = t.y + (hash01(t.id, 500 + i) - 0.5f) * REGION_R * 1.0f
                val br = REGION_R * (0.28f + 0.30f * hash01(t.id, 600 + i)) * s
                val cc = project(bx, by)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0x33000000), Color(0x00000000)), center = cc, radius = br
                    ), radius = br, center = cc
                )
            }
            // POSSE TRANSLÚCIDA — cor do exército deixando ver o terreno
            drawRect(ownerColor.copy(alpha = 0.42f))
        }

        // luz de borda (rim light) + fronteira escura
        drawPath(path, Color(0x30FFFFFF), style = Stroke(width = 3f))
        drawPath(path, Color(0xFF0A1220), style = Stroke(width = 2.4f))
        when {
            isSelected -> drawPath(path, Color(0xFFFFC24B), style = Stroke(width = 4.2f))
            isTarget -> drawPath(path, Color(0xFF7CF5A0), style = Stroke(width = 3.2f))
        }

        // nome do território
        drawContext.canvas.nativeCanvas.drawText(
            t.name, center.x, center.y - REGION_R * s * 0.72f, namePaint
        )

        // FORTIFICAÇÃO conforme quantidade de exércitos
        val level = when {
            armies >= 12 -> 3
            armies >= 8 -> 2
            armies >= 4 -> 1
            else -> 0
        }
        if (level > 0) {
            drawStructure(level, Offset(center.x, center.y - 1f * s), s, ownerColor)
        }

        // badge com a contagem de exércitos (parte inferior da região)
        val badgeCenter = Offset(center.x, center.y + REGION_R * s * 0.55f)
        val badgeR = 10.5f * s
        drawCircle(Color(0xE60A1220), radius = badgeR, center = badgeCenter)
        drawCircle(ownerColor, radius = badgeR, center = badgeCenter, style = Stroke(width = 2f))
        drawContext.canvas.nativeCanvas.drawText(
            armies.toString(), badgeCenter.x, badgeCenter.y + 4.4f * s, armyPaint
        )
    }
}

/** Desenha posto avançado (1), forte (2) ou fortaleza (3). */
private fun DrawScope.drawStructure(level: Int, c: Offset, u: Float, accent: Color) {
    val stone = Color(0xFF5A6570)
    val stoneDark = Color(0xFF333B43)

    fun merlons(x0: Float, top: Float, w: Float, count: Int) {
        if (count < 1) return
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
        drawRect(stoneDark, topLeft = Offset(cx - w / 2, top), size = Size(w, h), style = Stroke(1.2f))
        merlons(cx - w / 2, top, w, 3)
    }
    fun flag(cx: Float, topY: Float, h: Float) {
        drawLine(Color(0xFFEFEFEF), Offset(cx, topY), Offset(cx, topY - h), strokeWidth = 1.6f)
        val fp = Path().apply {
            moveTo(cx, topY - h)
            lineTo(cx + h * 0.8f, topY - h + h * 0.30f)
            lineTo(cx, topY - h + h * 0.58f)
            close()
        }
        drawPath(fp, accent)
    }

    when (level) {
        1 -> {
            tower(c.x, c.y + 6f * u, 8f * u, 12f * u)
            flag(c.x, c.y + 6f * u - 12f * u, 9f * u)
        }
        2 -> {
            val ww = 18f * u; val wh = 9f * u; val top = c.y + 5f * u - wh
            drawRect(stone, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh))
            drawRect(stoneDark, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh), style = Stroke(1.2f))
            merlons(c.x - ww / 2, top, ww, 5)
            tower(c.x + ww / 2 - 3f * u, c.y + 5f * u, 8f * u, 16f * u)
            flag(c.x + ww / 2 - 3f * u, c.y + 5f * u - 16f * u, 9f * u)
        }
        else -> {
            val ww = 24f * u; val wh = 10f * u; val top = c.y + 5f * u - wh
            drawRect(stone, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh))
            drawRect(stoneDark, topLeft = Offset(c.x - ww / 2, top), size = Size(ww, wh), style = Stroke(1.2f))
            merlons(c.x - ww / 2, top, ww, 6)
            tower(c.x - ww / 2, c.y + 5f * u, 9f * u, 18f * u)
            tower(c.x + ww / 2, c.y + 5f * u, 9f * u, 18f * u)
            flag(c.x, c.y + 5f * u - wh, 11f * u)
        }
    }
}

private fun DrawScope.drawContinentLabels(s: Float, project: (Float, Float) -> Offset) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(115, 220, 232, 245)
        textAlign = Paint.Align.CENTER
        textSize = 12f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }
    for (cont in MapData.continents) {
        val pts = cont.territoryIds.map { MapData.territory(it) }
        val cx = pts.map { it.x }.average().toFloat()
        val minY = pts.minOf { it.y } - 46f
        val p = project(cx, minY)
        drawContext.canvas.nativeCanvas.drawText(cont.name.uppercase(), p.x, p.y, paint)
    }
}
