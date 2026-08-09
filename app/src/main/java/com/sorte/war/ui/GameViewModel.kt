package com.sorte.war.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sorte.war.data.GameStorage
import com.sorte.war.data.SaveInfo
import com.sorte.war.engine.Ai
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.BattleResult
import com.sorte.war.model.Card
import com.sorte.war.model.Phase
import com.sorte.war.model.PlayerPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen { MENU, GAME }

/** Relatório narrativo do comandante entregue ao início da rodada do jogador. */
data class CommanderReport(
    val lost: List<Pair<String, String>>, // território -> cor inimiga
    val remainingTerritories: Int,
    val incomingReinforcements: Int,
    val flavor: String
)

class GameViewModel(app: Application) : AndroidViewModel(app) {

    val sound: SoundManager by lazy { SoundManager(getApplication<Application>()) }
    private val storage = GameStorage(getApplication<Application>())

    /** Resumo da partida salva (null se não houver). */
    var saveInfo by mutableStateOf<SaveInfo?>(storage.info())
        private set

    var screen by mutableStateOf(Screen.MENU)
        private set
    var engine: GameEngine? = null
        private set
    var refresh by mutableIntStateOf(0)
        private set

    var soundEnabled by mutableStateOf(true)
        private set

    private val humanId = 0

    // Seleções
    var selectedTerritory by mutableStateOf<Int?>(null)
        private set

    // Diálogos
    var battleDialog by mutableStateOf<BattleResult?>(null)
        private set
    var showAdvanceDialog by mutableStateOf(false)
        private set
    var fortifyTarget by mutableStateOf<Int?>(null)
        private set
    var showCards by mutableStateOf(false)
        private set
    var showObjective by mutableStateOf(false)
        private set
    var aiThinking by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set
    var commanderReport by mutableStateOf<CommanderReport?>(null)
        private set

    private var aiRunning = false
    private var humanTerritoriesBeforeAi: Set<Int> = emptySet()
    private var endSoundPlayed = false

    private fun bump() { refresh++ }

    fun toggleSound() {
        soundEnabled = !soundEnabled
        sound.enabled = soundEnabled
        bump()
    }

    // ---------------------------------------------------------------------
    // Navegação
    // ---------------------------------------------------------------------

    fun startGame(humanName: String, totalPlayers: Int, humanColorIndex: Int) {
        val palette = PlayerPalette.ordered
        val humanColor = palette[humanColorIndex.coerceIn(0, palette.size - 1)]
        val others = palette.filter { it != humanColor }
        val configs = buildList {
            add(GameEngine.PlayerConfig(humanName.ifBlank { "Você" }, humanColor, true))
            for (i in 1 until totalPlayers) {
                add(GameEngine.PlayerConfig("CPU $i", others[i - 1], false))
            }
        }
        engine = GameEngine(configs)
        selectedTerritory = null
        battleDialog = null
        showAdvanceDialog = false
        fortifyTarget = null
        commanderReport = null
        endSoundPlayed = false
        humanTerritoriesBeforeAi = emptySet()
        statusMessage = "Sua vez, Comandante — distribua seus reforços."
        screen = Screen.GAME
        sound.play(Sfx.CLICK)
        persist()
        bump()
    }

    /** Retoma a partida salva no aparelho. */
    fun continueGame() {
        val e = storage.load()
        if (e == null) {
            saveInfo = null
            statusMessage = "Não foi possível recuperar a partida salva."
            bump(); return
        }
        engine = e
        selectedTerritory = null
        battleDialog = null
        showAdvanceDialog = false
        fortifyTarget = null
        commanderReport = null
        endSoundPlayed = false
        humanTerritoriesBeforeAi = emptySet()
        statusMessage = if (e.currentPlayer.isHuman)
            "Partida retomada, Comandante." else "Retomando..."
        screen = Screen.GAME
        sound.play(Sfx.CLICK)
        bump()
        // se a vez era da CPU, ela continua de onde parou
        if (!e.currentPlayer.isHuman && e.winnerId == null) startAiRound()
    }

    /** Grava o estado atual (chamado após cada ação relevante). */
    private fun persist() {
        val e = engine ?: return
        storage.save(e)
        saveInfo = storage.info()
    }

    fun deleteSave() {
        storage.clear()
        saveInfo = null
        bump()
    }

    fun backToMenu() {
        persist()          // guarda a partida em andamento antes de sair
        engine = null
        screen = Screen.MENU
        bump()
    }

