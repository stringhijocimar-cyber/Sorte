package com.sorte.war.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sorte.war.model.Avatar

/**
 * Retrato do comandante, recortado em círculo, com um aro na cor do exército.
 *
 * A assinatura é a mesma usada em todo o app (menu, HUD, placar e diálogos),
 * então basta trocar a arte aqui para mudar em todas as telas.
 */
@Composable
fun AvatarPortrait(
    avatar: Avatar,
    sizeDp: Int,
    ringColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A141C))
    ) {
        Image(
            painter = painterResource(id = avatar.portrait),
            contentDescription = avatar.commander,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(CircleShape)
        )

        // vinheta suave, para o retrato encaixar no fundo escuro das telas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color.Transparent, Color.Transparent, Color(0x40000000))
                    )
                )
        )

        if (ringColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = if (sizeDp >= 56) 2.dp else 1.5.dp,
                        color = ringColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
