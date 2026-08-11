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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sorte.war.R
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.Fortification
import com.sorte.war.model.MapData
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val VW = MapData.VIRTUAL_WIDTH
private const val VH = MapData.VIRTUAL_HEIGHT

/**
 * MAPA PREMIUM 2.5D
 *
 * Esta implementação preserva integralmente:
 * - MapData / 42 territórios
 * - hit-test
 * - zoom/pan
 * - rotas e adjacências
 * - regras e estado do GameEngine
 *
 * Ela altera somente a camada de renderização.
 */
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

    val resources = LocalContext.current.resources
    val fortifications = remember(resources) {
        FortificationBitmaps(
            outpost = ImageBitmap.imageResource(resources, R.drawable.fortification_outpost),
            bunker = ImageBitmap.imageResource(resources, R.drawable.fortification_bunker),
            fortress = ImageBitmap.imageResource(resources, R.drawable.fortification_fortress)
        )
    }

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
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF092A35),
                        Color(0xFF061B24),
                        Color(0xFF030D13),
                        Color(0xFF02070A)
                    )
                )
            )
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

                    var hit = MapData.hitTest(v.x, v.y)
                    if (hit == null) {
                        var best = -1
                        var bestD = 26f
                        for (t in MapData.territories) {
                            val d = hypot(v.x - t.x, v.y - t.y)
                            if (d < bestD) {
                                bestD = d
                                best = t.id
                            }
                        }
                        if (best >= 0) hit = best
                    }
                    hit?.let(onTap)
                }
            }
    ) {
        val s = baseScale(size) * userScale
        val proj = { x: Float, y: Float -> project(size, x, y) }

        drawOceanAtmosphere(size)
        drawWatermark(size)
        drawOceanGrid(proj)
        drawOceanCurrents(proj, s)
        drawOceanLabels(proj, s)
        drawRoutes(proj, s)
        drawLandmasses(engine, selectedTerritory, validTargets, s, proj)
        drawLabels(engine, s, proj, fortifications)
        drawContinentBadges(s, proj)
        drawCompassRose(size)
        drawVignette(size)
    }
}

private data class FortificationBitmaps(
    val outpost: ImageBitmap,
    val bunker: ImageBitmap,
    val fortress: ImageBitmap
)

private data class TerrainPalette(
    val light: Color,
    val mid: Color,
    val dark: Color,
    val contour: Color
)

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

private fun DrawScope.drawOceanAtmosphere(size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x183B9AA4),
                Color(0x0D17616C),
                Color.Transparent
            ),
            center = Offset(size.width * 0.48f, size.height * 0.36f),
            radius = maxOf(size.width, size.height) * 0.72f
        )
    )

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0x001F7D89),
                Color(0x160C4652),
                Color(0x00000000)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )
}

/** Marca d'água tática discreta, ancorada na tela. */
private fun DrawScope.drawWatermark(size: Size) {
    val cx = size.width * 0.50f
    val cy = size.height * 0.51f
    val r = minOf(size.width, size.height) * 0.31f
    val ink = Color(0xFF6CB7B3).copy(alpha = 0.032f)

    val len = r * 1.34f
    drawLine(ink, Offset(cx - len, cy + len), Offset(cx + len, cy - len), strokeWidth = r * 0.075f)
    drawLine(ink, Offset(cx + len, cy + len), Offset(cx - len, cy - len), strokeWidth = r * 0.075f)

    drawCircle(ink, radius = r, center = Offset(cx, cy), style = Stroke(width = r * 0.055f))
    drawCircle(ink, radius = r * 0.63f, center = Offset(cx, cy), style = Stroke(width = r * 0.04f))
    drawLine(ink, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = r * 0.04f)

    for (f in listOf(0.42f, 0.78f)) {
        val rx = r * sqrt(1f - f * f)
        drawLine(ink, Offset(cx - rx, cy - r * f), Offset(cx + rx, cy - r * f), strokeWidth = r * 0.025f)
        drawLine(ink, Offset(cx - rx, cy + r * f), Offset(cx + rx, cy + r * f), strokeWidth = r * 0.025f)
    }
}

