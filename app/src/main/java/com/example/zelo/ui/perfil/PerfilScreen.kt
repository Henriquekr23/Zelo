package com.example.zelo.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zelo.ui.theme.*

@Composable
fun PerfilScreen(
    onAbrirMeusPets: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        CabecalhoPerfil()

        Column(modifier = Modifier.padding(20.dp)) {
            SecaoMeusPets(onAbrirMeusPets = onAbrirMeusPets)
            Spacer(modifier = Modifier.height(24.dp))
            SecaoPreferencias()

            Spacer(modifier = Modifier.height(32.dp))
            TextButton(
                onClick = { /* Sair da conta(futuramente) */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sair da conta",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CabecalhoPerfil() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CM",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Camila Martins",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "(11) 98421-7730",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SecaoMeusPets(onAbrirMeusPets: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Meus pets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onAbrirMeusPets)
        )
        TextButton(onClick = onAbrirMeusPets) {
            Text(text = "ver todos", color = MaterialTheme.colorScheme.primary)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, ZeloShapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ZeloShapes.small)
                    .background(SageContainer), // Verde claro do Color.kt
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐾", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = "Frida", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Gata • Persa • 3 anos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                color = WarningContainer,
                shape = PillShape,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "ATIVO",
                    color = WarningIcon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))


    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ZeloShapes.small)
                    .background(OutlineVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐶", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = "Thor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Cão • Golden Retriever • 5 anos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SecaoPreferencias() {
    Text(
        text = "Preferências",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {

            var whatsappAtivo by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🔔 Lembretes via WhatsApp", fontWeight = FontWeight.Medium)
                Switch(
                    checked = whatsappAtivo,
                    onCheckedChange = { whatsappAtivo = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }

            Divider(color = OutlineVariant, thickness = 1.dp)


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // TODO (André, Isa ou Henrique): Coloque aqui a Intent para abrir a HistoricoActivity
                        // startActivity(Intent(context, HistoricoActivity::class.java))
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "📄 Histórico clínico completo", fontWeight = FontWeight.Medium)
                Text(text = ">", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Divider(color = OutlineVariant, thickness = 1.dp)


            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🏥 Clínica: Vida Animal", fontWeight = FontWeight.Medium)
                Text(text = ">", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}