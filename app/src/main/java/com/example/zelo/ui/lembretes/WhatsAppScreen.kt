package com.example.zelo.ui.lembretes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Academic entry point. No pilot ViewModel, credential fields or synchronization. */
@Composable
fun WhatsAppScreen(onBack: () -> Unit, onOpenDemo: () -> Unit) {
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
            .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            TextButton(onClick = onBack) { Text("Voltar") }
            Text("Lembretes pelo WhatsApp", style = MaterialTheme.typography.headlineMedium)
            Text("Conheça como o Zelo automatiza lembretes de cuidados veterinários e permite confirmar, remarcar ou cancelar atendimentos.",
                style = MaterialTheme.typography.bodyLarge)
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(36.dp))
                    Text("Modo Demonstração", style = MaterialTheme.typography.titleLarge)
                    Text("Experimente a funcionalidade diretamente no aplicativo, sem internet ou envio de mensagens reais.")
                    Button(onClick = onOpenDemo, modifier = Modifier.fillMaxWidth()) {
                        Text("Experimentar demonstração")
                    }
                }
            }
        }
    }
}