private fun DrawScope.drawOceanGrid(project: (Float, Float) -> Offset) {
    val major = Color(0x176CB7B3)
    val minor = Color(0x0B6CB7B3)

    var gx = 0f
    var index = 0
    while (gx <= VW) {
        drawLine(
            if (index % 2 == 0) major else minor,
            project(gx, 0f),
            project(gx, VH),
            strokeWidth = if (index % 2 == 0) 1.0f else 0.65f
        )
        gx += 50f
        index++
    }

    var gy = 0f
    index = 0
    while (gy <= VH) {
        drawLine(
            if (index % 2 == 0) major else minor,
            project(0f, gy),
            project(VW, gy),
            strokeWidth = if (index % 2 == 0) 1.0f else 0.65f
        )
        gy += 50f
        index++
    }
}

private fun DrawScope.drawOceanCurrents(
    project: (Float, Float) -> Offset,
    s: Float
) {
    val currents = listOf(
        floatArrayOf(65f, 270f, 180f, 245f, 310f, 258f, 405f, 292f),
        floatArrayOf(120f, 510f, 255f, 545f, 390f, 530f, 510f, 485f),
        floatArrayOf(585f, 115f, 690f, 95f, 810f, 112f, 930f, 155f),
        floatArrayOf(620f, 515f, 735f, 500f, 855f, 530f, 960f, 575f)
    )

    for ((idx, p) in currents.withIndex()) {
        val path = Path()
        val a = project(p[0], p[1])
        val b = project(p[2], p[3])
        val c = project(p[4], p[5])
        val d = project(p[6], p[7])

        path.moveTo(a.x, a.y)
        path.cubicTo(b.x, b.y, c.x, c.y, d.x, d.y)

        drawPath(
            path,
            Color(0xFF67AEB6).copy(alpha = if (idx % 2 == 0) 0.10f else 0.07f),
            style = Stroke(width = (1.0f * s).coerceIn(0.6f, 1.6f))
        )
    }
}

private fun DrawScope.drawOceanLabels(
    project: (Float, Float) -> Offset,
    s: Float
) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(72, 136, 189, 196)
        textAlign = Paint.Align.CENTER
        textSize = (9.0f * s).coerceIn(7f, 14f)
        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        isAntiAlias = true
    }

    val labels = listOf(
        Triple("OCEANO PACÍFICO", 90f, 360f),
        Triple("OCEANO ATLÂNTICO", 420f, 390f),
        Triple("OCEANO ÍNDICO", 720f, 510f),
        Triple("MAR ÁRTICO", 590f, 55f)
    )

    for ((text, x, y) in labels) {
        val p = project(x, y)
        drawContext.canvas.nativeCanvas.drawText(text, p.x, p.y, paint)
    }
}

private fun DrawScope.drawRoutes(
    project: (Float, Float) -> Offset,
    s: Float
) {
    val dashed = PathEffect.dashPathEffect(
        floatArrayOf((8f * s).coerceAtLeast(5f), (10f * s).coerceAtLeast(7f)),
        0f
    )

    for ((a, b) in MapData.edges) {
        val ta = MapData.territory(a)
        val tb = MapData.territory(b)
        val far = hypot(ta.x - tb.x, ta.y - tb.y) > 150f

        if (far) {
            val p1 = project(ta.x, ta.y)
            val p2 = project(tb.x, tb.y)
            val midX = (p1.x + p2.x) * 0.5f
            val midY = (p1.y + p2.y) * 0.5f - (18f * s).coerceIn(8f, 32f)
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                quadraticBezierTo(midX, midY, p2.x, p2.y)
            }

            drawPath(
                path,
                Color(0xFF000000).copy(alpha = 0.28f),
                style = Stroke(width = (2.4f * s).coerceIn(1.2f, 3.2f), pathEffect = dashed)
            )
            drawPath(
                path,
                Color(0xFF80C6CC).copy(alpha = 0.36f),
                style = Stroke(width = (1.2f * s).coerceIn(0.8f, 2.0f), pathEffect = dashed)
            )
        }
    }
}

