package com.sorte.war.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sorte.war.model.Card
import com.sorte.war.model.CardSymbol
import com.sorte.war.model.ClassicCardArt
import com.sorte.war.model.MapData

private val CardBlack = Color(0xFF0A0D14)
private val CardEdge = Color(0xFF2C3646)
private val SilhouetteCyan = Color(0xFF2FD3F5)
private val SymbolRed = Color(0xFFE23B3B)
private val JokerGold = Color(0xFFFFC24B)

/**
 * Carta do War desenhada como a do tabuleiro: fundo escuro, nome do território
 * no topo, a silhueta real do território em ciano e o símbolo (círculo,
 * quadrado ou triângulo) embaixo.
 */
@Composable
fun WarCard(
    card: Card,
    selected: Boolean,
    modifier: Modifier = Modifier,
    width: Int = 104,
    height: Int = 152
) {
    val isJoker = card.symbol == CardSymbol.CORINGA
    val accent = if (isJoker) JokerGold else SymbolRed

    // Enquanto o baralho ilustrado não estiver completo, a mão continua
    // desenhada — misturar carta pintada com carta desenhada fica feio.
    val painted = if (ClassicCardArt.isComplete(MapData.territories.size)) {
        if (isJoker) ClassicCardArt.jokerArt else ClassicCardArt.of(card.territoryId)
    } else null

    if (painted != null) {
        Box(
            modifier = modifier
                .width(width.dp)
                .height(height.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CardBlack)
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) JokerGold else CardEdge,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Image(
                painter = painterResource(id = painted),
                contentDescription = if (isJoker) "Coringa"
                else MapData.territory(card.territoryId).name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    if (isJoker) listOf(Color(0xFF2A1E06), Color(0xFF0A0D14))
                    else listOf(Color(0xFF141A26), CardBlack)
                )
            )
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) JokerGold else CardEdge,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp, vertical = 7.dp)
    ) {
        Text(
            text = if (isJoker) "CORINGA" else MapData.territory(card.territoryId).name,
            color = Color(0xFFEFF4FA),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        // Silhueta real do território (ou estrela, no coringa)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((height * 0.52f).dp),
            contentAlignment = Alignment.Center
        ) {
            if (isJoker) {
                Canvas(Modifier.size((width * 0.5f).dp)) {
                    drawPath(starPath(size.width, size.height), JokerGold)
                }
            } else {
                Canvas(Modifier.fillMaxWidth().height((height * 0.5f).dp)) {
                    val rings = MapData.shapeOf(card.territoryId)
                    if (rings.isEmpty()) return@Canvas

                    // enquadra a silhueta na área da carta
                    var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
                    var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
                    for (r in rings) {
                        var i = 0
                        while (i < r.size) {
                            if (r[i] < minX) minX = r[i]
                            if (r[i] > maxX) maxX = r[i]
                            if (r[i + 1] < minY) minY = r[i + 1]
                            if (r[i + 1] > maxY) maxY = r[i + 1]
                            i += 2
                        }
                    }
                    val gw = (maxX - minX).coerceAtLeast(0.001f)
                    val gh = (maxY - minY).coerceAtLeast(0.001f)
                    val k = minOf(size.width * 0.86f / gw, size.height * 0.92f / gh)
                    val offX = (size.width - gw * k) / 2f - minX * k
                    val offY = (size.height - gh * k) / 2f - minY * k

                    val path = Path()
                    for (r in rings) {
                        if (r.size < 6) continue
                        path.moveTo(r[0] * k + offX, r[1] * k + offY)
                        var i = 2
                        while (i < r.size) {
                            path.lineTo(r[i] * k + offX, r[i + 1] * k + offY)
                            i += 2
                        }
                        path.close()
                    }
                    drawPath(path, SilhouetteCyan.copy(alpha = 0.30f))
                    drawPath(path, SilhouetteCyan, style = Stroke(width = 1.6f))
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        // Símbolo do tabuleiro
        Box(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(20.dp)) {
                when (card.symbol) {
                    CardSymbol.CIRCULO -> drawCircle(SymbolRed, radius = size.minDimension / 2.2f)
                    CardSymbol.QUADRADO -> {
                        val s = size.minDimension * 0.86f
                        drawRect(
                            SymbolRed,
                            topLeft = Offset((size.width - s) / 2f, (size.height - s) / 2f),
                            size = Size(s, s)
                        )
                    }
                    CardSymbol.TRIANGULO -> {
                        val p = Path().apply {
                            moveTo(size.width / 2f, size.height * 0.08f)
                            lineTo(size.width * 0.94f, size.height * 0.90f)
                            lineTo(size.width * 0.06f, size.height * 0.90f)
                            close()
                        }
                        drawPath(p, SymbolRed)
                    }
                    CardSymbol.CORINGA -> drawPath(starPath(size.width, size.height), JokerGold)
                }
            }
        }

        Text(
            "W A R",
            color = accent.copy(alpha = 0.85f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun starPath(w: Float, h: Float): Path {
    val cx = w / 2f; val cy = h / 2f
    val rOut = minOf(w, h) / 2f
    val rIn = rOut * 0.42f
    val p = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) rOut else rIn
        val a = -Math.PI / 2 + i * Math.PI / 5
        val x = cx + (r * Math.cos(a)).toFloat()
        val y = cy + (r * Math.sin(a)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    return p
}
