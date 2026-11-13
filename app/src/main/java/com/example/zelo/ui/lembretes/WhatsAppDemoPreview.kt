package com.example.zelo.ui.lembretes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.WhatsAppDemoViewModel

@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun WhatsAppDemoPreview() {
    ZeloTheme {
        val model = remember { WhatsAppDemoViewModel() }
        WhatsAppDemoScreen(model, onBack = {})
    }
}