private fun DrawScope.drawLandmasses(
    engine: GameEngine,
    selected: Int?,
    validTargets: Set<Int>,
    s: Float,
    project: (Float, Float) -> Offset
) {
    val shadowDx = (3.0f * s).coerceIn(2f, 7f)
    val shadowDy = (4.5f * s).coerceIn(3f, 9f)

    // Passada 1: sombra geral de costa / elevação 2.5D.
    for (t in MapData.territories) {
        val shadow = territoryPath(t.id) { x, y ->
            project(x, y).let { Offset(it.x + shadowDx, it.y + shadowDy) }
        }
        drawPath(shadow, Color(0x7D000000))
    }

    // Passada 2: terreno, textura, posse e contornos.
    for (t in MapData.territories) {
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) {
            Color(engine.players[owner].colorArgb)
        } else {
            Color(0xFF7A7A7A)
        }

        val path = territoryPath(t.id, project)
        val center = project(t.x, t.y)
        val terrain = terrainPalette(t.continentId)

        clipPath(path) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(terrain.light, terrain.mid, terrain.dark),
                    start = Offset(center.x - 55f * s, center.y - 62f * s),
                    end = Offset(center.x + 62f * s, center.y + 70f * s)
                )
            )

            drawTerrainRelief(t.id, t.continentId, center, s, terrain)

            // Luz de altitude vindo do noroeste.
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x36FFF0C0), Color(0x12FFFFFF), Color.Transparent),
                    center = Offset(center.x - 20f * s, center.y - 25f * s),
                    radius = 60f * s
                ),
                radius = 60f * s,
                center = Offset(center.x - 20f * s, center.y - 25f * s)
            )

            // Sombra de relevo no sudeste.
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x41000000), Color(0x16000000), Color.Transparent),
                    center = Offset(center.x + 26f * s, center.y + 30f * s),
                    radius = 62f * s
                ),
                radius = 62f * s,
                center = Offset(center.x + 26f * s, center.y + 30f * s)
            )

            // Cor do exército propositalmente translúcida: terreno continua visível.
            drawRect(ownerColor.copy(alpha = 0.43f))

            // Vinheta local muito sutil.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x24000000)),
                    center = center,
                    radius = 72f * s
                ),
                radius = 72f * s,
                center = center
            )

            // Fronteiras internas dos países/regiões.
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
                drawPath(
                    sub,
                    Color(0x4A071015),
                    style = Stroke(width = (0.85f * s).coerceIn(0.65f, 1.35f))
                )
                drawPath(
                    sub,
                    Color(0x285D6B5D),
                    style = Stroke(width = (0.35f * s).coerceIn(0.30f, 0.70f))
                )
            }
        }

        // Costa: sombra externa + filete metálico claro.
        drawPath(
            path,
            Color(0xD904090C),
            style = Stroke(width = (2.4f * s).coerceIn(1.4f, 3.4f))
        )
        drawPath(
            path,
            Color(0xFFB99A57).copy(alpha = 0.73f),
            style = Stroke(width = (0.85f * s).coerceIn(0.65f, 1.35f))
        )

        when {
            t.id == selected -> {
                drawPath(
                    path,
                    Color(0x55F4C65B),
                    style = Stroke(width = (8f * s).coerceIn(5f, 12f))
                )
                drawPath(
                    path,
                    Color(0xFFFFD46D),
                    style = Stroke(width = (2.7f * s).coerceIn(2f, 4f))
                )
            }

            t.id in validTargets -> {
                drawPath(
                    path,
                    Color(0x4459FF83),
                    style = Stroke(width = (7f * s).coerceIn(4.5f, 10f))
                )
                drawPath(
                    path,
                    Color(0xFF7DFF92),
                    style = Stroke(width = (2.2f * s).coerceIn(1.7f, 3.3f))
                )
            }
        }
    }
}

