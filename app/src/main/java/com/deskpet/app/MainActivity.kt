package com.deskpet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deskpet.app.ui.navigation.NavGraph
import com.deskpet.app.ui.theme.DeskPetTheme

/**
 * Host activity for the Compose UI.
 *
 * Hosts the [NavGraph] which wires up the onboarding flow and the four
 * primary tabs (home / dress-up / health / settings) with a bottom
 * navigation bar.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeskPetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
