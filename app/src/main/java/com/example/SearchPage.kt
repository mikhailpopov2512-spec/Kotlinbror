package com.example

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchPage(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchQueryResults.collectAsState()
    val isLoading by viewModel.isSearchLoading.collectAsState()
    val browserMode by viewModel.browserMode.collectAsState()

    var activeTab by remember { mutableStateOf("Веб") } // "Веб", "Картинки", "Факты"

    // Glass style colors
    val glassBg = if (browserMode == BrowserMode.INCOGNITO) {
        Color(0xBA0F172A)
    } else if (browserMode == BrowserMode.STEALTH) {
        Color(0xE6050505)
    } else {
        Color(0xCBFFFFFF)
    }

    val glassBorder = if (browserMode == BrowserMode.STEALTH) {
        Color(0xFF00FF66).copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }

    val textPrimaryColor = if (browserMode == BrowserMode.STEALTH) {
        Color(0xFF00FF66)
    } else if (browserMode == BrowserMode.INCOGNITO) {
        Color.White
    } else {
        Color(0xFF1E293B)
    }

    val textSecondaryColor = if (browserMode == BrowserMode.STEALTH) {
        Color(0xFF00B344)
    } else if (browserMode == BrowserMode.INCOGNITO) {
        Color(0xFFCBD5E1)
    } else {
        Color(0xFF64748B)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (browserMode == BrowserMode.STEALTH) Color.Black
                else if (browserMode == BrowserMode.INCOGNITO) Color(0xFF0F172A)
                else Color(0xFFF1F5F9)
            )
            .statusBarsPadding()
    ) {
        // Search Header containing the RosPoisk Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад в табло",
                    tint = textPrimaryColor
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // RosPoisk branded logo: "Р" inside circular Russian flag
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.White, Color(0xFF1E88E5), Color(0xFFE53935))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Р",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "РосПоиск",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimaryColor,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Быстрая выдача • Без слежки",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textSecondaryColor
                    )
                )
            }
        }

        // Search Bar Info Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
                .background(glassBg, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Запрос",
                        tint = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "chrome-native://rossearx",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textSecondaryColor
                    )
                )
            }
        }

        // Result Sub-Navigation: Web / Images / Facts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Веб", "Картинки", "Факты").forEach { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            if (isSelected) (if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5)) else glassBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            if (isSelected) (if (browserMode == BrowserMode.STEALTH) Color(0x3300FF66) else Color(0xFF1E88E5)) else glassBg,
                            RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { activeTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected && browserMode != BrowserMode.STEALTH) Color.White else textPrimaryColor
                        )
                    )
                }
            }
        }

        // Inner Results listing
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("search_results_recycler"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Facts and quick answer Card if tab is "Факты" or on standard results
                if (activeTab == "Факты" || (activeTab == "Веб" && results.isNotEmpty())) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (browserMode == BrowserMode.STEALTH) Color(0xFF0F0F0F) else Color(0xFFFFFDF0)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Быстрый ответ",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Мнгновенный ответ РосПоиска",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD84315)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "«$query»",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF3E2723)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "По данным РосПоиска, искомый объект имеет гарантированно достоверное государственное и экспертное подтверждение. Вся заблокированная инфраструктура отфильтрована в соответствии ФЗ-149 РФ.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (browserMode == BrowserMode.STEALTH) Color(0xFF00B344) else Color(0xFF5D4037),
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }

                if (activeTab == "Картинки") {
                    // Grid-like layout of parsed parsed HTML visual cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                "Красные маки" to "https://images.unsplash.com/photo-1500627869374-13cd993b1115",
                                "Стеклянный блик" to "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe"
                            ).forEach { (title, url) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .shadow(3.dp, RoundedCornerShape(12.dp))
                                        .background(glassBg, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onNavigate("https://nature-russia.ru") }
                                ) {
                                    Column {
                                        // Mock rendering box since Coil needs network
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = title,
                                                tint = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(8.dp),
                                            color = textPrimaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Tabs Web results list
                    if (results.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Пусто",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Результаты временно недоступны",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = textPrimaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Пожалуйста, проверьте введённый запрос или подключение",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = textSecondaryColor
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    } else {
                        items(results) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(14.dp))
                                    .border(1.dp, glassBorder, RoundedCornerShape(14.dp))
                                    .clickable { onNavigate(item.url) },
                                colors = CardDefaults.cardColors(containerColor = glassBg),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = item.url,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66).copy(alpha = 0.7f) else Color(0xFF1B5E20)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1565C0)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.snippet,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = textSecondaryColor
                                        )
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
