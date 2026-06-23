package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminPasswordEntryPage(
    modifier: Modifier = Modifier,
    onVerify: (String) -> Boolean,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Aesthetic pairings matching the 'Summer Glass' vibe
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A).copy(alpha = 0.95f),
            Color(0xFF0B1329).copy(alpha = 0.98f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Upper background glow blobs (Coastal / Sunset Amber & Sky Blue)
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF43F5E).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        // Close/Cancel Floating Button
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .testTag("admin_pin_close_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Отмена",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Header Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(16.dp, CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security lock",
                    tint = Color(0xFFFFCC00),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "РОССЕТЬ БЕЗОПАСНОСТЬ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Вход в Админ-панель",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Введите секретный 4-значный ключ Госаппарата для доступа к 40 защищенным параметрам.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // PIN Indicator Bubbles
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                for (i in 1..4) {
                    val active = i <= pin.length
                    val indicatorColor = if (errorMessage != null) Color(0xFFEF4444) else if (active) Color(0xFF00FF66) else Color.White.copy(alpha = 0.22f)
                    val sizeScale = if (active) 16.dp else 12.dp
                    Box(
                        modifier = Modifier
                            .size(sizeScale)
                            .clip(CircleShape)
                            .background(indicatorColor)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }

            // Error Display with Slide Animation
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = errorMessage ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Summer Glass Numeric Keypad grid (3x4)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "delete")
                )

                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            if (key.isEmpty()) {
                                // Empty spacer
                                Spacer(modifier = Modifier.size(54.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .shadow(2.dp, CircleShape)
                                        .border(
                                            width = 1.dp,
                                            color = if (key == "delete") Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.45f),
                                            shape = CircleShape
                                        )
                                        .background(
                                            color = if (key == "delete") Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable {
                                            if (errorMessage != null) {
                                                errorMessage = null
                                            }

                                            if (key == "delete") {
                                                if (pin.isNotEmpty()) {
                                                    pin = pin.dropLast(1)
                                                }
                                            } else if (pin.length < 4) {
                                                pin += key
                                                
                                                // Trigger validation on 4 characters complete
                                                if (pin.length == 4) {
                                                    scope.launch {
                                                        delay(200) // slight visual pause before verify
                                                        val success = onVerify(pin)
                                                        if (!success) {
                                                            errorMessage = "Ошибка доступа: Ключ введен неверно!"
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("admin_keypad_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "delete") {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
