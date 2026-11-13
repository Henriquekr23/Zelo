package com.example.zelo.ui.clinic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zelo.demo.clinic.ClinicDemoData
import com.example.zelo.viewmodel.ClinicDemoViewModel

@Composable
fun ClinicDemoScreen(model: ClinicDemoViewModel, onBack: () -> Unit) {
    Scaffold(topBar = {
        Surface {
            Row(Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Voltar") }
                Text("Minha clínica", style = MaterialTheme.typography.titleLarge)
            }
        }
    }) { padding ->
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp,
            top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 24.dp),
            modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("CLÍNICA DEMONSTRATIVA", style = MaterialTheme.typography.labelSmall)
                        Text(ClinicDemoData.NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Cuidando da saúde e do bem-estar dos seus pets.")
                        Text("Clínica veterinária • Atendimento disponível na simulação",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        Box(Modifier.fillMaxWidth().height(250.dp)) { ClinicIllustration(model.routeVisible) }
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(ClinicDemoData.MAP_NOTICE, style = MaterialTheme.typography.bodySmall)
                            Button(onClick = model::toggleRoute, modifier = Modifier.fillMaxWidth()) {
                                Text(if (model.routeVisible) "Ocultar trajeto" else "Simular trajeto")
                            }
                            if (model.routeVisible) {
                                Text("Trajeto demonstrativo", fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                                Text("${ClinicDemoData.DISTANCE} • ${ClinicDemoData.DURATION} — valores ilustrativos")
                                Text("Partida fictícia → Clínica Jardim Veterinária",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Endereço fictício", style = MaterialTheme.typography.titleMedium)
                        Text(ClinicDemoData.ADDRESS)
                        Text(ClinicDemoData.CITY, style = MaterialTheme.typography.bodySmall)
                        Text("A posição no desenho é apenas ilustrativa. Não representa seu endereço nem sua localização.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Serviços veterinários", style = MaterialTheme.typography.titleMedium)
                        ClinicDemoData.services.forEach { Text("• $it") }
                        HorizontalDivider()
                        Text("Funcionamento demonstrativo", style = MaterialTheme.typography.titleMedium)
                        Text(ClinicDemoData.HOURS)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicIllustration(routeVisible: Boolean) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val land = if (dark) Color(0xFF273A32) else Color(0xFFE3EDDF)
    val block = if (dark) Color(0xFF34463D) else Color(0xFFF6F1E7)
    val road = if (dark) Color(0xFF617067) else Color.White
    val routeColor = MaterialTheme.colorScheme.primary
    val progress by animateFloatAsState(if (routeVisible) 1f else 0f, label = "Illustrative route")
    BoxWithConstraints(Modifier.fillMaxSize().background(land).semantics {
        contentDescription = "Mapa ilustrativo da Clínica Jardim Veterinária. Ruas e quadras fictícias."
        stateDescription = if (routeVisible) "Trajeto demonstrativo visível" else "Sem trajeto demonstrativo"
    }) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (x in listOf(0.04f, 0.43f, 0.81f)) {
                for (y in listOf(0.08f, 0.56f)) {
                    drawRoundRect(block, Offset(w * x, h * y), Size(w * 0.27f, h * 0.18f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()))
                }
            }
            for (x in listOf(0.16f, 0.38f, 0.76f)) {
                drawLine(road, Offset(w * x, 0f), Offset(w * x, h), 18.dp.toPx())
            }
            for (y in listOf(0.30f, 0.51f, 0.82f)) {
                drawLine(road, Offset(0f, h * y), Offset(w, h * y), 18.dp.toPx())
            }
            // Trees and a small square; all positions are diagram fractions.
            for (p in listOf(Offset(.59f, .13f), Offset(.61f, .70f), Offset(.89f, .64f))) {
                drawCircle(if (dark) Color(0xFF668B6D) else Color(0xFF9CC79C), 10.dp.toPx(), Offset(w * p.x, h * p.y))
            }
            if (progress > 0f) {
                val path = Path()
                ClinicDemoData.route.forEachIndexed { index, point ->
                    if (index == 0) path.moveTo(w * point.x, h * point.y) else path.lineTo(w * point.x, h * point.y)
                }
                drawPath(path, routeColor.copy(alpha = progress), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(routeColor, 8.dp.toPx(), Offset(w * ClinicDemoData.origin.x, h * ClinicDemoData.origin.y))
                drawCircle(Color.White, 3.dp.toPx(), Offset(w * ClinicDemoData.origin.x, h * ClinicDemoData.origin.y))
            }
        }
        Surface(Modifier.align(Alignment.TopEnd).padding(10.dp), shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = routeColor, modifier = Modifier.size(18.dp))
                Text("Clínica Jardim\nVeterinária", style = MaterialTheme.typography.labelSmall)
            }
        }
        Icon(Icons.Default.LocationOn, contentDescription = "Destino: Clínica Jardim Veterinária",
            tint = routeColor, modifier = Modifier
                .offset(x = maxWidth * ClinicDemoData.destination.x - 18.dp,
                    y = maxHeight * ClinicDemoData.destination.y - 30.dp).size(36.dp))
        if (routeVisible) {
            Surface(Modifier.align(Alignment.BottomStart).padding(8.dp),
                shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface) {
                Text("Partida fictícia", Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
