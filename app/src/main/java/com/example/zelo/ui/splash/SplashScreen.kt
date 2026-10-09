package com.example.zelo.ui.splash

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun ZeloSplashScreen() {

    val context = LocalContext.current

    val icone = remember {
        context.packageManager
            .getApplicationIcon(context.packageName)
            .toBitmap(
                width = 256,
                height = 256
            )
            .asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102A43)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Zelo",
            fontSize = 56.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            letterSpacing = 3.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Image(
            bitmap = icone,
            contentDescription = "Ícone do Zelo",
            modifier = Modifier.size(112.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Cuidar é amar.",
            fontSize = 14.sp,
            letterSpacing = 2.sp,
            color = Color(0xFFB8CCDB)
        )
    }
}