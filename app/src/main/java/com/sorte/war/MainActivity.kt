package com.sorte.war

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.screens.GameScreen
import com.sorte.war.ui.screens.MenuScreen
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
    when (vm.screen) {
        Screen.MENU -> MenuScreen(vm)
        Screen.GAME -> GameScreen(vm)
    }
}