    fun openCards() { showCards = true; sound.play(Sfx.CLICK) }
    fun closeCards() { showCards = false }
    fun openObjective() { showObjective = true; sound.play(Sfx.CLICK) }
    fun closeObjective() { showObjective = false }
    fun dismissReport() { commanderReport = null; bump() }

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
            sound.play(Sfx.CLICK, 0.6f)
            statusMessage = if (e.reinforcements > 0)
                "Reforços restantes: ${e.reinforcements}"
            else "Reforços concluídos — avance para o ataque."
            bump()
        }
    }

    private fun handleAttackTap(e: GameEngine, id: Int) {
        val from = selectedTerritory
        if (from == null || !e.canAttackFrom(from)) {
            if (e.canAttackFrom(id)) {
                selectedTerritory = id
                statusMessage = "Escolha um território inimigo vizinho para atacar."
            } else selectedTerritory = null
            bump(); return
        }
        if (id == from) { selectedTerritory = null; bump(); return }
        if (e.ownerOf[id] == e.currentPlayerIndex) {
            selectedTerritory = if (e.canAttackFrom(id)) id else null
            bump(); return
        }
        if (id in e.attackTargets(from)) {
            val result = e.attack(from, id)
            if (result != null) {
                battleDialog = result
                if (!e.canAttackFrom(from)) selectedTerritory = null
                checkEndSound()
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
        if (e?.pendingAdvance != null) showAdvanceDialog = true else persist()
        bump()
    }

    fun confirmAdvance(extra: Int) {
        engine?.advanceMore(extra); showAdvanceDialog = false; persist(); bump()
    }

    fun cancelAdvance() {
        engine?.clearPendingAdvance(); showAdvanceDialog = false; persist(); bump()
    }

    fun confirmFortify(count: Int) {
        val e = engine ?: return
        val from = selectedTerritory
        val to = fortifyTarget
        if (from != null && to != null) {
            e.fortify(from, to, count)
            sound.play(Sfx.CLICK)
        }
        fortifyTarget = null
        selectedTerritory = null
        persist()
        bump()
    }

    fun cancelFortify() { fortifyTarget = null; bump() }

    fun tradeCards(cards: List<Card>) {
        val e = engine ?: return
        val gained = e.tradeCards(cards)
        if (gained > 0) {
            sound.play(Sfx.CONQUER, 0.7f)
            statusMessage = "Você trocou cartas e recrutou $gained exércitos!"
            persist()
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
        sound.play(Sfx.CLICK)
        statusMessage = when (e.phase) {
            Phase.ATAQUE -> "Fase de ataque — selecione um território seu."
            Phase.DESLOCAMENTO -> "Fase de deslocamento — mova tropas (opcional)."
            else -> statusMessage
        }
        persist()
        bump()
        if (wasDeslocamento) startAiRound()
    }

    // ---------------------------------------------------------------------
    // Turnos da IA + Relatório do Comandante
    // ---------------------------------------------------------------------

    private fun startAiRound() {
        val e = engine ?: return
        if (aiRunning) return
        if (e.currentPlayer.isHuman || e.winnerId != null) {
            checkEndSound(); return
        }
        // Guarda o estado dos territórios do jogador antes da rodada inimiga.
        humanTerritoriesBeforeAi = e.territoriesOf(humanId).toSet()
        aiRunning = true
        aiThinking = true
        bump()
        viewModelScope.launch {
            while (true) {
                val eng = engine ?: break
                if (eng.currentPlayer.isHuman || eng.winnerId != null) break
                statusMessage = "${eng.currentPlayer.name} avança suas tropas..."
                bump()
                delay(650)
                Ai.playTurn(eng)
                // Estrondo distante de batalha durante o turno inimigo.
                sound.play(Sfx.CANNON, 0.25f)
                bump()
                delay(250)
            }
            aiRunning = false
            aiThinking = false
            checkEndSound()
            buildCommanderReport()
            persist()
            bump()
        }
    }

    private fun buildCommanderReport() {
        val e = engine ?: return
        if (e.winnerId != null) return
        if (!e.currentPlayer.isHuman) return
        if (humanTerritoriesBeforeAi.isEmpty()) {
            statusMessage = "Sua vez, Comandante."
            return
        }
        val nowOwned = e.territoriesOf(humanId).toSet()
        val lostIds = humanTerritoriesBeforeAi - nowOwned
        val lost = lostIds.map { id ->
            val t = com.sorte.war.model.MapData.territory(id)
            val enemy = e.ownerOf[id]
            val enemyName = if (enemy in e.players.indices) e.players[enemy].name else "inimigo"
            t.name to enemyName
        }
        val flavor = if (lost.isEmpty()) HOLD_MESSAGES.random() else LOSS_MESSAGES.random()
        commanderReport = CommanderReport(
            lost = lost,
            remainingTerritories = nowOwned.size,
            incomingReinforcements = e.reinforcements,
            flavor = flavor
        )
        statusMessage = "Sua vez, Comandante — distribua seus reforços."
    }

    private fun checkEndSound() {
        val e = engine ?: return
        val w = e.winnerId ?: return
        if (endSoundPlayed) return
        endSoundPlayed = true
        if (e.players[w].isHuman) sound.play(Sfx.VICTORY) else sound.play(Sfx.DEFEAT)
    }

    override fun onCleared() {
        super.onCleared()
        sound.release()
    }

    companion object {
        private val HOLD_MESSAGES = listOf(
            "\"Nossas linhas resistiram, Comandante. O inimigo recuou.\"",
            "\"As fronteiras estão seguras. As tropas aguardam suas ordens.\"",
            "\"Nenhuma perda nesta rodada, senhor. Moral elevada!\"",
            "\"Mantivemos cada palmo de terra. Estamos prontos para avançar.\""
        )
        private val LOSS_MESSAGES = listOf(
            "\"Comandante, sofremos baixas. O inimigo tomou terreno!\"",
            "\"Perdemos posições, senhor. Precisamos reagrupar e contra-atacar!\"",
            "\"As linhas foram rompidas em alguns pontos. Aguardamos reforços!\"",
            "\"O inimigo avançou sobre nós. Vingança, Comandante!\""
        )
    }
}
