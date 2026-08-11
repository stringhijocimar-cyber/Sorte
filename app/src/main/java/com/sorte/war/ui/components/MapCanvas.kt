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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sorte.war.R
import com.sorte.war.model.Fortification
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

    // Recursos carregados uma vez e reaproveitados em todos os quadros.
    val terrain = ImageBitmap.imageResource(R.drawable.map_terrain)
    val outpost = ImageBitmap.imageResource(R.drawable.fortification_outpost)
    val bunker = ImageBitmap.imageResource(R.drawable.fortification_bunker)
    val fortress = ImageBitmap.imageResource(R.drawable.fortification_fortress)

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
            .background(Brush.verticalGradient(listOf(Color(0xFF0C3243), Color(0xFF072030), Color(0xFF04121C))))
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

        drawOceanDepth(size, proj)
        drawWatermark(size)
        drawOceanGrid(proj)
        drawRoutes(proj)
        drawLandmasses(engine, selectedTerritory, validTargets, s, proj)
        // Textura de relevo por cima de tudo, num único blit: dá granulação
        // ao terreno e ao mar sem custar 42 desenhos por quadro.
        drawTerrainTexture(terrain, size)
        drawOceanNames(s, proj)
        drawFortifications(engine, s, proj, outpost, bunker, fortress)
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

/**
 * Marca d'água do jogo: emblema de globo com espadas cruzadas, bem discreto,
 * ancorado na tela (não acompanha o zoom do mapa).
 */
private fun DrawScope.drawWatermark(size: Size) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.52f
    val r = minOf(size.width, size.height) * 0.30f
    val ink = Color(0xFF6CB7B3).copy(alpha = 0.045f)

    // espadas cruzadas
    val len = r * 1.35f
    drawLine(ink, Offset(cx - len, cy + len), Offset(cx + len, cy - len), strokeWidth = r * 0.09f)
    drawLine(ink, Offset(cx + len, cy + len), Offset(cx - len, cy - len), strokeWidth = r * 0.09f)

    // globo
    drawCircle(ink, radius = r, center = Offset(cx, cy), style = Stroke(width = r * 0.07f))
    drawCircle(ink, radius = r * 0.62f, center = Offset(cx, cy), style = Stroke(width = r * 0.05f))
    drawLine(ink, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = r * 0.05f)

    // paralelos
    for (f in listOf(0.45f, 0.8f)) {
        val rx = r * kotlin.math.sqrt(1f - f * f)
        drawLine(ink, Offset(cx - rx, cy - r * f), Offset(cx + rx, cy - r * f), strokeWidth = r * 0.035f)
        drawLine(ink, Offset(cx - rx, cy + r * f), Offset(cx + rx, cy + r * f), strokeWidth = r * 0.035f)
    }
}

/**
 * Profundidade do mar: manchas mais claras nas plataformas continentais e
 * escurecimento nas bordas da tela, para o mapa parecer uma mesa iluminada.
 */
private fun DrawScope.drawOceanDepth(size: Size, project: (Float, Float) -> Offset) {
    // bancos rasos junto às massas de terra
    val shelves = listOf(
        Triple(360f, 150f, 190f),   // Atlântico norte
        Triple(700f, 250f, 210f),   // Índico
        Triple(120f, 120f, 170f),   // Pacífico leste
        Triple(900f, 150f, 200f)    // Pacífico oeste
    )
    for ((x, y, r) in shelves) {
        val c = project(x, y)
        val rr = r * (size.width / VW)
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0x3A1C6B7A), Color(0x001C6B7A)),
                center = c, radius = rr
            ),
            radius = rr, center = c
        )
    }
    // vinheta
    val maxR = kotlin.math.max(size.width, size.height) * 0.78f
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0x00000000), Color(0x00000000), Color(0x5A000814)),
            center = Offset(size.width / 2f, size.height / 2f), radius = maxR
        ),
        radius = maxR, center = Offset(size.width / 2f, size.height / 2f)
    )
}

/** Nomes dos oceanos, discretos, para não competirem com o mapa. */
private fun DrawScope.drawOceanNames(s: Float, project: (Float, Float) -> Offset) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(64, 168, 214, 224)
        textAlign = Paint.Align.CENTER
        textSize = 8.5f * s
        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        isAntiAlias = true
        letterSpacing = 0.28f
    }
    val names = listOf(
        Triple("MAR ÁRTICO", 480f, 16f),
        Triple("OCEANO ATLÂNTICO", 395f, 190f),
        Triple("OCEANO PACÍFICO", 88f, 250f),
        Triple("OCEANO PACÍFICO", 952f, 205f),
        Triple("OCEANO ÍNDICO", 680f, 320f)
    )
    for ((text, x, y) in names) {
        val p = project(x, y)
        drawContext.canvas.nativeCanvas.drawText(text, p.x, p.y, paint)
    }
}

/**
 * Granulação de relevo aplicada sobre o mapa inteiro em uma passada só.
 * BlendMode.Overlay escurece o que é escuro e clareia o que é claro, então a
 * textura acompanha o terreno em vez de cobri-lo.
 */
