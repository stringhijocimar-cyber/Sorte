package com.sorte.war.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sorte.war.model.Avatar
import com.sorte.war.model.Headgear

/**
 * Retrato estilizado de um comandante histórico, desenhado vetorialmente
 * (sem imagens externas): rosto + elmo/toucado característico da figura.
 */
@Composable
fun AvatarPortrait(
    avatar: Avatar,
    sizeDp: Int,
    ringColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        drawPortrait(avatar, ringColor)
    }
}

private fun DrawScope.drawPortrait(avatar: Avatar, ringColor: Color?) {
    val w = size.width
    val h = size.height
    val u = minOf(w, h) / 100f          // unidade relativa
    val cx = w / 2f
    val skin = Color(avatar.skinArgb)
    val accent = Color(avatar.accentArgb)

    // fundo circular
    drawCircle(
        Brush.verticalGradient(listOf(Color(0xFF1B2C44), Color(0xFF0B1220))),
        radius = minOf(w, h) / 2f,
        center = Offset(cx, h / 2f)
    )

    // ombros
    val shoulders = Path().apply {
        moveTo(cx - 34f * u, h)
        cubicTo(cx - 32f * u, 66f * u, cx - 16f * u, 60f * u, cx, 60f * u)
        cubicTo(cx + 16f * u, 60f * u, cx + 32f * u, 66f * u, cx + 34f * u, h)
        close()
    }
    drawPath(shoulders, accent.copy(alpha = 0.92f))
    drawPath(shoulders, Color(0x33000000), style = Stroke(width = 1.5f))

    // pescoço e rosto
    drawRect(
        skin.copy(alpha = 0.95f),
        topLeft = Offset(cx - 7f * u, 50f * u),
        size = Size(14f * u, 14f * u)
    )
    drawOval(
        skin,
        topLeft = Offset(cx - 16f * u, 22f * u),
        size = Size(32f * u, 38f * u)
    )
    // olhos
    drawCircle(Color(0xFF20262E), 1.7f * u, Offset(cx - 6f * u, 40f * u))
    drawCircle(Color(0xFF20262E), 1.7f * u, Offset(cx + 6f * u, 40f * u))
    // boca
    drawLine(
        Color(0x66000000),
        Offset(cx - 4f * u, 50f * u), Offset(cx + 4f * u, 50f * u),
        strokeWidth = 1.4f * u
    )

    drawHeadgear(avatar.headgear, cx, u, accent, skin)

    // aro
    ringColor?.let {
        drawCircle(
            it, radius = minOf(w, h) / 2f - 1.5f,
            center = Offset(cx, h / 2f), style = Stroke(width = 3f)
        )
    }
}

