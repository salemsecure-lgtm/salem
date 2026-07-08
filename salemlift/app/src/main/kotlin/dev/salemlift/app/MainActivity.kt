package dev.salemlift.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.salemlift.app.di.AppContainer
import dev.salemlift.app.navigation.SalemNavHost
import dev.salemlift.app.theme.SalemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SalemApplication).container
        setContent {
            SalemTheme {
                SalemRoot(container)
            }
        }
    }
}

/** Runs first-launch catalog seeding before any screen touches the database. */
@Composable
private fun SalemRoot(container: AppContainer) {
    var isSeeded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        container.ensureSeeded()
        isSeeded = true
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        if (isSeeded) SalemNavHost(container) else SeedingScreen()
    }
}

@Composable
private fun SeedingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Preparing exercise catalog…",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
