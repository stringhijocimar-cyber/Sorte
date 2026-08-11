# WAR — Domínio Mundial 🌍⚔️

Jogo de estratégia para **Android** inspirado no clássico jogo de tabuleiro **War**
(conquista de territórios e dominação mundial). Escrito 100% em **Kotlin** com
**Jetpack Compose**, com mapa interativo, combate com dados, cartas de território,
objetivos secretos e oponentes controlados por IA.

<p align="center">
  <em>Conquiste territórios, cumpra seu objetivo secreto e domine o mundo.</em>
</p>

---

## ✨ Funcionalidades

- **Mapa-múndi com 42 territórios** agrupados em **6 continentes** (América do Norte,
  América do Sul, Europa, África, Ásia e Oceania), com adjacências fiéis ao tabuleiro.
- **Mapa interativo** desenhado em `Canvas`, com **zoom (pinça)** e **arraste (pan)**,
  destaque de seleção e de alvos válidos, contagem de exércitos por território.
- **Turno completo em 3 fases**, como no tabuleiro:
  1. **Reforço** — recebe exércitos por territórios (nº ÷ 2, mínimo 3) + bônus de
     continente completo + troca de cartas.
  2. **Ataque** — combate por **dados** (até 3 de ataque × até 3 de defesa; empate
     favorece o defensor), com conquista e escolha de quantas tropas avançar.
  3. **Deslocamento** — mover tropas entre territórios próprios conectados.
- **Cartas de território** (Infantaria, Cavalaria, Canhão e Coringa) com **troca
  escalonada** (4, 6, 8, 10, 12, 15, +5…) e bônus de +2 em território seu ilustrado.
- **Objetivos secretos** (conquistar continentes, conquistar N territórios, ou
  destruir uma cor), com substituição automática quando o objetivo se torna inválido.
- **Eliminação de jogadores** com herança de cartas do eliminado (regra do War).
- **De 2 a 6 jogadores**: você + oponentes controlados por uma **IA** que reforça
  fronteiras, ataca com vantagem e reagrupa tropas.
- **Condições de vitória**: cumprir o objetivo secreto ou ser o último em jogo.
- Interface **moderna e escura**, tela de menu, HUD com placar dos jogadores,
  animação de dados e tela de vitória.

---

## 🏗️ Arquitetura

```
app/src/main/java/com/sorte/war/
├── MainActivity.kt            # Ponto de entrada Compose
├── model/                     # Dados (sem dependências de Android)
│   ├── Models.kt              # Territory, Continent, Player, Card, Objective, Phase…
│   ├── MapData.kt             # 42 territórios, continentes, adjacências, posições
│   └── Objectives.kt          # Baralho de objetivos + paleta de cores
├── engine/                    # Regras (Kotlin puro, testável isoladamente)
│   ├── GameEngine.kt          # Estado da partida e toda a lógica de regras
│   └── Ai.kt                  # IA dos oponentes
└── ui/                        # Camada Compose
    ├── GameViewModel.kt       # Estado de UI e orquestração dos turnos da IA
    ├── theme/                 # Cores, tipografia e tema
    ├── screens/               # MenuScreen, GameScreen
    └── components/            # MapCanvas, Hud, Dialogs (dados, cartas, objetivo…)
```

A lógica de jogo (`model` + `engine` + `ai`) não depende do Android e pode ser
compilada e simulada de forma isolada.

---

## ▶️ Como compilar e rodar

Requisitos: **Android Studio** (Koala/Ladybug ou mais recente), **JDK 17** e o
**Android SDK** (compileSdk 35).

1. Abra a pasta do projeto no Android Studio (ou clone e abra).
2. Aponte o SDK do Android (o Studio cria o `local.properties` automaticamente;
   se preferir, defina a variável de ambiente `ANDROID_HOME`).
3. Selecione um emulador ou dispositivo (Android 7.0 / API 24 ou superior).
4. Clique em **Run ▶**.

Pela linha de comando (com o SDK instalado):

```bash
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala no dispositivo/emulador conectado
```

> Observação: este repositório inclui o Gradle Wrapper. O download do Android
> Gradle Plugin e do SDK exige acesso ao repositório Maven do Google
> (`dl.google.com` / `maven.google.com`).

---

## 🎮 Como jogar (resumo das regras)

1. **Objetivo**: cumpra sua carta de objetivo secreta (veja no ícone de bandeira).
2. **Reforço**: toque nos seus territórios para posicionar os exércitos recebidos.
   Troque cartas na fase de reforço para ganhar tropas extras.
3. **Ataque**: toque num território seu (com 2+ exércitos), depois num território
   inimigo vizinho destacado. Os dados decidem as baixas. Ao conquistar, escolha
   quantas tropas avançar.
4. **Deslocamento**: mova tropas entre territórios seus conectados (uma vez por turno).
5. Toque em **Encerrar Turno** para passar a vez.

Bônus de continente: América do Norte **+5**, América do Sul **+2**, Europa **+5**,
África **+3**, Ásia **+7**, Oceania **+2**.

---

Feito com Kotlin + Jetpack Compose.