private fun DrawScope.drawTerrainRelief(
    territoryId: Int,
    continentId: Int,
    center: Offset,
    s: Float,
    terrain: TerrainPalette
) {
    val stroke = (0.75f * s).coerceIn(0.45f, 1.15f)

    // Curvas de nível determinísticas por território.
    repeat(7) { i ->
        val nx = noise01(territoryId * 131 + i * 17)
        val ny = noise01(territoryId * 197 + i * 29)
        val nw = noise01(territoryId * 233 + i * 41)
        val nh = noise01(territoryId * 271 + i * 53)

        val cx = center.x + (nx - 0.5f) * 70f * s
        val cy = center.y + (ny - 0.5f) * 58f * s
        val w = (16f + 35f * nw) * s
        val h = (8f + 22f * nh) * s

        drawOval(
            color = terrain.contour.copy(alpha = 0.10f + 0.035f * (i % 3)),
            topLeft = Offset(cx - w * 0.5f, cy - h * 0.5f),
            size = Size(w, h),
            style = Stroke(width = stroke)
        )
    }

    // Pequenas cristas/vales para quebrar a superfície plana.
    repeat(9) { i ->
        val n1 = noise01(territoryId * 311 + i * 67)
        val n2 = noise01(territoryId * 347 + i * 73)
        val n3 = noise01(territoryId * 389 + i * 79)

        val x = center.x + (n1 - 0.5f) * 82f * s
        val y = center.y + (n2 - 0.5f) * 66f * s
        val len = (5f + 11f * n3) * s

        val ridgeColor = if ((i + continentId) % 2 == 0) {
            Color(0x25F7E6B3)
        } else {
            Color(0x29000000)
        }

        drawLine(
            ridgeColor,
            Offset(x - len * 0.55f, y - len * 0.22f),
            Offset(x + len * 0.55f, y + len * 0.22f),
            strokeWidth = (1.0f * s).coerceIn(0.55f, 1.5f)
        )
    }

    // Granulação visual leve.
    repeat(10) { i ->
        val nx = noise01(territoryId * 421 + i * 89)
        val ny = noise01(territoryId * 457 + i * 97)
        val nr = noise01(territoryId * 499 + i * 101)
        val c = Offset(
            center.x + (nx - 0.5f) * 82f * s,
            center.y + (ny - 0.5f) * 68f * s
        )

        drawCircle(
            color = if (i % 2 == 0) Color(0x18FFF3C7) else Color(0x1B000000),
            radius = (0.7f + nr * 1.4f) * s.coerceIn(0.65f, 1.7f),
            center = c
        )
    }
}

private fun terrainPalette(continentId: Int): TerrainPalette = when (continentId) {
    0 -> TerrainPalette(
        light = Color(0xFF8A8C72),
        mid = Color(0xFF596B55),
        dark = Color(0xFF34483D),
        contour = Color(0xFFB7B897)
    )
    1 -> TerrainPalette(
        light = Color(0xFF82915E),
        mid = Color(0xFF4E6E42),
        dark = Color(0xFF294732),
        contour = Color(0xFFADBC82)
    )
    2 -> TerrainPalette(
        light = Color(0xFF8B866C),
        mid = Color(0xFF656A57),
        dark = Color(0xFF3D493E),
        contour = Color(0xFFC0B994)
    )
    3 -> TerrainPalette(
        light = Color(0xFFA38A58),
        mid = Color(0xFF77653F),
        dark = Color(0xFF4B442F),
        contour = Color(0xFFD0B979)
    )
    4 -> TerrainPalette(
        light = Color(0xFF9B8957),
        mid = Color(0xFF6F6947),
        dark = Color(0xFF3F4A37),
        contour = Color(0xFFC9B679)
    )
    else -> TerrainPalette(
        light = Color(0xFF8C8259),
        mid = Color(0xFF5F704B),
        dark = Color(0xFF324939),
        contour = Color(0xFFC5B37F)
    )
}

