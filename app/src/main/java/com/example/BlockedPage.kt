package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlockedPage(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Serious dark background
    ) {
        // Draw real-time background: shattered dark flag
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val block = h * 0.12f
            val startY = h * 0.35f

            // Slightly greyed out flag
            drawRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(0f, startY),
                size = androidx.compose.ui.geometry.Size(w, block)
            )
            drawRect(
                color = Color(0xFF1E88E5).copy(alpha = 0.18f),
                topLeft = Offset(0f, startY + block),
                size = androidx.compose.ui.geometry.Size(w, block)
            )
            drawRect(
                color = Color(0xFFE53935).copy(alpha = 0.18f),
                topLeft = Offset(0f, startY + block * 2),
                size = androidx.compose.ui.geometry.Size(w, block)
            )

            // Draw dramatic "fracture/crack" lines (representing a broken barrier)
            val crackColor = Color(0xFFEF4444).copy(alpha = 0.5f)
            val crackWeight = 5f

            // Primary Crack Starburst in center
            val cx = w * 0.5f
            val cy = startY + block

            val crackPath = Path().apply {
                // Ray 1
                moveTo(cx, cy)
                lineTo(cx - 150f, cy - 120f)
                lineTo(cx - 300f, cy - 80f)
                lineTo(cx - 420f, cy - 220f)

                // Ray 2
                moveTo(cx, cy)
                lineTo(cx + 180f, cy - 70f)
                lineTo(cx + 340f, cy + 50f)
                lineTo(w, cy + 20f)

                // Ray 3
                moveTo(cx, cy)
                lineTo(cx - 50f, cy + 200f)
                lineTo(cx - 180f, cy + 350f)

                // Ray 4
                moveTo(cx, cy)
                lineTo(cx + 110f, cy + 180f)
                lineTo(cx + 280f, cy + 400f)
            }

            drawPath(
                path = crackPath,
                color = crackColor,
                style = Stroke(width = crackWeight)
            )

            // Central cracked concentric impact rings
            drawCircle(color = crackColor, radius = 50f, center = Offset(cx, cy), style = Stroke(width = 3f))
            drawCircle(color = crackColor, radius = 110f, center = Offset(cx, cy), style = Stroke(width = 2f))
        }

        // Overlay Serious Warning Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Закон",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier
                            .size(72.dp)
                            .padding(bottom = 12.dp)
                    )

                    Text(
                        text = "ДОСТУП ОГРАНИЧЕН",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Информационный ресурс заблокирован на территории Российской Федерации на основании Федерального закона от 27 июля 2006 г. № 149-ФЗ «Об информации, информационных технологиях и о защите информации».",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF94A3B8),
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // RKN Badge detail
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Реестр",
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Проверил: РосРеестр-РКН-Блокатор",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("blocked_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Вернуться на летнюю поляну",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
