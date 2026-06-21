package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTabPage(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onOpenSecuritySettings: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
    val shortcuts by viewModel.shortcuts.collectAsState()
    val weatherTemp by viewModel.weatherTemp.collectAsState()
    val weatherCondition by viewModel.weatherCondition.collectAsState()
    val trafficScore by viewModel.trafficScore.collectAsState()
    val isCoinSpinning by viewModel.isCoinSpinning.collectAsState()
    val dzenFeed by viewModel.dzenFeed.collectAsState()
    val browserMode by viewModel.browserMode.collectAsState()
    
    // Connected security and blocked list counters
    val blockedCount by viewModel.blockedDomainsCount.collectAsState()
    val securityStatus by viewModel.connectionSecurityStatus.collectAsState()
    val filterLevel by viewModel.filterLevel.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }

    // Dialog state for feedback
    var showDialogFeedback by remember { mutableStateOf<String?>(null) }

    // Glass style colors based on browserMode
    val glassBg = if (browserMode == BrowserMode.INCOGNITO) {
        Color(0xBA0F172A)
    } else if (browserMode == BrowserMode.STEALTH) {
        Color(0xE6050505)
    } else {
        Color(0xA6FFFFFF) // #99FFFFFF with micro adjust
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

    // Pull to Refresh sunrise animation simulation
    var isRefreshing by remember { mutableStateOf(false) }
    var sunriseAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            // Animate spin sunrise
            for (i in 0..15) {
                delay(40)
                sunriseAngle += 24f
            }
            viewModel.reloadDzenFeed()
            isRefreshing = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Main New Tab Scrollable list (Widgets, Pebbles, Dzen)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .testTag("new_tab_scrollable_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Флаг",
                            tint = Color(0xFFE53935),
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(2.dp, CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.White, Color(0xFF1E88E5), Color(0xFFE53935))
                                    ),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (browserMode) {
                                BrowserMode.INCOGNITO -> "РосБраузер • Инкогнито"
                                BrowserMode.STEALTH -> "С Т Е Л С"
                                BrowserMode.KIDS -> "Детский РосБраузер 🐬"
                                BrowserMode.GUEST -> "Временный Гость 🌊"
                                else -> "РосБраузер"
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor,
                                letterSpacing = if (browserMode == BrowserMode.STEALTH) 4.sp else 0.5.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = when (browserMode) {
                            BrowserMode.INCOGNITO -> "Ночной уединённый пляж"
                            BrowserMode.STEALTH -> "Режим максимальной скрытности"
                            BrowserMode.KIDS -> "Безопасный детский уголок с дельфинами"
                            BrowserMode.GUEST -> "Все сеансы смываются волной"
                            else -> "Стабильный импортозамещённый интернет"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textSecondaryColor
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Interactive Modules (Widgets: Weather, Traffic, Currency)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Weather Buoy Widget (Takes 1.0f weight)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(135.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
                            .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                            .background(glassBg, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        WeatherBuoyWidget(
                            temp = weatherTemp,
                            condition = weatherCondition,
                            textPrimaryColor = textPrimaryColor,
                            textSecondaryColor = textSecondaryColor,
                            onClick = { viewModel.incrementWeather() }
                        )
                    }

                    // Traffic Palm Widget (Takes 1.0f weight)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(135.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
                            .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                            .background(glassBg, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        TrafficPalmWidget(
                            score = trafficScore,
                            textPrimaryColor = textPrimaryColor,
                            textSecondaryColor = textSecondaryColor,
                            onLightHit = { viewModel.hitTrafficLight() }
                        )
                    }
                }
            }

            // Currency Pirate Coin Widget
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
                        .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                        .background(glassBg, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    CurrencyCoinWidget(
                        viewModel = viewModel,
                        isCoinSpinning = isCoinSpinning,
                        textPrimaryColor = textPrimaryColor,
                        textSecondaryColor = textSecondaryColor,
                        onClickCoin = {
                            scope.launch {
                                viewModel.spinCoin()
                                delay(1200)
                                viewModel.stopCoinSpinning()
                            }
                        }
                    )
                }
            }

            // Security Lock & AdBlock Counter Widget
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
                        .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                        .background(glassBg, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    SecurityAdBlockWidget(
                        viewModel = viewModel,
                        textPrimaryColor = textPrimaryColor,
                        textSecondaryColor = textSecondaryColor,
                        onClick = {
                            showDialogFeedback = "Защита РосБраузера активна! Уровень фильтрации Роскомнадзора настроен на режим: $filterLevel. Обнаружено и обезврежено рекламных банеров и вредоносных угроз: $blockedCount."
                            onOpenSecuritySettings()
                        }
                    )
                }
            }

            // Табло: Sea Pebbles Quick Access Grid
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Табло (Быстрый доступ)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                        )

                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Добавить плитку",
                                tint = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5)
                            )
                        }
                    }

                    // Custom 4-Column Grid layout using basic Rows
                    val chunkedList = shortcuts.chunked(4)
                    for (rowItems in chunkedList) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (shortcut in rowItems) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SeaPebbleShortcutTile(
                                        shortcut = shortcut,
                                        onNavigate = onNavigate,
                                        onDelete = { viewModel.deleteShortcut(shortcut.id, context) }
                                    )
                                }
                            }
                            // Fill remaining space if row is not full
                            val emptySpaces = 4 - rowItems.size
                            for (e in 0 until emptySpaces) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (shortcuts.isEmpty()) {
                        Text(
                            text = "Плитки табло пусты. Тапните '+', чтобы добавить сайт.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSecondaryColor,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }

            // Dzen Infinite Feed
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Лента • Дзен",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                    )

                    IconButton(
                        onClick = { isRefreshing = true },
                        modifier = Modifier.size(28.dp),
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить ленту",
                            tint = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5),
                            modifier = Modifier.alpha(if (isRefreshing) 0.4f else 1.0f)
                        )
                    }
                }

                if (isRefreshing) {
                    // Pull to Refresh Banner mimicking sun rise with Flag Colors
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Солнце",
                                tint = Color(0xFFFFEB3B),
                                modifier = Modifier
                                    .size(36.dp)
                                    .graphicsLayer(rotationZ = sunriseAngle)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Восходящее солнце РосЗагрузки...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            val visibleFeed = dzenFeed.filter { it.dismissState == "visible" }
            if (visibleFeed.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Newspaper,
                            contentDescription = "Обновлено",
                            tint = textSecondaryColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Вы очистили всю ленту на сегодня!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = textPrimaryColor),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Дзен восстановится при обновлении (тап по стрелке)",
                            style = MaterialTheme.typography.bodySmall.copy(color = textSecondaryColor),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Button(
                            onClick = { viewModel.reloadDzenFeed() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5),
                                contentColor = if (browserMode == BrowserMode.STEALTH) Color.Black else Color.White
                            ),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Text("Вернуть статьи")
                        }
                    }
                }
            } else {
                items(visibleFeed, key = { it.id }) { article ->
                    DzenParchmentCard(
                        article = article,
                        textPrimary = textPrimaryColor,
                        textSecondary = textSecondaryColor,
                        onSwipeLeft = { viewModel.deferArticleLater(article.id) },
                        onSwipeRight = { viewModel.dismissArticleSeagull(article.id) }
                    )
                }
            }

            // Decorative spacer
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    // Site creation Pop—Up Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Добавить плитку табло") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Имя сайта") },
                        modifier = Modifier.fillMaxWidth().testTag("add_shortcut_title_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("URL (например: site.ru)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_shortcut_url_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                            var target = newUrl
                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                target = "https://$target"
                            }
                            viewModel.addShortcut(newTitle.trim(), target.trim(), context)
                            newTitle = ""
                            newUrl = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("add_shortcut_confirm_button")
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// 1. Weather Widget featuring custom drawing + sway physics animations
@Composable
fun WeatherBuoyWidget(
    temp: Int,
    condition: String,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    onClick: () -> Unit
) {
    // Sway rotation loop for the buoy
    val infiniteTransition = rememberInfiniteTransition(label = "BuoyLoop")
    val buoyRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BuoySway"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Погода в Сочи",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textSecondaryColor
            )
        )

        // Draw animated Lifesaver Buoy (Спасательный Круг)
        Box(
            modifier = Modifier
                .size(54.dp)
                .graphicsLayer(rotationZ = buoyRotation),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val outerRadius = size.minDimension / 2f
                val innerRadius = outerRadius * 0.45f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // Draw water curves below
                drawArc(
                    color = Color(0xFF03A9F4).copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(0f, size.height * 0.45f),
                    size = Size(size.width, size.height * 0.55f),
                    style = Stroke(width = 4f)
                )

                // Base white buoy ring
                drawCircle(color = Color.White, radius = outerRadius, center = centerOffset)

                // Red stripes on the buoy (4 segments)
                for (i in 0 until 4) {
                    rotate(45f + i * 90f, pivot = centerOffset) {
                        drawArc(
                            color = Color(0xFFD32F2F),
                            startAngle = -20f,
                            sweepAngle = 40f,
                            useCenter = true,
                            topLeft = Offset(centerOffset.x - outerRadius, centerOffset.y - outerRadius),
                            size = Size(outerRadius * 2, outerRadius * 2)
                        )
                    }
                }

                // Buoy inner hole cutout (painted with clear or blue transparent color)
                drawCircle(color = Color(0xFF87CEEB).copy(alpha = 0.4f), radius = innerRadius, center = centerOffset)
                // Buoy inner border
                drawCircle(color = Color(0xFFD32F2F), radius = innerRadius, center = centerOffset, style = Stroke(width = 2f))
                // Buoy outer border
                drawCircle(color = Color(0xFFD32F2F), radius = outerRadius, center = centerOffset, style = Stroke(width = 2.5f))

                // Inner central icon dot mimicking sun
                if (condition == "Солнечно") {
                    drawCircle(color = Color(0xFFFFC107), radius = innerRadius * 0.5f, center = centerOffset)
                } else if (condition == "Облачно") {
                    drawCircle(color = Color(0xFFCBD5E1), radius = innerRadius * 0.45f, center = centerOffset)
                } else {
                    // Rain droplets
                    drawCircle(color = Color(0xFF2196F3), radius = innerRadius * 0.25f, center = Offset(centerOffset.x - 3f, centerOffset.y))
                    drawCircle(color = Color(0xFF2196F3), radius = innerRadius * 0.25f, center = Offset(centerOffset.x + 3f, centerOffset.y + 4f))
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${temp}°C",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = condition,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textSecondaryColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 2. Traffic Widget with Palm tree and interactive hanging/falling coconut
@Composable
fun TrafficPalmWidget(
    score: Int,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    onLightHit: () -> Unit
) {
    // Falling coconut animation state triggers when score > 7
    val shouldFall = score > 7
    val infiniteTransition = rememberInfiniteTransition(label = "PalmLoop")

    // Gentle palm leaves sway rotation
    val leafSway by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LeafSway"
    )

    // Interpolation for coconut fall offset
    val coconutYOffset by animateFloatAsState(
        targetValue = if (shouldFall) 65f else 0f,
        animationSpec = if (shouldFall) {
            spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
        } else {
            snap()
        },
        label = "CoconutFall"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onLightHit() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Пробки на море",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textSecondaryColor
            )
        )

        // Palm tree with traffic lights & falling coconut drawn manually in Box
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(65.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val ox = size.width * 0.4f
                val oy = size.height

                // Draw curvy brown palm trunk
                val trunkPath = Path().apply {
                    moveTo(ox - 6f, oy)
                    quadraticTo(ox - 3f, oy - 25f, ox + 8f, oy - 45f)
                    lineTo(ox + 16f, oy - 43f)
                    quadraticTo(ox + 5f, oy - 23f, ox + 4f, oy)
                    close()
                }
                drawPath(trunkPath, Color(0xFF8D6E63))

                // Draw swaying green Palm leaves
                rotate(leafSway, pivot = Offset(ox + 12f, oy - 45f)) {
                    val px = ox + 12f
                    val py = oy - 45f
                    // Leaf Left
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = 140f,
                        sweepAngle = 90f,
                        useCenter = true,
                        topLeft = Offset(px - 35f, py - 15f),
                        size = Size(40f, 25f)
                    )
                    // Leaf Right
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -50f,
                        sweepAngle = 90f,
                        useCenter = true,
                        topLeft = Offset(px - 5f, py - 12f),
                        size = Size(40f, 25f)
                    )
                    // Leaf Top
                    drawArc(
                        color = Color(0xFF43A047),
                        startAngle = 210f,
                        sweepAngle = 120f,
                        useCenter = true,
                        topLeft = Offset(px - 15f, py - 30f),
                        size = Size(30f, 35f)
                    )
                }

                // Draw светофор (traffic lights card) on secondary side
                val tx = size.width * 0.75f
                val ty = size.height * 0.15f
                val tw = 16f
                val th = 42f
                // Backplate
                drawRoundRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(tx, ty),
                    size = Size(tw, th),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Red segment
                drawCircle(
                    color = if (score >= 8) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.25f),
                    radius = 4.5f,
                    center = Offset(tx + tw/2f, ty + 7f)
                )
                // Yellow segment
                drawCircle(
                    color = if (score in 5..7) Color(0xFFFBBF24) else Color(0xFFFBBF24).copy(alpha = 0.25f),
                    radius = 4.5f,
                    center = Offset(tx + tw/2f, ty + th/2f)
                )
                // Green segment
                drawCircle(
                    color = if (score <= 4) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.25f),
                    radius = 4.5f,
                    center = Offset(tx + tw/2f, ty + th - 7f)
                )

                // COCONUT displaying traffic rating
                // It hangs near leaves initially, falling down upon score > 7
                val cx = ox + 15f
                val cy = oy - 40f + coconutYOffset
                drawCircle(
                    color = Color(0xFF5D4037),
                    radius = 9f,
                    center = Offset(cx, cy)
                )
                // 3 tiny black spots to represent actual coconut shell pores
                drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = 1.2f, center = Offset(cx - 3f, cy - 2f))
                drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = 1.2f, center = Offset(cx + 1f, cy - 3f))
                drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = 1.2f, center = Offset(cx - 1f, cy + 1f))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$score баллов",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor,
                    fontSize = 15.sp
                )
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (shouldFall) "(Бум! 🥥)" else "(Пальма)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (shouldFall) Color(0xFFFF5252) else textSecondaryColor,
                    fontWeight = if (shouldFall) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

