package com.sorte.war.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorte.war.engine.Ai
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.BattleResult
import com.sorte.war.model.Card
import com.sorte.war.model.Phase
import com.sorte.war.model.PlayerPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen { MENU, GAME }

class GameViewModel : ViewModel() {

    var screen by mutableStateOf(Screen.MENU)
        private set

    var engine: GameEngine? = null
        private set

    /** Contador de atualização: qualquer mutação incrementa para recompor a UI. */
    var refresh by mutableIntStateOf(0)
        private set

    // Seleções de interação
    var selectedTerritory by mutableStateOf<Int?>(null)
        private set

    // Diálogos
    var battleDialog by mutableStateOf<BattleResult?>(null)
        private set
    var showAdvanceDialog by mutableStateOf(false)
        private set
    var fortifyTarget by mutableStateOf<Int?>(null) // destino escolhido, abre slider
        private set
    var showCards by mutableStateOf(false)
        private set
    var showObjective by mutableStateOf(false)
        private set
    var aiThinking by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set

    private var aiRunning = false

    private fun bump() { refresh++ }

    // ---------------------------------------------------------------------
    // Navegação / criação de partida
    // ---------------------------------------------------------------------

    fun startGame(humanName: String, totalPlayers: Int) {
        val configs = buildList {
            add(GameEngine.PlayerConfig(humanName.ifBlank { "Você" }, PlayerPalette.ordered[0], true))
            for (i in 1 until totalPlayers) {
                add(GameEngine.PlayerConfig("CPU $i", PlayerPalette.ordered[i], false))
            }
        }
        engine = GameEngine(configs)
        selectedTerritory = null
        battleDialog = null
        showAdvanceDialog = false
        fortifyTarget = null
        statusMessage = "Sua vez — distribua seus reforços."
        screen = Screen.GAME
        bump()
        runAiIfNeeded()
    }

    fun backToMenu() {
        engine = null
        screen = Screen.MENU
        bump()
    }

    fun openCards() { showCards = true }
    fun closeCards() { showCards = false }
    fun openObjective() { showObjective = true }
    fun closeObjective() { showObjective = false }

    // ---------------------------------------------------------------------
    // Interação com o mapa
    // ---------------------------------------------------------------------

    fun onTerritoryTap(id: Int) {
        val e = engine ?: return
        if (!e.currentPlayer.isHuman || aiRunning) return
        when (e.phase) {
            Phase.REFORCO -> handleReinforceTap(e, id)
            Phase.ATAQUE -> handleAttackTap(e, id)
            Phase.DESLOCAMENTO -> handleFortifyTap(e, id)
            Phase.FIM_DE_JOGO -> {}
        }
    }

    private fun handleReinforceTap(e: GameEngine, id: Int) {
        if (e.ownerOf[id] == e.currentPlayerIndex && e.reinforcements > 0) {
            e.reinforce(id, 1)
            selectedTerritory = id
            statusMessage = if (e.reinforcements > 0)
                "Reforços restantes: ${e.reinforcements}"
            else "Reforços concluídos — toque em Avançar para atacar."
            bump()
        }
    }

    private fun handleAttackTap(e: GameEngine, id: Int) {
        val from = selectedTerritory
        if (from == null || !e.canAttackFrom(from)) {
            if (e.canAttackFrom(id)) {
                selectedTerritory = id
                statusMessage = "Escolha um território inimigo vizinho para atacar."
            } else {
                selectedTerritory = null
            }
            bump()
            return
        }
        // Já há origem selecionada
        if (id == from) { selectedTerritory = null; bump(); return }
        if (e.ownerOf[id] == e.currentPlayerIndex) {
            selectedTerritory = if (e.canAttackFrom(id)) id else null
            bump(); return
        }
        if (id in e.attackTargets(from)) {
            val result = e.attack(from, id) // move mínimo por padrão
            if (result != null) {
                battleDialog = result
                if (!e.canAttackFrom(from)) selectedTerritory = null
            }
            bump()
        }
    }