private fun DrawScope.drawTerrainTexture(texture: ImageBitmap, size: Size) {
    val tile = 260f
    val cols = kotlin.math.ceil(size.width / tile).toInt() + 1
    val rows = kotlin.math.ceil(size.height / tile).toInt() + 1
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            drawImage(
                image = texture,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(texture.width, texture.height),
                dstOffset = IntOffset((c * tile).toInt(), (r * tile).toInt()),
                dstSize = IntSize(tile.toInt(), tile.toInt()),
                alpha = 0.30f,
                blendMode = BlendMode.Overlay
            )
        }
    }
}

private fun DrawScope.drawOceanGrid(project: (Float, Float) -> Offset) {
    val grid = Color(0x0E6CB7B3)
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
            color = Color(0xFF82B8C7).copy(alpha = if (far) 0.28f else 0.10f),
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
                    listOf(Color(0x4CFFFFFF), Color(0x00FFFFFF)),
                    center = Offset(center.x - 14f * s, center.y - 16f * s),
                    radius = 46f * s
                ),
                radius = 46f * s,
                center = Offset(center.x - 14f * s, center.y - 16f * s)
            )
            // sombra do relevo a sudeste
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0x48000000), Color(0x00000000)),
                    center = Offset(center.x + 16f * s, center.y + 18f * s),
                    radius = 44f * s
                ),
                radius = 44f * s,
                center = Offset(center.x + 16f * s, center.y + 18f * s)
            )
            // Cor do exército puxada para um tom terroso antes de entrar como
            // véu: a cor pura, mesmo translúcida, chapava o relevo.
            val muted = lerp(ownerColor, Color(0xFF6E6A52), 0.34f)
            drawRect(muted.copy(alpha = 0.52f))

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

        // Fronteira: sombra externa fina e linha dourada envelhecida por cima.
        drawPath(path, Color(0xCC030910), style = Stroke(width = 2.6f))
        drawPath(path, Color(0xB8C9A96A), style = Stroke(width = 1.1f))
        when {
            t.id == selected -> {
                drawPath(path, Color(0x55E8B85A), style = Stroke(width = 7f))
                drawPath(path, Color(0xFFF3CB77), style = Stroke(width = 2.8f))
            }
            t.id in validTargets -> {
                drawPath(path, Color(0x4458B77B), style = Stroke(width = 6f))
                drawPath(path, Color(0xFF65D17E), style = Stroke(width = 2.4f))
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

        drawContext.canvas.nativeCanvas.drawText(t.name, c.x, c.y - 15f * s, namePaint)

        // Emblema de tropas: metal escuro, aro dourado e um filete na cor do
        // exército, para o número continuar legível no zoom mínimo.
        val badgeR = 10.5f * s
        drawCircle(Color(0x99000000), radius = badgeR * 1.16f, center = Offset(c.x, c.y + 1.4f * s))
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF243240), Color(0xFF070D14)),
                center = Offset(c.x - badgeR * 0.35f, c.y - badgeR * 0.4f),
                radius = badgeR * 1.7f
            ),
            radius = badgeR, center = c
        )
        drawCircle(ownerColor.copy(alpha = 0.85f), radius = badgeR, center = c, style = Stroke(width = 2.4f))
        drawCircle(Color(0xCCE8B85A), radius = badgeR - 2.2f, center = c, style = Stroke(width = 0.9f))
        drawContext.canvas.nativeCanvas.drawText(armies.toString(), c.x, c.y + 4.2f * s, armyPaint)
    }
}

/**
 * Estruturas militares no mapa, em 2.5D.
 *
 * O sprite é escalado pelo nível e limitado por baixo e por cima: no zoom
 * mínimo continua reconhecível, no zoom alto não vira um outdoor. Fica um
 * pouco acima e à esquerda do emblema de tropas, para não cobrir o número.
 */
private fun DrawScope.drawFortifications(
    engine: GameEngine,
    s: Float,
    project: (Float, Float) -> Offset,
    outpost: ImageBitmap,
    bunker: ImageBitmap,
    fortress: ImageBitmap
) {
    for (t in MapData.territories) {
        val fort = Fortification.forArmies(engine.armiesOf[t.id])
        if (fort == Fortification.NENHUMA) continue

        val sprite = when (fort) {
            Fortification.POSTO -> outpost
            Fortification.BUNKER -> bunker
            else -> fortress
        }
        val baseSize = when (fort) {
            Fortification.POSTO -> 34f
            Fortification.BUNKER -> 41f
            else -> 50f
        }
        val side = (baseSize * s).coerceIn(24f, 150f)
        val c = project(t.x, t.y)
        val left = c.x - side / 2f
        val top = c.y - side * 0.92f

        val owner = engine.ownerOf[t.id]
        if (owner >= 0) {
            // halo na cor do exército: diz de quem é a estrutura sem bandeira
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(engine.players[owner].colorArgb).copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(c.x, top + side * 0.72f), radius = side * 0.52f
                ),
                radius = side * 0.52f, center = Offset(c.x, top + side * 0.72f)
            )
        }

        drawImage(
            image = sprite,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(sprite.width, sprite.height),
            dstOffset = IntOffset(left.toInt(), top.toInt()),
            dstSize = IntSize(side.toInt(), side.toInt()),
            alpha = 0.97f
        )
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