// 3. Currency Pirate Coin Widget
@Composable
fun CurrencyCoinWidget(
    viewModel: BrowserViewModel,
    isCoinSpinning: Boolean,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    onClickCoin: () -> Unit
) {
    val usd by viewModel.rubToUsd.collectAsState()
    val eur by viewModel.rubToEur.collectAsState()
    val cny by viewModel.rubToCny.collectAsState()

    // Real-time coin spin rotation animation
    val coinSpinAngle by animateFloatAsState(
        targetValue = if (isCoinSpinning) 1080f else 0f,
        animationSpec = if (isCoinSpinning) {
            tween(1200, easing = EaseOutCubic)
        } else {
            snap()
        },
        label = "PirateCoinSpin"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Pirate Coin Spinner Column
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onClickCoin() }
                .padding(end = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer(rotationY = coinSpinAngle),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Draw golden metal gradient coin
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF176), Color(0xFFF57F17)),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )

                    // Embossed border ridges
                    drawCircle(color = Color(0xFFFFD54F), radius = r - 4f, center = center, style = Stroke(width = 2.5f))
                    drawCircle(color = Color(0xFFE65100), radius = r - 0.5f, center = center, style = Stroke(width = 2f))

                    // Draw anchor inside coin representing pirate sum
                    val path = Path().apply {
                        moveTo(center.x, center.y - r * 0.45f)
                        lineTo(center.x, center.y + r * 0.4f)
                        // central ring
                        addOval(androidx.compose.ui.geometry.Rect(center.x - 4f, center.y - r * 0.45f, center.x + 4f, center.y - r * 0.2f))
                        // crescent base
                        moveTo(center.x - r * 0.4f, center.y + r * 0.1f)
                        quadraticTo(center.x, center.y + r * 0.55f, center.x + r * 0.4f, center.y + r * 0.1f)
                    }
                    drawPath(path, Color(0xFFE65100), style = Stroke(width = 3.5f))

                    // Anchor arrows
                    drawCircle(color = Color(0xFFE65100), radius = 3f, center = Offset(center.x - r * 0.4f, center.y + r * 0.1f))
                    drawCircle(color = Color(0xFFE65100), radius = 3f, center = Offset(center.x + r * 0.4f, center.y + r * 0.1f))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Ио-хо-хо курс",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textSecondaryColor
                    )
                )
                Text(
                    text = if (isCoinSpinning) "Круть! 💰" else "ЦБ РФ монетка",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textPrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // USD, EUR, CNY vertical/horizontal panel with sparklines
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CurrencyTile(label = "$", value = String.format("%.2f", usd), color = textPrimaryColor, textSecondaryColor)
            CurrencyTile(label = "€", value = String.format("%.2f", eur), color = textPrimaryColor, textSecondaryColor)
            CurrencyTile(label = "¥", value = String.format("%.2f", cny), color = textPrimaryColor, textSecondaryColor)
        }
    }
}

