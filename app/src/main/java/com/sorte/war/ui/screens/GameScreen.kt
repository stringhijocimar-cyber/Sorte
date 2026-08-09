package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sorte.war.model.Phase
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.components.AdvanceDialog
import com.sorte.war.ui.components.BattleDialog
import com.sorte.war.ui.components.BottomBar
import com.sorte.war.ui.components.CardsDialog
import com.sorte.war.ui.components.CommanderReportDialog
import com.sorte.war.ui.components.FortifyDialog
import com.sorte.war.ui.components.MapCanvas
import com.sorte.war.ui.components.ObjectiveDialog
import com.sorte.war.ui.components.TopBar
import com.sorte.war.ui.components.VictoryDialog

@Composable
fun GameScreen(vm: GameViewModel) {
    val engine = vm.engine ?: return
    val refresh = vm.refresh // dispara recomposição

    BackHandler { vm.backToMenu() }

    val selected = vm.selectedTerritory
    val validTargets: Set<Int> = when (engine.phase) {
        Phase.ATAQUE -> if (selected != null && engine.canAttackFrom(selected))
            engine.attackTargets(selected).toSet() else emptySet()
        Phase.DESLOCAMENTO -> if (selected != null)
            engine.fortifyTargets(selected).toSet() else emptySet()
        else -> emptySet()
    }

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        TopBar(
            engine = engine,
            refresh = refresh,
            soundEnabled = vm.soundEnabled,
            onToggleSound = { vm.toggleSound() },
            onObjective = { vm.openObjective() },
            onCards = { vm.openCards() }
        )

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            MapCanvas(
                engine = engine,
                refresh = refresh,
                selectedTerritory = selected,
                validTargets = validTargets,
                onTap = { vm.onTerritoryTap(it) }
            )
        }

        BottomBar(
            engine = engine,
            refresh = refresh,
            statusMessage = vm.statusMessage,
            onNextPhase = { vm.nextPhase() }
        )
    }

    // ---------------- Diálogos ----------------

    vm.battleDialog?.let { result ->
        BattleDialog(
            result = result,
            attackerName = engine.currentPlayer.name,
            defenderName = "Defensor",
            sound = vm.sound,
            onDismiss = { vm.dismissBattle() }
        )
    }

    vm.commanderReport?.let { report ->
        CommanderReportDialog(report = report, onClose = { vm.dismissReport() })
    }

    if (vm.showAdvanceDialog) {
        engine.pendingAdvance?.let { opt ->
            AdvanceDialog(
                option = opt,
                onConfirm = { vm.confirmAdvance(it) },
                onCancel = { vm.cancelAdvance() }
            )
        }
    }

    vm.fortifyTarget?.let { to ->
        val from = selected
        if (from != null) {
            FortifyDialog(
                fromName = com.sorte.war.model.MapData.territory(from).name,
                toName = com.sorte.war.model.MapData.territory(to).name,
                maxMovable = (engine.armiesOf[from] - 1).coerceAtLeast(1),
                onConfirm = { vm.confirmFortify(it) },
                onCancel = { vm.cancelFortify() }
            )
        }
    }

    if (vm.showCards) {
        CardsDialog(
            cards = engine.currentPlayer.cards.toList(),
            canTradeNow = engine.phase == Phase.REFORCO && engine.currentPlayer.isHuman,
            nextBonus = engine.nextTradeBonus(),
            isValidSet = { engine.isValidCardSet(it) },
            onTrade = { vm.tradeCards(it) },
            onClose = { vm.closeCards() }
        )
    }

    if (vm.showObjective) {
        ObjectiveDialog(
            description = engine.currentPlayer.objective?.description ?: "",
            onClose = { vm.closeObjective() }
        )
    }

    engine.winnerId?.let { wid ->
        val winner = engine.players[wid]
        VictoryDialog(
            winnerName = winner.name,
            humanWon = winner.isHuman,
            objectiveDesc = winner.objective?.description ?: "",
            onMenu = { vm.backToMenu() }
        )
    }
}