    private fun handleFortifyTap(e: GameEngine, id: Int) {
        if (e.fortifyUsed) return
        val from = selectedTerritory
        if (from == null || e.ownerOf[from] != e.currentPlayerIndex || e.armiesOf[from] < 2) {
            if (e.ownerOf[id] == e.currentPlayerIndex && e.armiesOf[id] > 1) {
                selectedTerritory = id
                statusMessage = "Escolha um território seu conectado para receber tropas."
            } else selectedTerritory = null
            bump(); return
        }
        if (id == from) { selectedTerritory = null; bump(); return }
        if (id in e.fortifyTargets(from)) {
            fortifyTarget = id
            bump()
        } else if (e.ownerOf[id] == e.currentPlayerIndex && e.armiesOf[id] > 1) {
            selectedTerritory = id
            bump()
        }
    }

    // ---------------------------------------------------------------------
    // Diálogos de ação
    // ---------------------------------------------------------------------

    fun dismissBattle() {
        val e = engine
        battleDialog = null
        if (e?.pendingAdvance != null) {
            showAdvanceDialog = true
        }
        bump()
    }

    fun confirmAdvance(extra: Int) {
        engine?.advanceMore(extra)
        showAdvanceDialog = false
        bump()
    }

    fun cancelAdvance() {
        engine?.clearPendingAdvance()
        showAdvanceDialog = false
        bump()
    }

    fun confirmFortify(count: Int) {
        val e = engine ?: return
        val from = selectedTerritory
        val to = fortifyTarget
        if (from != null && to != null) {
            e.fortify(from, to, count)
        }
        fortifyTarget = null
        selectedTerritory = null
        bump()
    }

    fun cancelFortify() {
        fortifyTarget = null
        bump()
    }

    fun tradeCards(cards: List<Card>) {
        val e = engine ?: return
        val gained = e.tradeCards(cards)
        if (gained > 0) {
            statusMessage = "Você trocou cartas e ganhou $gained exércitos!"
        }
        bump()
    }

    // ---------------------------------------------------------------------
    // Fases
    // ---------------------------------------------------------------------

    fun nextPhase() {
        val e = engine ?: return
        if (!e.currentPlayer.isHuman || aiRunning) return
        if (e.phase == Phase.REFORCO && !e.canAdvanceFromReinforce()) {
            statusMessage = "Distribua todos os reforços antes de avançar."
            bump(); return
        }
        val wasDeslocamento = e.phase == Phase.DESLOCAMENTO
        e.advancePhase()
        selectedTerritory = null
        fortifyTarget = null
        statusMessage = when (e.phase) {
            Phase.ATAQUE -> "Fase de ataque — selecione um território seu."
            Phase.DESLOCAMENTO -> "Fase de deslocamento — mova tropas (opcional)."
            else -> statusMessage
        }
        bump()
        if (wasDeslocamento) runAiIfNeeded()
    }

    // ---------------------------------------------------------------------
    // Turnos da IA
    // ---------------------------------------------------------------------

    private fun runAiIfNeeded() {
        val e = engine ?: return
        if (aiRunning) return
        if (e.currentPlayer.isHuman || e.winnerId != null) return
        aiRunning = true
        aiThinking = true
        bump()
        viewModelScope.launch {
            while (true) {
                val eng = engine ?: break
                if (eng.currentPlayer.isHuman || eng.winnerId != null) break
                statusMessage = "${eng.currentPlayer.name} está jogando..."
                bump()
                delay(700)
                Ai.playTurn(eng)
                bump()
                delay(300)
            }
            aiRunning = false
            aiThinking = false
            val eng = engine
            if (eng != null && eng.winnerId == null && eng.currentPlayer.isHuman) {
                statusMessage = "Sua vez — distribua seus reforços."
            }
            bump()
        }
    }
}
