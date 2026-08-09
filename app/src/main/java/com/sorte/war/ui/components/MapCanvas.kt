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
private const val REGION_R = 27f // raio-base da região em unidades virtuais
private const val REGION_POINTS = 12

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
            .background(Brush.verticalGradient(listOf(Color(0xFF10314F), Color(0xFF0A1626))))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    userScale = (userScale * zoom).coerceIn(1f, 4.5f)
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
        drawOceanGrid { x, y -> project(size, x, y) }
        drawContinentTints { x, y -> project(size, x, y) }
        drawEdges { x, y -> project(size, x, y) }
        drawTerritories(engine, selectedTerritory, validTargets, s) { x, y -> project(size, x, y) }
        drawContinentLabels(s) { x, y -> project(size, x, y) }
    }
}

/** Hash determinístico 0..1 para gerar contornos orgânicos estáveis. */
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
        val vy = t.y + (sin(ang) * rr * 0.86f).toFloat() // levemente achatado
        val p = project(vx, vy)
        if (k == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    return path
}

private fun DrawScope.drawOceanGrid(project: (Float, Float) -> Offset) {
    val grid = Color(0x14BFE3FF)
    var gx = 0f
    while (gx <= VW) {
        drawLine(grid, project(gx, 0f), project(gx, VH), strokeWidth = 1f)
        gx += 100f
    }
    var gy = 0f
    while (gy <= VH) {
        drawLine(grid, project(0f, gy), project(VW, gy), strokeWidth = 1f)
        gy += 100f
    }
}

private fun DrawScope.drawContinentTints(project: (Float, Float) -> Offset) {
    for (cont in MapData.continents) {
        for (id in cont.territoryIds) {
            val t = MapData.territory(id)
            drawPath(buildRegionPath(t, 1.18f, project), Color(cont.colorArgb).copy(alpha = 0.16f))
        }
    }
}

private fun DrawScope.drawEdges(project: (Float, Float) -> Offset) {
    val dashed = PathEffect.dashPathEffect(floatArrayOf(9f, 9f), 0f)
    for ((a, b) in MapData.edges) {
        val ta = MapData.territory(a)
        val tb = MapData.territory(b)
        val isLong = hypot(ta.x - tb.x, ta.y - tb.y) > 320f
        drawLine(
            color = Color(0xFF6E93BE).copy(alpha = if (isLong) 0.40f else 0.65f),
            start = project(ta.x, ta.y),
            end = project(tb.x, tb.y),
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
        color = android.graphics.Color.argb(235, 245, 248, 252)
        textAlign = Paint.Align.CENTER
        textSize = 10f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(3f, 0f, 1f, android.graphics.Color.argb(200, 0, 0, 0))
    }
    val armyPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 13f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }

    for (t in MapData.territories) {
        val center = project(t.x, t.y)
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) Color(engine.players[owner].colorArgb) else Color.Gray
        val isSelected = t.id == selected
        val isTarget = t.id in validTargets

        val path = buildRegionPath(t, 1f, project)

        drawPath(path, ownerColor.copy(alpha = 0.92f))
        drawPath(path, Color(0xFF0A1220), style = Stroke(width = 2.2f))
        when {
            isSelected -> drawPath(path, Color(0xFFFFC24B), style = Stroke(width = 4f))
            isTarget -> drawPath(path, Color(0xFF7CF5A0), style = Stroke(width = 3.2f))
        }

        drawContext.canvas.nativeCanvas.drawText(
            t.name, center.x, center.y - REGION_R * s * 0.55f, namePaint
        )

        val badgeCenter = center.copy(y = center.y + 4f * s)
        val badgeR = 11f * s
        drawCircle(Color(0xE60A1220), radius = badgeR, center = badgeCenter)
        drawCircle(ownerColor, radius = badgeR, center = badgeCenter, style = Stroke(width = 2f))
        drawContext.canvas.nativeCanvas.drawText(
            engine.armiesOf[t.id].toString(), badgeCenter.x, badgeCenter.y + 4.6f * s, armyPaint
        )
    }
}

private fun DrawScope.drawContinentLabels(s: Float, project: (Float, Float) -> Offset) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(120, 220, 232, 245)
        textAlign = Paint.Align.CENTER
        textSize = 12f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }
    for (cont in MapData.continents) {
        val pts = cont.territoryIds.map { MapData.territory(it) }
        val cx = pts.map { it.x }.average().toFloat()
        val minY = pts.minOf { it.y } - 44f
        val p = project(cx, minY)
        drawContext.canvas.nativeCanvas.drawText(cont.name.uppercase(), p.x, p.y, paint)
    }
}
