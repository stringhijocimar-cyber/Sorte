package com.sorte.war

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sorte.war.data.ScreenOrientationMode
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.screens.ArsenalScreen
import com.sorte.war.ui.screens.GameScreen
import com.sorte.war.ui.screens.HomeScreen
import com.sorte.war.ui.screens.HowToPlayScreen
import com.sorte.war.ui.screens.NewGameScreen
import com.sorte.war.ui.screens.StatsScreen
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.WarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WarTheme {
                Surface(modifier = Modifier.fillMaxSize().background(NightNavy)) {
                    WarApp()
                }
            }
        }
    }
}

@Composable
fun WarApp(vm: GameViewModel = viewModel()) {
    // Aplica a orientação de tela escolhida pelo jogador.
    val context = LocalContext.current
    LaunchedEffect(vm.orientationMode) {
        (context as? Activity)?.requestedOrientation = when (vm.orientationMode) {
            ScreenOrientationMode.RETRATO -> ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            ScreenOrientationMode.PAISAGEM -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
            ScreenOrientationMode.AUTOMATICO -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }
    }

    when (vm.screen) {
        Screen.HOME -> HomeScreen(vm)
        Screen.NEW_GAME -> NewGameScreen(vm)
        Screen.STATS -> StatsScreen(vm)
        Screen.HOW_TO_PLAY -> HowToPlayScreen(vm)
        Screen.ARSENAL -> ArsenalScreen(vm)
        Screen.GAME -> GameScreen(vm)
    }
}
