package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.model.Avatar
import com.sorte.war.model.ArmyPlacement
import com.sorte.war.model.Difficulty
import com.sorte.war.model.GameMode
import com.sorte.war.model.PlayerPalette
import com.sorte.war.model.SetupMode
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.components.AvatarPortrait
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.PanelNavyLight
import com.sorte.war.ui.theme.SurfaceHigh
import com.sorte.war.ui.theme.TacticalStroke
import com.sorte.war.ui.theme.TacticalTeal
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary

@Composable
fun NewGameScreen(vm: GameViewModel) {
    var name by remember { mutableStateOf(vm.stats.playerName) }
    var players by remember { mutableIntStateOf(4) }
    var colorIndex by remember { mutableIntStateOf(0) }
    var avatarIndex by remember { mutableIntStateOf(vm.stats.favoriteAvatarId) }
    var difficulty by remember { mutableStateOf(Difficulty.VETERANO) }
    var setupMode by remember { mutableStateOf(SetupMode.DADOS) }
    var gameMode by remember { mutableStateOf(GameMode.CLASSICO) }
    var placement by remember { mutableStateOf(ArmyPlacement.AUTOMATICA) }
    var pickObjective by remember { mutableStateOf(true) }

    BackHandler { vm.goTo(Screen.HOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF081923), Color(0xFF061018), NightNavy)
                )
            )
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .border(1.dp, Gold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .clickableNoRipple { vm.goTo(Screen.HOME) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Gold)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(
                    "NOVA CAMPANHA",
                    color = Gold,
                    fontWeight = FontWeight.Black,
                    fontSize = 21.sp,
                    letterSpacing = 1.9.sp
                )
                Text(
                    "CONFIGURE SUA CONQUISTA",
                    color = TacticalTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 1.3.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        SetupSection(1, "SEU NOME") {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 16) name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(2, "SEU COMANDANTE") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Avatar.all.forEachIndexed { i, av ->
                    val selected = avatarIndex == i
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(78.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (selected) TacticalTeal.copy(alpha = 0.10f)
                                else Color.Black.copy(alpha = 0.15f)
                            )
                            .border(
                                if (selected) 1.7.dp else 1.dp,
                                if (selected) TacticalTeal else TacticalStroke,
                                RoundedCornerShape(13.dp)
                            )
                            .clickableNoRipple { avatarIndex = i }
                            .padding(vertical = 8.dp, horizontal = 5.dp)
                    ) {
                        AvatarPortrait(
                            avatar = av,
                            sizeDp = if (selected) 57 else 50,
                            ringColor = if (selected) TacticalTeal else Color(0x55FFFFFF)
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            av.commander,
                            color = if (selected) TacticalTeal else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                        if (selected) {
                            Spacer(Modifier.height(3.dp))
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = TacticalTeal,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            val chosen = Avatar.byId(avatarIndex)
            Text(
                "${chosen.commander} — ${chosen.rankTitle}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                chosen.specialty,
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(3, "COR DO SEU EXÉRCITO") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PlayerPalette.ordered.forEachIndexed { i, army ->
                    val selected = colorIndex == i
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(army.argb))
                            .border(
                                if (selected) 3.dp else 1.dp,
                                if (selected) TacticalTeal else Color(0x66FFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .clickableNoRipple { colorIndex = i },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = army.name,
                                tint = if (army.argb == 0xFFFDD835 || army.argb == 0xFFECEFF1)
                                    NightNavy else Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(4, "NÚMERO DE EXÉRCITOS") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (2..6).forEach { count ->
                    val selected = players == count
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) TacticalTeal.copy(alpha = 0.13f) else SurfaceHigh
                            )
                            .border(
                                if (selected) 1.7.dp else 1.dp,
                                if (selected) TacticalTeal else TacticalStroke,
                                RoundedCornerShape(10.dp)
                            )
                            .clickableNoRipple { players = count }
                    ) {
                        Text(
                            count.toString(),
                            color = if (selected) TacticalTeal else TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(
                "Você + ${players - 1} oponente(s) controlado(s) pela CPU.",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(5, "MODO DE JOGO") {
            GameMode.entries.forEach { mode ->
                val selected = gameMode == mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (selected) TacticalTeal.copy(alpha = 0.09f)
                            else Color.Black.copy(alpha = 0.13f)
                        )
                        .border(
                            if (selected) 1.6.dp else 1.dp,
                            if (selected) TacticalTeal else TacticalStroke,
                            RoundedCornerShape(11.dp)
                        )
                        .clickableNoRipple { gameMode = mode }
                        .padding(horizontal = 11.dp, vertical = 9.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            mode.label.uppercase(),
                            color = if (selected) TacticalTeal else TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                        Text(
                            mode.tagline,
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = TacticalTeal,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(6, "DIFICULDADE") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Difficulty.entries.forEach { level ->
                    val selected = difficulty == level
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(126.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) TacticalTeal.copy(alpha = 0.09f)
                                else Color.Black.copy(alpha = 0.16f)
                            )
                            .border(
                                if (selected) 1.7.dp else 1.dp,
                                if (selected) TacticalTeal else TacticalStroke,
                                RoundedCornerShape(12.dp)
                            )
                            .clickableNoRipple { difficulty = level }
                            .padding(horizontal = 9.dp, vertical = 11.dp)
                    ) {
                        Text(
                            level.label.uppercase(),
                            color = if (selected) TacticalTeal else Gold,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            level.description,
                            color = TextSecondary,
                            fontSize = 8.sp,
                            lineHeight = 11.sp
                        )
                        if (selected) {
                            Spacer(Modifier.height(5.dp))
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = TacticalTeal,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(7, "INÍCIO DA PARTIDA") {
            SetupMode.entries.forEach { mode ->
                val selected = setupMode == mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (selected) TacticalTeal.copy(alpha = 0.09f)
                            else Color.Black.copy(alpha = 0.13f)
                        )
                        .border(
                            if (selected) 1.6.dp else 1.dp,
                            if (selected) TacticalTeal else TacticalStroke,
                            RoundedCornerShape(11.dp)
                        )
                        .clickableNoRipple { setupMode = mode }
                        .padding(horizontal = 11.dp, vertical = 9.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            mode.label.uppercase(),
                            color = if (selected) TacticalTeal else TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                        Text(
                            mode.description,
                            color = TextSecondary,
                            fontSize = 8.sp
                        )
                    }
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = TacticalTeal,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(8, "POSICIONAMENTO DAS TROPAS") {
            ArmyPlacement.entries.forEach { mode ->
                val selected = placement == mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (selected) TacticalTeal.copy(alpha = 0.09f)
                            else Color.Black.copy(alpha = 0.13f)
                        )
                        .border(
                            if (selected) 1.6.dp else 1.dp,
                            if (selected) TacticalTeal else TacticalStroke,
                            RoundedCornerShape(11.dp)
                        )
                        .clickableNoRipple { placement = mode }
                        .padding(horizontal = 11.dp, vertical = 9.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            mode.label.uppercase(),
                            color = if (selected) TacticalTeal else TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                        Text(mode.description, color = TextSecondary, fontSize = 9.sp)
                    }
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = TacticalTeal,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SetupSection(9, "OBJETIVO SECRETO") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.Black.copy(alpha = 0.13f))
                    .border(1.dp, TacticalStroke, RoundedCornerShape(11.dp))
                    .clickableNoRipple { pickObjective = !pickObjective }
                    .padding(horizontal = 11.dp, vertical = 10.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (pickObjective) "ESCOLHER ENTRE 3 CARTAS" else "SORTEAR AUTOMATICAMENTE",
                        color = if (pickObjective) TacticalTeal else TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Text(
                        if (pickObjective)
                            "Três objetivos serão apresentados para sua escolha."
                        else "O jogo selecionará seu objetivo secreto.",
                        color = TextSecondary,
                        fontSize = 8.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp, 27.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (pickObjective) TacticalTeal else SurfaceHigh)
                        .border(1.dp, TacticalStroke, RoundedCornerShape(14.dp)),
                    contentAlignment = if (pickObjective) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        Modifier
                            .padding(3.dp)
                            .size(21.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (vm.saveInfo != null) {
            Text(
                "Atenção: uma nova campanha substituirá a partida salva.",
                color = Color(0xFFE8A45B),
                fontSize = 9.sp
            )
            Spacer(Modifier.height(7.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0D5B60), Color(0xFF11363C), Color(0xFF0A1A20))
                    )
                )
                .border(1.6.dp, TacticalTeal, RoundedCornerShape(14.dp))
                .clickableNoRipple {
                    vm.startGame(
                        name, players, colorIndex, avatarIndex,
                        difficulty, setupMode, pickObjective, gameMode, placement
                    )
                }
                .padding(horizontal = 16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f))
                    .border(1.dp, Gold.copy(alpha = 0.60f), CircleShape)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Gold)
            }
            Spacer(Modifier.width(11.dp))
            Text(
                "COMEÇAR A GUERRA",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = TacticalTeal)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SetupSection(
    number: Int,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(PanelNavyLight.copy(alpha = 0.90f), PanelNavy)
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.48f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Gold.copy(alpha = 0.13f))
                    .border(1.dp, Gold.copy(alpha = 0.52f), RoundedCornerShape(7.dp))
            ) {
                Text(
                    number.toString(),
                    color = Gold,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                color = Gold,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 1.1.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}