private fun noise01(seed: Int): Float {
    var x = seed * 0x45D9F3B
    x = (x xor (x ushr 16)) * 0x45D9F3B
    x = x xor (x ushr 16)
    return (x ushr 1).toFloat() / Int.MAX_VALUE.toFloat()
}

private fun DrawScope.drawLabels(
    engine: GameEngine,
    s: Float,
    project: (Float, Float) -> Offset,
    fortifications: FortificationBitmaps
) {
    val labelScale = s.coerceIn(0.72f, 1.65f)

    val namePaint = Paint().apply {
        color = android.graphics.Color.argb(238, 246, 244, 225)
        textAlign = Paint.Align.CENTER
        textSize = (9.2f * labelScale).coerceIn(7.5f, 14.5f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(4.5f, 0f, 1.4f, android.graphics.Color.argb(245, 0, 0, 0))
    }

    val armyPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = (12.2f * labelScale).coerceIn(10f, 18f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(2.5f, 0f, 1f, android.graphics.Color.BLACK)
    }

    for (t in MapData.territories) {
        val c = project(t.x, t.y)
        val owner = engine.ownerOf[t.id]
        val ownerColor = if (owner >= 0) Color(engine.players[owner].colorArgb) else Color.Gray
        val armies = engine.armiesOf[t.id]

        // Nível vem da regra do jogo (5/8/12), não de limiares próprios: o
        // pacote trazia 4 como piso, o que mostraria um posto avançado em
        // território sem bônus defensivo no Modo Tático.
        val level = Fortification.forArmies(armies).level

        if (level > 0) {
            drawFortificationAsset(
                level = level,
                anchor = Offset(c.x, c.y - 12f * labelScale),
                mapScale = s,
                accent = ownerColor,
                bitmaps = fortifications
            )
        }

        // Nome vem por cima da estrutura para nunca perder legibilidade.
        drawContext.canvas.nativeCanvas.drawText(
            t.name,
            c.x,
            c.y - 14.5f * labelScale,
            namePaint
        )

        val badgeR = (10f * labelScale).coerceIn(8f, 15f)

        drawCircle(
            Color(0x66000000),
            radius = badgeR + 3.2f,
            center = Offset(c.x + 1.5f, c.y + 2.5f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF182128), Color(0xFF050A0E)),
                center = Offset(c.x - badgeR * 0.30f, c.y - badgeR * 0.35f),
                radius = badgeR * 1.4f
            ),
            radius = badgeR,
            center = c
        )
        drawCircle(
            Color(0xFFE1B95D).copy(alpha = 0.82f),
            radius = badgeR,
            center = c,
            style = Stroke(width = (1.0f * labelScale).coerceIn(0.8f, 1.8f))
        )
        drawCircle(
            ownerColor.copy(alpha = 0.92f),
            radius = badgeR - 2.1f * labelScale,
            center = c,
            style = Stroke(width = (1.5f * labelScale).coerceIn(1.0f, 2.4f))
        )

        drawContext.canvas.nativeCanvas.drawText(
            armies.toString(),
            c.x,
            c.y + 4.2f * labelScale,
            armyPaint
        )
    }
}

