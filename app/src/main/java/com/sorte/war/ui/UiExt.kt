package com.sorte.war.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** clique sem efeito de "ripple", útil para chips/botões customizados. */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
