package com.example

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val browserViewModel: BrowserViewModel = viewModel()
                val currentMode by browserViewModel.browserMode.collectAsState()

                // Stealth mode protection: prohibit screenshots
                LaunchedEffect(currentMode) {
                    if (currentMode == BrowserMode.STEALTH) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                // Autostart with edges
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserMainScreen(viewModel = browserViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMainScreen(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Collect URL, Mode states
    val currentUrl by viewModel.currentUrl.collectAsState()
    val browserMode by viewModel.browserMode.collectAsState()
    val isWebViewLoading by viewModel.isWebViewLoading.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()

    // Detect device battery percent at launch to set low-charge optimization
    var deviceBatteryPercent by remember { mutableStateOf(100) }
    LaunchedEffect(Unit) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) {
            deviceBatteryPercent = (level * 100) / scale
        }
    }

    var manualPowerSaveToggle by remember { mutableStateOf(false) }
    val isPowerSaveActive = deviceBatteryPercent < 20 || manualPowerSaveToggle

    // Address Bar Focus and input states
    var isOmniboxFocused by remember { mutableStateOf(false) }
    var addressTextInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Floating UI Mock Dialogs
    var showQrDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showYandexLoginDialog by remember { mutableStateOf(false) }

    // Keep reference to actual WebView
    var activeWebView by remember { mutableStateOf<WebView?>(null) }

    // Intercept back actions in the browser to act like a real browser!
    val backAction: () -> Unit = {
        if (currentUrl.isNotBlank()) {
            if (currentUrl == "chrome-native://blocked" || currentUrl == "chrome-native://rossearx") {
                viewModel.setUrl("")
            } else if (activeWebView?.canGoBack() == true) {
                activeWebView?.goBack()
            } else {
                viewModel.setUrl("")
            }
        }
    }

    // Glass style colors
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

    val glassShadow = if (browserMode == BrowserMode.STEALTH) {
        Color(0x6600FF66)
    } else {
        Color.Black.copy(alpha = 0.12f)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("browser_main_scaffold")
    ) {
        // 1. Dynamic Live Summer Background (Or Incognito/Stealth matrix theme)
        SummerBackground(
            mode = browserMode,
            lowBatteryMode = isPowerSaveActive
        )

        // 2. Main content container (Top Bar + Main Body + Bottom Space)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding() // Protect the bottom gesture pill fully
        ) {
            // A. Centralized Top Controller Bar (Compact, translucent glass bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, glassBorder, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(glassBg, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Quick Mode Indicators
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                BrowserMode.REGULAR to Icons.Default.WbSunny,
                                BrowserMode.INCOGNITO to Icons.Default.ModeNight,
                                BrowserMode.GUEST to Icons.Default.Water,
                                BrowserMode.KIDS to Icons.Default.ChildCare,
                                BrowserMode.STEALTH to Icons.Default.VisibilityOff
                            ).forEach { (m, ic) ->
                                val active = browserMode == m
                                IconButton(
                                    onClick = {
                                        viewModel.changeMode(m)
                                        if (m == BrowserMode.REGULAR) {
                                            viewModel.updateSearchQuery("")
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (active) (if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66).copy(alpha = 0.35f) else Color(0x331E88E5)) else Color.Transparent,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = ic,
                                        contentDescription = m.name,
                                        tint = if (active) (if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5)) else textPrimaryColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Rightside: Battery Optimizer badge & Yandex sync login
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Low Battery indicator indicator
                            IconButton(
                                onClick = { manualPowerSaveToggle = !manualPowerSaveToggle },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPowerSaveActive) Icons.Default.BatteryAlert else Icons.Default.BatteryFull,
                                    contentDescription = "Энергосбережение",
                                    tint = if (isPowerSaveActive) Color(0xFFFF5252) else Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Yandex Quick login badge sync
                            Button(
                                onClick = { showYandexLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD54F).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFE53935)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Я",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE53935)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "Войти", fontSize = 10.sp, color = textPrimaryColor)
                                }
                            }
                        }
                    }

                    // Mode description tag
                    Text(
                        text = when (browserMode) {
                            BrowserMode.STEALTH -> "АКТИВЕН STEALTH • 100% приватность • Скриншоты запрещены"
                            BrowserMode.INCOGNITO -> "АКТИВЕН ИНКОГНИТО • История не пишется"
                            BrowserMode.KIDS -> "АКТИВЕН ДЕТСКИЙ • Только одобренные сайты • Долфины"
                            BrowserMode.GUEST -> "АКТИВЕН ГОСТЬ • Временный профиль • Смывается волной"
                            else -> "Летний РосБраузер • 120 Гц плавный движок Blink"
                        } + if (isPowerSaveActive) " [Энергосбережение 60 Гц]" else "",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else textSecondaryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            // B. Main screen display routing based on url state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    currentUrl.isEmpty() -> {
                        // Show beautiful Summer New Tab Page
                        NewTabPage(
                            viewModel = viewModel,
                            onNavigate = { target ->
                                addressTextInput = target
                                viewModel.setUrl(target)
                            }
                        )
                    }

                    currentUrl == "chrome-native://blocked" -> {
                        // RKN custom block page
                        BlockedPage(
                            viewModel = viewModel,
                            onBack = { viewModel.setUrl("") }
                        )
                    }

                    currentUrl == "chrome-native://rossearx" -> {
                        // Custom RosPoisk engine UI
                        SearchPage(
                            viewModel = viewModel,
                            onBack = { viewModel.setUrl("") },
                            onNavigate = { target ->
                                addressTextInput = target
                                viewModel.setUrl(target)
                            }
                        )
                    }

                    else -> {
                        // Real rendering of target website inside WebView
                        WebViewContainer(
                            url = currentUrl,
                            viewModel = viewModel,
                            onWebViewCreated = { activeWebView = it }
                        )
                    }
                }
            }
        }

        // 3. Embedded Bottom Omnibox (Floating Address Bar)
        // Implemented strictly as described in Section 3.2
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp) // Leave minor spacing from screen edge
        ) {
            // A. Focused suggestions flyup popup
            if (isOmniboxFocused) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .border(1.5.dp, glassBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassBg)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Подсказки РосПоиска & История",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textSecondaryColor
                            )
                        )

                        // 1. RosPoisk quick lookup item
                        if (addressTextInput.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        viewModel.setUrl(addressTextInput)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Искать «$addressTextInput» в РосПоиск",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = textPrimaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // 2. Pre-fetched simple local history links
                        val matchHistory = listOf(
                            "yandex.ru" to "Яндекс — Поиск и погода",
                            "gosuslugi.ru" to "Госуслуги РФ",
                            "rustore.ru" to "RuStore Каталог",
                            "wikipedia.org" to "Википедия свободная энциклопедия",
                            "nature-russia.ru" to "Цветущие маковые поля России"
                        )
                        matchHistory.forEach { (url, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        addressTextInput = url
                                        focusManager.clearFocus()
                                        viewModel.setUrl(url)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "История",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = textPrimaryColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = url,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = textSecondaryColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // B. Omnibox Bar
            // Focused bar implements smooth springs, floating flag glow border
            val focusedWeight by animateFloatAsState(
                targetValue = if (isOmniboxFocused) 1.0f else 0.92f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                label = "OmniboxSpringWidth"
            )

            // Animated Mic pulsation
            val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
            val micScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "MicPulse"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(focusedWeight)
                    .align(Alignment.CenterHorizontally)
                    .shadow(12.dp, RoundedCornerShape(20.dp), clip = false)
                    .drawBehind {
                        // Drawing Tricolor glowing shadow around centered focused address bar
                        if (isOmniboxFocused) {
                            val strokeW = 4.dp.toPx()
                            val tricolorBrush = Brush.horizontalGradient(
                                colors = listOf(Color.White, Color(0xFF1E88E5), Color(0xFFE53935))
                            )
                            drawRoundRect(
                                brush = tricolorBrush,
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    20.dp.toPx(),
                                    20.dp.toPx()
                                ),
                                style = Stroke(width = strokeW)
                            )
                        }
                    }
                    .border(
                        1.dp,
                        if (isOmniboxFocused) Color.Transparent else glassBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .background(glassBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return Back to NTP button inside URL browsing state
                if (currentUrl.isNotEmpty()) {
                    IconButton(
                        onClick = backAction,
                        modifier = Modifier.size(34.dp).testTag("omnibox_return_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Домой",
                            tint = textPrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Security Shield Lock Icon with MicroWaves (as requested)
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Micro waves drawings around shield
                    val lockWaves by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "LockWave"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val c = Offset(this.size.width / 2f, this.size.height / 2f)
                        if (isOmniboxFocused || currentUrl.startsWith("https://")) {
                            drawCircle(
                                color = Color(0xFF4CAF50).copy(alpha = 0.25f * (1f - lockWaves)),
                                radius = this.size.width * 0.45f * lockWaves,
                                center = c,
                                style = Stroke(width = 3f)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (currentUrl.startsWith("https://") || currentUrl.isEmpty()) Icons.Default.Lock else Icons.Default.NoEncryption,
                        contentDescription = "Безопасность",
                        tint = if (currentUrl.startsWith("https://") || currentUrl.isEmpty()) {
                            if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF4CAF50)
                        } else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Central address typing input field
                OutlinedTextField(
                    value = addressTextInput,
                    onValueChange = { addressTextInput = it },
                    placeholder = {
                        Text(
                            text = if (browserMode == BrowserMode.KIDS) "Поиск детских добрых сайтов..." else "Поиск в РосПоиске или адрес...",
                            color = textSecondaryColor.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            isOmniboxFocused = state.isFocused
                        }
                        .testTag("omnibox_input_field"),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = textPrimaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.setUrl(addressTextInput)
                        }
                    )
                )

                // Voice input microphone (Pulsates on trigger)
                IconButton(
                    onClick = { showVoiceDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            if (showVoiceDialog) {
                                scaleX = micScale
                                scaleY = micScale
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Голосовой РосПоиск",
                        tint = if (showVoiceDialog) Color.Red else textPrimaryColor
                    )
                }

                // QR Code scan scanner access code
                IconButton(
                    onClick = { showQrDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR-сканер",
                        tint = textPrimaryColor
                    )
                }
            }
        }
    }

    // Interactive Dialog 1: Fake Voice search matching product specifications
    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Голосовой РосПоиск")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Слушаю вас и шум прибоя... 🌊",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Pulsating concentric circles represent voice level
                    CircularProgressIndicator(
                        color = Color.Red,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Произнесите например: «Красные маковые поля в Сочи»",
                        fontSize = 12.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    addressTextInput = "Красные маковие поля"
                    showVoiceDialog = false
                    viewModel.setUrl("Красные маковие поля")
                }) {
                    Text("Имитировать: «Маковые поля»")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Interactive Dialog 2: QR Scanner code mimicking
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Сканирование QR-кода") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(2.dp, Color.Green, RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR",
                            modifier = Modifier.size(110.dp),
                            tint = textPrimaryColor
                        )
                        // Moving scan bar lines animation
                        val infiniteScan = rememberInfiniteTransition(label = "ScanBar")
                        val scanMove by infiniteScan.animateFloat(
                            initialValue = -80f,
                            targetValue = 80f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "LaserMover"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = scanMove.dp)
                                .background(Color.Green)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Наведите камеру судна на QR-код для расшифровки ссылки.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = textSecondaryColor
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    addressTextInput = "gosuslugi.ru"
                    showQrDialog = false
                    viewModel.setUrl("gosuslugi.ru")
                }) {
                    Text("Имитировать QR: gosuslugi.ru")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Interactive Dialog 3: Yandex ID sync mock
    if (showYandexLoginDialog) {
        var yaUsername by remember { mutableStateOf("") }
        var yaPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showYandexLoginDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Yandex ID",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Синхронизация профиля")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Авторизуйте Яндекс ID для импорта в РосБраузер ваших паролей, закладок, истории и летних настроек.",
                        fontSize = 12.sp,
                        color = textSecondaryColor
                    )
                    OutlinedTextField(
                        value = yaUsername,
                        onValueChange = { yaUsername = it },
                        label = { Text("Логин или почта") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = yaPassword,
                        onValueChange = { yaPassword = it },
                        label = { Text("Пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showYandexLoginDialog = false
                        viewModel.resetShortcuts() // reloads services
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Войти и синхронизировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showYandexLoginDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