@Composable
fun CurrencyTile(
    label: String,
    value: String,
    color: Color,
    secondaryColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (color == Color(0xFF00FF66)) Color(0xFF00FF66) else Color(0xFFE65100)
                )
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
        // Micro sparkline drawn in Canvas (mock trend path)
        Canvas(modifier = Modifier.size(45.dp, 12.dp)) {
            val sparkPath = Path().apply {
                moveTo(0f, size.height * 0.5f)
                val segment = size.width / 4f
                lineTo(segment, size.height * (0.2f + 0.5f * sin(PI.toFloat() * 0.3f)))
                lineTo(segment * 2, size.height * (0.8f + 0.1f * sin(PI.toFloat() * 1.5f)))
                lineTo(segment * 3, size.height * (0.1f + 0.3f * sin(PI.toFloat() * 0.7f)))
                lineTo(size.width, size.height * 0.3f)
            }
            drawPath(
                path = sparkPath,
                color = if (color == Color(0xFF00FF66)) Color(0xFF00FF66) else Color(0xFF4CAF50),
                style = Stroke(width = 3.5f)
            )
        }
    }
}

// 4. Sea Pebble Shortcut Tile
@Composable
fun SeaPebbleShortcutTile(
    shortcut: PebbleShortcut,
    onNavigate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Click scale animations matching actual prompt guidelines
    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "PebbleScale"
    )

    val elevationVal by animateFloatAsState(
        targetValue = if (isPressed) 2f else 8f,
        label = "PebbleElevation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(key1 = shortcut) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } catch (e: Exception) {
                            // Cancelled
                        }
                        isPressed = false
                    },
                    onTap = {
                        onNavigate(shortcut.url)
                    },
                    onLongPress = {
                        // Triggers deletion
                        onDelete()
                    }
                )
            }
            .graphicsLayer(
                scaleX = scaleFactor,
                scaleY = scaleFactor
            )
            .shadow(elevationVal.dp, RoundedCornerShape(20.dp), clip = false)
            .border(
                1.dp,
                if (shortcut.isYandexService) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            // Sand color brush beach background
            .background(
                Brush.verticalGradient(
                    colors = if (shortcut.isYandexService) {
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
                    } else {
                        listOf(Color(0xFFF5EDD6), Color(0xFFE5D5B8))
                    }
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 14.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.35f), CircleShape)
                    .border(1f.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = when {
                    shortcut.url.contains("yandex.ru") -> R.drawable.img_shortcut_yandex_1782028118694
                    shortcut.url.contains("gosuslugi.ru") -> R.drawable.img_gosuslugi_pebble_1781961477713
                    shortcut.url.contains("vk.com") -> R.drawable.img_vk_pebble_1781961462500
                    shortcut.url.contains("rustore.ru") || shortcut.url.contains("market.yandex.ru") -> R.drawable.img_sber_pebble_1781961492341
                    else -> null
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                val painter = androidx.compose.runtime.remember(imageRes) {
                    if (imageRes != null) {
                        try {
                            val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, imageRes)
                            if (bitmap != null) {
                                androidx.compose.ui.graphics.painter.BitmapPainter(bitmap.asImageBitmap())
                            } else {
                                null
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            null
                        }
                    } else {
                        null
                    }
                }

                if (painter != null) {
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = shortcut.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else if (shortcut.isYandexService) {
                    // Quick red Yandex letter branding
                    Text(
                        text = "Я",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE53935)
                        )
                    )
                } else {
                    Icon(
                        imageVector = when {
                            shortcut.url.contains("gost") -> Icons.Default.Shield
                            shortcut.url.contains("gosuslugi") -> Icons.Default.AccountBalance
                            shortcut.url.contains("rustore") -> Icons.Default.SystemUpdate
                            shortcut.url.contains("vk") -> Icons.Default.Group
                            shortcut.url.contains("wiki") -> Icons.Default.MenuBook
                            else -> Icons.Default.Language
                        },
                        contentDescription = shortcut.title,
                        tint = Color(0xFF5D4037)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = shortcut.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E342E)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

// 5. Dzen Parchment Card with curved deckle edges details and swipe reactions
@Composable
fun DzenParchmentCard(
    article: DzenArticle,
    textPrimary: Color,
    textSecondary: Color,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    val screenSwipeAnim by animateFloatAsState(
        targetValue = offsetX,
        label = "DzenSwipe"
    )

    // Parchment paper design colors
    val parchmentBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFDF0), Color(0xFFFAF2DE))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer(translationX = screenSwipeAnim)
            .pointerInput(key1 = article.id) {
                detectDragGestures(
                    onDragStart = { offsetX = 0f },
                    onDragEnd = {
                        if (offsetX > 250f) {
                            // Dismiss Seagull fly away
                            onSwipeRight()
                        } else if (offsetX < -250f) {
                            // Defer later
                            onSwipeLeft()
                        } else {
                            offsetX = 0f
                        }
                    },
                    onDragCancel = {
                        offsetX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                    }
                )
            }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(0.5.dp, Color(0xFFD4C4A8), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(parchmentBg, RoundedCornerShape(12.dp))
                .drawWithContent {
                    drawContent()
                    // Draw a deckle folded edge (загнутый край) on the bottom right corner
                    val pathDeckle = Path().apply {
                        moveTo(size.width - 24f, size.height)
                        lineTo(size.width, size.height - 24f)
                        lineTo(size.width - 24f, size.height - 24f)
                        close()
                    }
                    drawPath(pathDeckle, Color(0xFFEFE5CD))
                    // fold crease line
                    drawLine(
                        color = Color(0xFFD3C5A1),
                        start = Offset(size.width - 24f, size.height),
                        end = Offset(size.width, size.height - 24f),
                        strokeWidth = 2.5f
                    )
                }
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дзен • " + article.category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315)
                        )
                    )

                    Row {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Скрыть",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onSwipeRight() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Позже",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onSwipeLeft() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF5D4037)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action hint row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Смахните вправо для скрытия (чайка) ➜",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF8D6E63)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityAdBlockWidget(
    viewModel: BrowserViewModel,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    onClick: () -> Unit
) {
    val blockedCount by viewModel.blockedDomainsCount.collectAsState()
    val securityStatus by viewModel.connectionSecurityStatus.collectAsState()
    val filterLevel by viewModel.filterLevel.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing Green glowing dot
                val infiniteDot = rememberInfiniteTransition(label = "PulseDot")
                val dotAlpha by infiniteDot.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1250, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseDotAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = dotAlpha), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = securityStatus,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = textPrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Фильтрация Роскомнадзора: $filterLevel",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textSecondaryColor,
                    fontSize = 11.sp
                )
            )
            Text(
                text = "Все шлюзы защищены ГОСТ TLS 256-бит",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textSecondaryColor.copy(alpha = 0.73f),
                    fontSize = 10.sp
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(start = 8.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$blockedCount",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = textPrimaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            )
            Text(
                text = "угроз блок.",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textSecondaryColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
