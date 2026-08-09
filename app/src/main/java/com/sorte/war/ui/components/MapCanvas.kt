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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.MapData
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.OceanDeep
import kotlin.math.hypot

private const val VW = MapData.VIRTUAL_WIDTH
private const val VH = MapData.VIRTUAL_HEIGHT
private const val NODE_R = 16f // raio do nó em unidades virtuais

@Composable
fun MapCanvas(
    engine: GameEngine,
    refresh: Int,
    selectedTerritory: Int?,
    validTargets: Set<Int>,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // refresh é lido para forçar recomposição a cada mutação do motor.
    @Suppress("UNUSED_EXPRESSION") refresh

    var userScale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    fun baseScale(size: Size) = minOf(size.width / VW, size.height / VH)

    fun project(size: Size, x: Float, y: Float): Offset {
        val s = baseScale(size) * userScale
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Offset(cx + (x - VW / 2f) * s + panX, cy + (y - VH / 2f) * s + panY)
    }

    fun invert(size: Size, p: Offset): Offset {
        val s = baseScale(size) * userScale
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Offset((p.x - cx - panX) / s + VW / 2f, (p.y - cy - panY) / s + VH / 2f)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OceanDeep, NightNavy)))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    userScale = (userScale * zoom).coerceIn(1f, 4f)
                    panX += pan.x
                    panY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val size = Size(this.size.width.toFloat(), this.size.height.toFloat())
                    val v = invert(size, offset)
                    val s = baseScale(size) * userScale
                    val hitR = (NODE_R + 8f)
                    var closest = -1
                    var closestDist = Float.MAX_VALUE
                    for (t in MapData.territories) {
                        val d = hypot(v.x - t.x, v.y - t.y)
                        if (d < hitR && d < closestDist) {
                            closestDist = d; closest = t.id
                        }
                    }
                    if (closest >= 0) onTap(closest)
                }
            }
    ) {
        val s = baseScale(size) * userScale

        drawContinentRegions(size, s) { x, y -> project(size, x, y) }
        drawEdges(size, s) { x, y -> project(size, x, y) }
        drawTerritories(
            engine, selectedTerritory, validTargets, s
        ) { x, y -> project(size, x, y) }
    }
}

private fun DrawScope.drawContinentRegions(
    size: Size, s: Float, project: (Float, Float) -> Offset
) {
    for (cont in MapData.continents) {
        val pts = cont.territoryIds.map { MapData.territory(it) }
        val minX = pts.minOf { it.x } - 34f
        val maxX = pts.maxOf { it.x } + 34f
        val minY = pts.minOf { it.y } - 34f
        val maxY = pts.maxOf { it.y } + 34f
        val topLeft = project(minX, minY)
        val bottomRight = project(maxX, maxY)
        drawRoundRect(
            color = Color(cont.colorArgb).copy(alpha = 0.10f),
            topLeft = topLeft,
            size = Size(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f * s, 28f * s)
        )
    }
}

private fun DrawScope.drawEdges(
    size: Size, s: Float, project: (Float, Float) -> Offset
) {
    val dashed = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    for ((a, b) in MapData.edges) {
        val ta = MapData.territory(a)
        val tb = MapData.territory(b)
        val pa = project(ta.x, ta.y)
        val pb = project(tb.x, tb.y)
        // Ligações trans-oceânicas muito longas são tracejadas (ex.: Alasca–Vladivostok).
        val isLong = hypot(ta.x - tb.x, ta.y - tb.y) > 320f
        drawLine(
            color = Color(0xFF3A5170).copy(alpha = if (isLong) 0.55f else 0.9f),
            start = pa,
            end = pb,
            strokeWidth = 2f,
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
    val r = NODE_R * s

    val namePaint = Paint().apply {
        color = android.graphics.Color.argb(220, 220, 230, 245)
        textAlign = Paint.Align.CENTER
        textSize = 10.5f * s
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    val armyPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 15f * s
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }

    for (t in MapData.territories) {
        val center = project(t.x, t.y)
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) Color(engine.players[owner].colorArgb) else Color.Gray
        val isSelected = t.id == selected
        val isTarget = t.id in validTargets

        // halo de destaque
        if (isSelected) {
            drawCircle(Color(0xFFFFC24B), radius = r + 7f, center = center, style = Stroke(width = 4f))
        } else if (isTarget) {
            drawCircle(Color(0xFF7CF5A0), radius = r + 6f, center = center, style = Stroke(width = 3.5f))
        }

        // sombra sutil
        drawCircle(Color(0x66000000), radius = r, center = center.copy(y = center.y + 2f))
        // nó
        drawCircle(ownerColor, radius = r, center = center)
        drawCircle(Color(0xFF0B1220), radius = r, center = center, style = Stroke(width = 2.5f))
        drawCircle(Color(0xCCFFFFFF), radius = r, center = center, style = Stroke(width = 1f))

        // nome do território (acima)
        drawContext.canvas.nativeCanvas.drawText(
            t.name, center.x, center.y - r - 6f, namePaint
        )
        // contagem de exércitos (dentro), cor contrastante
        armyPaint.color = contrastOn(ownerColor)
        drawContext.canvas.nativeCanvas.drawText(
            engine.armiesOf[t.id].toString(), center.x, center.y + 5.5f * s, armyPaint
        )
    }
}

/** Escolhe texto claro ou escuro conforme a luminância da cor de fundo. */
private fun contrastOn(bg: Color): Int {
    val argb = bg.toArgb()
    val r = (argb shr 16 and 0xFF)
    val g = (argb shr 8 and 0xFF)
    val b = (argb and 0xFF)
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
    return if (luminance > 150) android.graphics.Color.parseColor("#10161F")
    else android.graphics.Color.WHITE
}