private fun DrawScope.drawHeadgear(
    kind: Headgear, cx: Float, u: Float, accent: Color, skin: Color
) {
    val steel = Color(0xFFB9C4D0)
    val steelDark = Color(0xFF6C7A89)
    val cloth = accent

    fun dome(topY: Float, halfW: Float, hgt: Float, col: Color) {
        val p = Path().apply {
            moveTo(cx - halfW, topY + hgt)
            cubicTo(cx - halfW, topY, cx + halfW, topY, cx + halfW, topY + hgt)
            close()
        }
        drawPath(p, col)
        drawPath(p, Color(0x40000000), style = Stroke(width = 1.2f))
    }

    when (kind) {
        Headgear.ELMO_GREGO -> {
            dome(12f * u, 18f * u, 22f * u, steel)
            // protetor nasal e face
            drawRect(steel, Offset(cx - 2f * u, 28f * u), Size(4f * u, 16f * u))
            drawRect(steel, Offset(cx - 18f * u, 30f * u), Size(6f * u, 16f * u))
            drawRect(steel, Offset(cx + 12f * u, 30f * u), Size(6f * u, 16f * u))
            // crista de crina
            val crest = Path().apply {
                moveTo(cx - 14f * u, 12f * u)
                cubicTo(cx - 6f * u, 0f, cx + 6f * u, 0f, cx + 14f * u, 12f * u)
                lineTo(cx + 9f * u, 12f * u)
                cubicTo(cx + 4f * u, 5f * u, cx - 4f * u, 5f * u, cx - 9f * u, 12f * u)
                close()
            }
            drawPath(crest, cloth)
        }
        Headgear.LOUROS -> {
            // cabelo curto
            dome(20f * u, 17f * u, 14f * u, Color(0xFF4A3B2C))
            // coroa de louros
            for (i in 0 until 9) {
                val t = i / 8f
                val a = Math.PI * (0.15 + 0.7 * t)
                val rx = 19f * u
                val ry = 17f * u
                val x = cx - (Math.cos(a) * rx).toFloat()
                val y = 34f * u - (Math.sin(a) * ry).toFloat()
                drawOval(
                    Color(0xFF3E8E41),
                    topLeft = Offset(x - 3f * u, y - 1.6f * u),
                    size = Size(6f * u, 3.2f * u)
                )
            }
            drawArc(
                cloth, 200f, 140f, false,
                topLeft = Offset(cx - 19f * u, 17f * u), size = Size(38f * u, 34f * u),
                style = Stroke(width = 2f * u)
            )
        }
        Headgear.ELMO_PUNICO -> {
            dome(14f * u, 18f * u, 20f * u, Color(0xFFA8863F))
            drawRect(Color(0xFFA8863F), Offset(cx - 19f * u, 30f * u), Size(38f * u, 4f * u))
            // chifres laterais
            drawArc(
                cloth, 180f, 160f, false,
                topLeft = Offset(cx - 30f * u, 18f * u), size = Size(18f * u, 18f * u),
                style = Stroke(width = 2.6f * u)
            )
            drawArc(
                cloth, 200f, 160f, false,
                topLeft = Offset(cx + 12f * u, 18f * u), size = Size(18f * u, 18f * u),
                style = Stroke(width = 2.6f * u)
            )
        }
        Headgear.NEMES -> {
            // toucado listrado
            val head = Path().apply {
                moveTo(cx - 22f * u, 56f * u)
                lineTo(cx - 20f * u, 24f * u)
                cubicTo(cx - 14f * u, 12f * u, cx + 14f * u, 12f * u, cx + 20f * u, 24f * u)
                lineTo(cx + 22f * u, 56f * u)
                lineTo(cx + 14f * u, 56f * u)
                lineTo(cx + 15f * u, 30f * u)
                lineTo(cx - 15f * u, 30f * u)
                lineTo(cx - 14f * u, 56f * u)
                close()
            }
            drawPath(head, Color(0xFF2F6FA8))
            for (i in 0 until 4) {
                val y = 34f * u + i * 5f * u
                drawLine(cloth, Offset(cx - 21f * u, y), Offset(cx - 15f * u, y), strokeWidth = 1.6f * u)
                drawLine(cloth, Offset(cx + 15f * u, y), Offset(cx + 21f * u, y), strokeWidth = 1.6f * u)
            }
            // uraeus
            drawCircle(Color(0xFFD4AF37), 2.6f * u, Offset(cx, 22f * u))
        }
        Headgear.ELMO_CAVALEIRO -> {
            dome(10f * u, 18f * u, 24f * u, steel)
            drawRect(steel, Offset(cx - 18f * u, 32f * u), Size(36f * u, 16f * u))
            // visor
            drawRect(Color(0xFF1A222C), Offset(cx - 15f * u, 37f * u), Size(30f * u, 3.4f * u))
            drawRect(Color(0xFF1A222C), Offset(cx - 15f * u, 43f * u), Size(30f * u, 2.4f * u))
            drawLine(steelDark, Offset(cx, 32f * u), Offset(cx, 48f * u), strokeWidth = 1.4f * u)
            // penacho
            val plume = Path().apply {
                moveTo(cx, 8f * u)
                cubicTo(cx + 10f * u, 0f, cx + 16f * u, 8f * u, cx + 10f * u, 16f * u)
                cubicTo(cx + 6f * u, 10f * u, cx + 3f * u, 8f * u, cx, 8f * u)
                close()
            }
            drawPath(plume, cloth)
        }
        Headgear.ELMO_ESTEPE -> {
            dome(14f * u, 17f * u, 18f * u, Color(0xFF8A6A3A))
            // ponta
            drawLine(Color(0xFFD4AF37), Offset(cx, 14f * u), Offset(cx, 4f * u), strokeWidth = 2.4f * u)
            drawCircle(Color(0xFFD4AF37), 2f * u, Offset(cx, 4f * u))
            // barra de pele
            drawRect(cloth, Offset(cx - 20f * u, 28f * u), Size(40f * u, 7f * u))
            drawRect(Color(0x33000000), Offset(cx - 20f * u, 28f * u), Size(40f * u, 7f * u), style = Stroke(1f))
            // bigode
            drawLine(Color(0xFF2E2A24), Offset(cx - 7f * u, 47f * u), Offset(cx - 2f * u, 46f * u), strokeWidth = 1.8f * u)
            drawLine(Color(0xFF2E2A24), Offset(cx + 7f * u, 47f * u), Offset(cx + 2f * u, 46f * u), strokeWidth = 1.8f * u)
        }
        Headgear.TURBANTE -> {
            dome(16f * u, 20f * u, 18f * u, cloth)
            drawArc(
                Color(0x33FFFFFF), 200f, 140f, false,
                topLeft = Offset(cx - 20f * u, 16f * u), size = Size(40f * u, 26f * u),
                style = Stroke(width = 2.4f * u)
            )
            drawCircle(Color(0xFFD4AF37), 2.4f * u, Offset(cx, 20f * u))
            // barba
            val beard = Path().apply {
                moveTo(cx - 13f * u, 48f * u)
                cubicTo(cx - 10f * u, 64f * u, cx + 10f * u, 64f * u, cx + 13f * u, 48f * u)
                close()
            }
            drawPath(beard, Color(0xFF2B2620))
        }
        Headgear.BICORNE -> {
            val hat = Path().apply {
                moveTo(cx - 30f * u, 26f * u)
                cubicTo(cx - 16f * u, 6f * u, cx + 16f * u, 6f * u, cx + 30f * u, 26f * u)
                cubicTo(cx + 14f * u, 20f * u, cx - 14f * u, 20f * u, cx - 30f * u, 26f * u)
                close()
            }
            drawPath(hat, Color(0xFF1B2436))
            drawPath(hat, Color(0x55FFFFFF), style = Stroke(width = 1.2f))
            // cocar
            drawRect(cloth, Offset(cx - 2f * u, 12f * u), Size(4f * u, 10f * u))
        }
        Headgear.QUEPE -> {
            drawRect(cloth, Offset(cx - 17f * u, 18f * u), Size(34f * u, 12f * u))
            drawRect(Color(0xFF14181E), Offset(cx - 19f * u, 28f * u), Size(38f * u, 4f * u))
            drawOval(Color(0xFF14181E), Offset(cx - 20f * u, 30f * u), Size(40f * u, 5f * u))
            drawCircle(Color(0xFFD4AF37), 2.6f * u, Offset(cx, 24f * u))
            // costeletas
            drawRect(Color(0xFF3A2E24), Offset(cx - 17f * u, 36f * u), Size(4f * u, 12f * u))
            drawRect(Color(0xFF3A2E24), Offset(cx + 13f * u, 36f * u), Size(4f * u, 12f * u))
        }
        Headgear.FAIXA -> {
            dome(18f * u, 17f * u, 16f * u, Color(0xFF241C16))
            drawRect(cloth, Offset(cx - 18f * u, 26f * u), Size(36f * u, 5f * u))
            // pontas da faixa
            drawLine(cloth, Offset(cx + 16f * u, 30f * u), Offset(cx + 24f * u, 40f * u), strokeWidth = 2.4f * u)
            drawCircle(Color(0xFFD4AF37), 2.2f * u, Offset(cx, 28.5f * u))
        }
        Headgear.ELMO_CELTA -> {
            dome(16f * u, 17f * u, 16f * u, Color(0xFF9AA6B2))
            // cabelo ruivo comprido
            drawOval(cloth, Offset(cx - 24f * u, 30f * u), Size(10f * u, 30f * u))
            drawOval(cloth, Offset(cx + 14f * u, 30f * u), Size(10f * u, 30f * u))
            // torque
            drawArc(
                Color(0xFFD4AF37), 200f, 140f, false,
                topLeft = Offset(cx - 12f * u, 54f * u), size = Size(24f * u, 16f * u),
                style = Stroke(width = 2.6f * u)
            )
        }
        Headgear.ELMO_CHINES -> {
            dome(16f * u, 18f * u, 16f * u, Color(0xFF7E5E33))
            drawRect(Color(0xFF7E5E33), Offset(cx - 20f * u, 28f * u), Size(40f * u, 5f * u))
            // ponta com borla
            drawLine(Color(0xFF7E5E33), Offset(cx, 16f * u), Offset(cx, 8f * u), strokeWidth = 2f * u)
            drawCircle(cloth, 3f * u, Offset(cx, 6f * u))
            // barba fina
            drawLine(Color(0xFF2B2620), Offset(cx - 5f * u, 54f * u), Offset(cx - 6f * u, 64f * u), strokeWidth = 1.6f * u)
            drawLine(Color(0xFF2B2620), Offset(cx + 5f * u, 54f * u), Offset(cx + 6f * u, 64f * u), strokeWidth = 1.6f * u)
        }
    }
}