private fun DrawScope.drawFortificationAsset(
    level: Int,
    anchor: Offset,
    mapScale: Float,
    accent: Color,
    bitmaps: FortificationBitmaps
) {
    val image = when (level) {
        1 -> bitmaps.outpost
        2 -> bitmaps.bunker
        else -> bitmaps.fortress
    }

    val zoom = mapScale.coerceIn(0.72f, 2.15f)
    val baseWidth = when (level) {
        1 -> 42f
        2 -> 52f
        else -> 66f
    }

    val width = (baseWidth * zoom).coerceIn(
        if (level == 1) 28f else 34f,
        if (level == 3) 122f else 94f
    )
    val aspect = image.height.toFloat() / image.width.toFloat()
    val height = width * aspect

    val dstOffset = IntOffset(
        (anchor.x - width * 0.5f).roundToInt(),
        (anchor.y - height).roundToInt()
    )
    val dstSize = IntSize(
        width.roundToInt().coerceAtLeast(1),
        height.roundToInt().coerceAtLeast(1)
    )

    // Sombra sob a estrutura.
    drawOval(
        color = Color(0x88000000),
        topLeft = Offset(
            anchor.x - width * 0.34f,
            anchor.y - height * 0.10f
        ),
        size = Size(width * 0.68f, height * 0.18f)
    )

    drawImage(
        image = image,
        dstOffset = dstOffset,
        dstSize = dstSize
    )

    // Pequeno marcador de posse sem recolorir a arte.
    val markerR = (2.6f * zoom).coerceIn(2f, 5.2f)
    val marker = Offset(
        anchor.x + width * 0.29f,
        anchor.y - height * 0.68f
    )
    drawCircle(Color(0xDD05090C), markerR + 1.4f, marker)
    drawCircle(accent, markerR, marker)
    drawCircle(
        Color(0xFFE4C36C),
        markerR,
        marker,
        style = Stroke(width = 0.8f * zoom.coerceAtMost(1.5f))
    )
}

private fun DrawScope.drawContinentBadges(
    s: Float,
    project: (Float, Float) -> Offset
) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(205, 238, 190, 94)
        textAlign = Paint.Align.CENTER
        textSize = (10.8f * s).coerceIn(8f, 16f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(230, 0, 0, 0))
    }

    for (cont in MapData.continents) {
        val pts = cont.territoryIds.map { MapData.territory(it) }
        val cx = pts.map { it.x }.average().toFloat()
        val top = pts.minOf { it.y } - 26f
        val p = project(cx, top)

        drawContext.canvas.nativeCanvas.drawText(
            "${cont.name.uppercase()}  +${cont.bonus}",
            p.x,
            p.y,
            paint
        )
    }
}

private fun DrawScope.drawCompassRose(size: Size) {
    val r = minOf(size.width, size.height) * 0.045f
    if (r < 12f) return

    val c = Offset(r * 1.55f, size.height - r * 1.55f)
    val gold = Color(0xFFCEAA5B).copy(alpha = 0.78f)
    val darkGold = Color(0xFF775D2D).copy(alpha = 0.68f)

    drawCircle(Color(0x55000000), r * 1.25f, Offset(c.x + 2f, c.y + 3f))
    drawCircle(gold.copy(alpha = 0.22f), r, c, style = Stroke(width = 1.2f))

    for (i in 0 until 4) {
        val vertical = i % 2 == 0
        val sign = if (i < 2) -1f else 1f
        val p = Path()

        if (vertical) {
            p.moveTo(c.x, c.y + sign * r)
            p.lineTo(c.x - r * 0.18f, c.y)
            p.lineTo(c.x + r * 0.18f, c.y)
        } else {
            p.moveTo(c.x + sign * r, c.y)
            p.lineTo(c.x, c.y - r * 0.18f)
            p.lineTo(c.x, c.y + r * 0.18f)
        }
        p.close()

        drawPath(p, if (i % 2 == 0) gold else darkGold)
    }

    drawCircle(Color(0xFF11191D), r * 0.20f, c)
    drawCircle(gold, r * 0.20f, c, style = Stroke(width = 1f))
}

private fun DrawScope.drawVignette(size: Size) {
    val edge = maxOf(size.width, size.height)

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                Color(0x4D000000)
            ),
            center = Offset(size.width * 0.5f, size.height * 0.48f),
            radius = edge * 0.68f
        )
    )
}
