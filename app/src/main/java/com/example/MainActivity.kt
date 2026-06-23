package com.example

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.widget.Toast

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

    // Collected Advanced Custom Settings Configurations
    val isSummerBgAnimEnabled by viewModel.isSummerBgAnimEnabled.collectAsState()
    val flagWaveSpeed by viewModel.flagWaveSpeed.collectAsState()
    val searchEnginePreset by viewModel.searchEnginePreset.collectAsState()
    val fontSizeScale by viewModel.fontSizeScale.collectAsState()
    val isHapticVibeEnabled by viewModel.isHapticVibeEnabled.collectAsState()

    // Detect device battery percent at launch to set low-charge optimization
    var deviceBatteryPercent by remember { mutableStateOf(100) }
    LaunchedEffect(Unit) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = try {
            context.registerReceiver(null, intentFilter)
        } catch (e: Throwable) {
            null
        }
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

    // Advanced settings states and modals
    var showAdvancedSettingsDialog by remember { mutableStateOf(false) }
    var activeSettingsTab by remember { mutableStateOf(0) }
    var showBiometricUnlockDialog by remember { mutableStateOf(false) }
    var showSavedPasswordsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyTextDialog by remember { mutableStateOf(false) }
    var hasAcceptedPrivacyPolicy by remember { mutableStateOf(false) }
    var userLoginPinCode by remember { mutableStateOf("1234") } // Default starting PIN
    var showAppLoginLockScreen by remember { mutableStateOf(true) } // PIN lock at boot
    var enteredPinCode by remember { mutableStateOf("") }
    var isNotificationPermissionGranted by remember { mutableStateOf(false) }
    var showSecuritySettingsDialog by remember { mutableStateOf(false) }
    var showProfileManagerDialog by remember { mutableStateOf(false) }
    var showDonateAndPromoDialog by remember { mutableStateOf(false) }

    // Initialize profile persistence at launch
    LaunchedEffect(Unit) {
        viewModel.initPersistence(context)
    }

    // Loop for FSB custom compliance periodic notification
    LaunchedEffect(isNotificationPermissionGranted) {
        if (isNotificationPermissionGranted) {
            // First notification immediately
            sendFSBNotification(context)
            while (true) {
                delay(3600000) // hourly loop
                sendFSBNotification(context)
            }
        }
    }

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

    BackHandler(enabled = currentUrl.isNotBlank()) {
        backAction()
    }

    // Glass style colors with exquisite summer gradients (turquoise, sand, and sunny)
    val glassBg = if (browserMode == BrowserMode.INCOGNITO) {
        Color(0xBA0F172A)
    } else if (browserMode == BrowserMode.STEALTH) {
        Color(0xE6050505)
    } else {
        Color(0xACCCFBF1) // Crystal summer turquoise translucent base
    }

    val glassBorder = if (browserMode == BrowserMode.STEALTH) {
        Color(0xFF00FF66).copy(alpha = 0.5f)
    } else if (browserMode == BrowserMode.INCOGNITO) {
        Color.White.copy(alpha = 0.3f)
    } else {
        Color(0xFFFDE047).copy(alpha = 0.85f) // Glowing sunny-gold highlights
    }

    val glassShadow = if (browserMode == BrowserMode.STEALTH) {
        Color(0x6600FF66)
    } else if (browserMode == BrowserMode.INCOGNITO) {
        Color.Black.copy(alpha = 0.35f)
    } else {
        Color(0x36D97706) // Deep sand-amber shadow glow for glass depth
    }

    val textPrimaryColor = if (browserMode == BrowserMode.STEALTH) {
        Color(0xFF00FF66)
    } else if (browserMode == BrowserMode.INCOGNITO) {
        Color.White
    } else {
        Color(0xFF0F766E) // Rich, readable deep teal
    }

    val textSecondaryColor = if (browserMode == BrowserMode.STEALTH) {
        Color(0xFF00B344)
    } else if (browserMode == BrowserMode.INCOGNITO) {
        Color(0xFFCBD5E1)
    } else {
        Color(0xFF0D9488) // Mid-tone summer teal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("browser_main_scaffold")
    ) {
        // 1. Dynamic Live Summer Background (Or Incognito/Stealth matrix theme)
        SummerBackground(
            mode = browserMode,
            lowBatteryMode = isPowerSaveActive,
            isAnimEnabled = isSummerBgAnimEnabled,
            flagSpeedMultiplier = flagWaveSpeed
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
                        // Left Side: Active Mode Pill Capsule Button that triggers Settings!
                        val (modeText, modeIcon) = when (browserMode) {
                            BrowserMode.REGULAR -> "☀️ Поляна" to Icons.Default.WbSunny
                            BrowserMode.INCOGNITO -> "🌙 Пляж" to Icons.Default.ModeNight
                            BrowserMode.GUEST -> "🌊 Волна" to Icons.Default.Water
                            BrowserMode.KIDS -> "🐬 Дельфины" to Icons.Default.ChildCare
                            BrowserMode.STEALTH -> "👁 Скрытно" to Icons.Default.VisibilityOff
                        }
                        
                        Button(
                            onClick = { 
                                activeSettingsTab = 0 // theme selector tab
                                showAdvancedSettingsDialog = true 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66).copy(alpha = 0.2f) else Color(0x221E88E5),
                                contentColor = textPrimaryColor
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .border(1.dp, if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0x331E88E5), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = modeText,
                                    tint = if (browserMode == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = modeText,
                                    color = textPrimaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Rightside: Profiles, Battery, Yandex ID Sync
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Quick Profile Selector Badge
                            val currentProfile by viewModel.currentProfile.collectAsState()
                            val profiles by viewModel.profiles.collectAsState()
                            
                            val profileInitials = currentProfile?.name?.take(2)?.uppercase() ?: "ПР"
                            val profileColorHex = currentProfile?.avatarColor ?: "FF1E88E5"
                            val profileColor = try {
                                val hex = profileColorHex.trim().removePrefix("#")
                                Color(android.graphics.Color.parseColor("#$hex"))
                            } catch (e: Throwable) {
                                Color(0xFF1E88E5)
                            }

                            IconButton(
                                onClick = { showProfileManagerDialog = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(profileColor.copy(alpha = 0.25f), CircleShape)
                                    .border(1.dp, profileColor, CircleShape)
                                    .testTag("profile_badge_button")
                            ) {
                                Text(
                                    text = profileInitials,
                                    color = textPrimaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

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
                            onOpenSecuritySettings = {
                                showSecuritySettingsDialog = true
                            },
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

                    val bookmarks by viewModel.bookmarksList.collectAsState()
                    val isBookmarked = bookmarks.contains(currentUrl)
                    IconButton(
                        onClick = {
                            viewModel.toggleBookmark(currentUrl, context)
                            val toastMsg = if (!isBookmarked) "Добавлено в закладки" else "Удалено из закладок"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp).testTag("bookmark_star_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Закладка",
                            tint = if (isBookmarked) Color(0xFFFFC000) else textPrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Security Shield Lock Icon with MicroWaves (as requested) (Clickable to open security settings panel)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { showSecuritySettingsDialog = true }
                        .testTag("security_shield_icon_button"),
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

                // Advanced Settings cog
                IconButton(
                    onClick = { showAdvancedSettingsDialog = true },
                    modifier = Modifier.size(32.dp).testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
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
                        Toast.makeText(context, "Идет синхронизация профиля по ГОСТ-TLS...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
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

    // Onboarding / Privacy Policy Check overlapping dialog
    if (!showAppLoginLockScreen && !hasAcceptedPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = {}, // Force compliance
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF0E7490))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Конфиденциальность РосБраузер", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Добро пожаловать в суверенный РосБраузер! Наш браузер полностью защищен по российским стандартам ГОСТ шифрования и не передает данные третьим странанам.",
                        fontSize = 12.sp, color = textPrimaryColor
                    )
                    Text(
                        "Нажимая кнопку «Принять», вы соглашаетесь с Политикой Конфиденциальности РФ и разрешаете обработку локальных куки-файлов.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyPolicyTextDialog = true }
                            .border(1.dp, glassBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = textSecondaryColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Читать полный текст политики", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF0D9488)))
                    }
 
                    // Notification request checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isNotificationPermissionGranted,
                            onCheckedChange = { isNotificationPermissionGranted = it }
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text("Разрешить системные уведомления", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                            Text("Для оперативных проверок безопасности ФСБ", fontSize = 9.sp, color = textSecondaryColor)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hasAcceptedPrivacyPolicy = true
                        Toast.makeText(context, "Правила приняты! Защита активирована.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E7490))
                ) {
                    Text("Согласиться и Принять", color = Color.White)
                }
            },
            containerColor = glassBg,
            titleContentColor = textPrimaryColor,
            textContentColor = textPrimaryColor
        )
    }

    // App Login PIN lock screen overlay with Summer Glassmorphism (turquoise, sand, sun gradients)
    if (showAppLoginLockScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D9488), // Бирюзовый
                            Color(0xFFFBBF24), // Песочный
                            Color(0xFFFDE047)  // Солнечный
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "РосБраузер Сейф-Контроль",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Введите ПИН-код для расшифровки локальной базы паролей",
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // PIN dots representation
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val active = enteredPinCode.length > i
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        if (active) Color(0xFF34D399) else Color.White.copy(alpha = 0.35f),
                                        CircleShape
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Modern visual keyboard layout
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Стереть", "0", "Войти")
                        )
                        for (row in keys) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                for (k in row) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                            .clickable {
                                                if (k == "Стереть") {
                                                    if (enteredPinCode.isNotEmpty()) {
                                                        enteredPinCode = enteredPinCode.dropLast(1)
                                                    }
                                                } else if (k == "Войти") {
                                                    if (enteredPinCode == userLoginPinCode) {
                                                        showAppLoginLockScreen = false
                                                        Toast.makeText(context, "Локальная база успешно расшифрована!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Неверный ПИН-код! Попробуйте '1234'", Toast.LENGTH_SHORT).show()
                                                        enteredPinCode = ""
                                                    }
                                                } else {
                                                    if (enteredPinCode.length < 4) {
                                                        enteredPinCode += k
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = k,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (k.length > 1) 11.sp else 18.sp
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

    // Advanced setting dialog modal
    if (showAdvancedSettingsDialog) {
        val haptic = LocalHapticFeedback.current
        val blockedCount by viewModel.blockedDomainsCount.collectAsState()
        val filterLevel by viewModel.filterLevel.collectAsState()
        val biometricsEnabled by viewModel.isBiometricsEnabled.collectAsState()
        val inYandexMode by viewModel.isLoggedInYandex.collectAsState()
        val usernameYandex by viewModel.yandexUsername.collectAsState()
        val currentProfile by viewModel.currentProfile.collectAsState()
        val profiles by viewModel.profiles.collectAsState()
        
        AlertDialog(
            onDismissRequest = { showAdvancedSettingsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Управление РосБраузер", fontWeight = FontWeight.Bold, color = textPrimaryColor, fontSize = 16.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE53935).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("РФ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Custom Glass Scrollable Tabs - Reorganized for clear modular structure
                    val tabs = listOf("Оформление (Тема)", "Безопасность", "Профиль & Облако")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tabs.forEachIndexed { idx, label ->
                            val isSel = activeSettingsTab == idx
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) Color(0xFF0D9488) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        activeSettingsTab = idx
                                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else textPrimaryColor.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            when (activeSettingsTab) {
                                0 -> {
                                    // Custom extracted theme component
                                    ThemeSelectorComponent(
                                        browserMode = browserMode,
                                        isSummerBgAnimEnabled = isSummerBgAnimEnabled,
                                        flagWaveSpeed = flagWaveSpeed,
                                        fontSizeScale = fontSizeScale,
                                        searchEnginePreset = searchEnginePreset,
                                        isHapticVibeEnabled = isHapticVibeEnabled,
                                        haptic = haptic,
                                        context = context,
                                        viewModel = viewModel,
                                        textPrimaryColor = textPrimaryColor,
                                        textSecondaryColor = textSecondaryColor,
                                        glassBorder = glassBorder,
                                        onModeChange = { m ->
                                            viewModel.changeMode(m)
                                            if (m == BrowserMode.REGULAR) {
                                                viewModel.updateSearchQuery("")
                                            }
                                        }
                                    )
                                }
                                1 -> {
                                    // Custom extracted security component
                                    SecuritySettingsComponent(
                                        filterLevel = filterLevel,
                                        biometricsEnabled = biometricsEnabled,
                                        userLoginPinCode = userLoginPinCode,
                                        enteredPinCode = enteredPinCode,
                                        isHapticVibeEnabled = isHapticVibeEnabled,
                                        haptic = haptic,
                                        context = context,
                                        viewModel = viewModel,
                                        textPrimaryColor = textPrimaryColor,
                                        textSecondaryColor = textSecondaryColor,
                                        glassBorder = glassBorder,
                                        onPinEnteredChange = { enteredPinCode = it },
                                        onPinUpdate = { userLoginPinCode = it },
                                        onOpenPasswords = {
                                            showAdvancedSettingsDialog = false
                                            if (biometricsEnabled) {
                                                showBiometricUnlockDialog = true
                                            } else {
                                                showSavedPasswordsDialog = true
                                            }
                                        }
                                    )
                                }
                                2 -> {
                                    // tab 2: profile & sync (previously tab 3)
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .border(1.dp, glassBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val profileInitials = currentProfile?.name?.take(2)?.uppercase() ?: "ПР"
                                                val profileColorHex = currentProfile?.avatarColor ?: "FF1E88E5"
                                                val profileColor = try {
                                                    val hex = profileColorHex.trim().removePrefix("#")
                                                    Color(android.graphics.Color.parseColor("#$hex"))
                                                } catch (e: Throwable) {
                                                    Color(0xFF1E88E5)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(profileColor.copy(alpha = 0.25f), CircleShape)
                                                        .border(1.dp, profileColor, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(profileInitials, color = textPrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Текущий Профиль:", fontSize = 10.sp, color = textSecondaryColor)
                                                    Text(currentProfile?.name ?: "Неизвестно", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimaryColor)
                                                    Text("ID: РФ-941-${currentProfile?.id ?: 1}", fontSize = 9.sp, color = textSecondaryColor)
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                showAdvancedSettingsDialog = false
                                                showProfileManagerDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                        ) {
                                            Icon(Icons.Default.Group, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Выбрать другой профиль", fontSize = 11.sp, color = Color.White)
                                        }

                                        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f))

                                        Text("Облачная репликация Yandex ID:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimaryColor)
                                        
                                        if (inYandexMode) {
                                            Text("Облачная репликация Yandex ID активна", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                            Text("Данные пользователя ($usernameYandex) зашифрованы по TLS ГОСТ.", fontSize = 9.sp, color = textSecondaryColor)
                                            Button(
                                                onClick = { 
                                                    viewModel.logOutYandex(context)
                                                    if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Выйти из Yandex ID", fontSize = 11.sp, color = Color.White)
                                            }
                                        } else {
                                            Text(
                                                "Синхронизация временно отключена. Вы можете войти в облачный профиль Yandex на главном экране браузера.",
                                                fontSize = 10.sp,
                                                color = textSecondaryColor
                                            )
                                            Button(
                                                onClick = {
                                                    showAdvancedSettingsDialog = false
                                                    showYandexLoginDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Войти в Yandex ID 🇷🇺", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDonateAndPromoDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth().testTag("open_donate_dialog_button")
                    ) {
                        Icon(Icons.Default.CardGiftcard, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("💰 Донат-магазин и Промокоды", fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showAdvancedSettingsDialog = false 
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Text("Готово")
                }
            },
            containerColor = glassBg,
            textContentColor = textPrimaryColor,
            titleContentColor = textPrimaryColor
        )
    }

    // Biometric scanner dialogue simulate
    if (showBiometricUnlockDialog) {
        val infinitePulse = rememberInfiniteTransition(label = "BioPulse")
        val scanRadar by infinitePulse.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BioRadarScale"
        )
        // Autoclose and proceed to password manager in 1.8 seconds simulating fingerprint scan
        LaunchedEffect(Unit) {
            delay(1800)
            showBiometricUnlockDialog = false
            showSavedPasswordsDialog = true
            Toast.makeText(context, "Биометрия распознана! Доступ разрешен", Toast.LENGTH_SHORT).show()
        }

        AlertDialog(
            onDismissRequest = { showBiometricUnlockDialog = false },
            title = { Text("Биометрическая верификация") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Приложите палец к кнопке сканирования или посмотрите в камеру (FaceID)", fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                        // Pulsing outer radar circle
                        Box(
                            modifier = Modifier
                                .size(80.dp * scanRadar)
                                .border(2.dp, Color(0xFF00FF66).copy(alpha = 1.3f - scanRadar), CircleShape)
                        )
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Scan Finger",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Запрос к локальной системной службе безопасности...", fontSize = 10.sp, color = textSecondaryColor)
                }
            },
            confirmButton = {}
        )
    }

    // Saved Passwords Dialog
    if (showSavedPasswordsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedPasswordsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Встроенный менеджер паролей")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Сохраненные реквизиты доступа полностью зашифрованы ГОСТ алгоритмом на вашем устройстве:", fontSize = 11.sp)
                    
                    val passwordsList = listOf(
                        Triple("gosuslugi.ru", "ivanov_russia", "Gosuslugi1234_PF"),
                        Triple("yandex.ru", "rus_navigator", "YandexPassSummer199"),
                        Triple("nalog.gov.ru", "rus_taxpayer_32", "TaxPayer32890")
                    )
                    passwordsList.forEach { (site, login, pass) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(site, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E88E5))
                                Text("Логин: $login", fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Пароль: •••••••••••", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    TextButton(onClick = {
                                        Toast.makeText(context, "Пароль $pass скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text("Копировать", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSavedPasswordsDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Privacy Policy subtext dialogue
    if (showPrivacyPolicyTextDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyTextDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF0E7490))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Политика Конфиденциальности РосБраузер", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Настоящим соглашением устанавливается суверенная, расширенная и юридически выверенная политика конфиденциальности и порядка обработки пользовательской информации.",
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor
                    )
                    Text(
                        "1. СТАТУС СУВЕРЕННОЙ ИЗОЛЯЦИИ ДАННЫХ\n" +
                        "В соответствии со стандартами цифровой автономности, все пользовательские данные, включая закладки, пароли, историю сессий и метаданные, преобразуются в зашифрованные блоки и размещаются исключительно во внутреннем изолированном хранилище Sandbox приложения на физическом устройстве.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "2. КРИПТОГРАФИЧЕСКАЯ ЗАЗАЩИТА СЕЙФА\n" +
                        "Локальный сейф паролей и персональный профиль шифруются с использованием российского симметричного алгоритма блочного шифрования «Кузнечик» (ГОСТ Р 34.12-2015) с длиной ключа 256 бит, защищая данные от несанкционированного извлечения физическими лицами или третьими сторонами.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "3. БЕЗОПАСНАЯ ОБЛАЧНАЯ РЕПЛИКАЦИЯ\n" +
                        "Синхронизация профилей может проводиться пользователем добровольно через защищенное облако Yandex Cloud. Любая передача сетевых пакетов осуществляется исключительно по шифрованным протоколам TLS-ГОСТ с сертифицированным подтверждением безопасности.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "4. ФИЛЬТРАЦИЯ СЛЕЖКИ И РКН БЛОКИРОВКИ\n" +
                        "Встроенный компонент сетевой безопасности сверяет посещаемые URL-адреса с официальными реестрами РКН и списком неблагоприятных доменов, блокируя сборщики рекламы, пиксели сквозной слежки и фишинговые ресурсы в режиме реального времени.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "5. КОНТРОЛЬ ПЕРИОДИЧЕСКОЙ СОВМЕСТИМОСТИ\n" +
                        "Пользователь соглашается с получением технических уведомлений о проверке целостности защищенного контейнера мобильного устройства, необходимых для поддержания стабильной связи и предотвращения утечек локальных баз паролей.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "6. СОЮЗНЫЕ КУКИ-ФАЙЛЫ И ОЧИСТКА В КЛИК\n" +
                        "Временные куки-файлы (cookies) сессии не подлежат трансграничной передаче. По нажатию специальной функциональной кнопки «Смыть cookies волной» все локальные сессии стираются из памяти незамедлительно и безвозвратно.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "7. ЭКСКЛЮЗИВНЫЕ ПРАВА ПОЛЬЗОВАТЕЛЯ\n" +
                        "Пользователь имеет право в любой момент изменить свой защитный ПИН-код (пароль дешифровки по умолчанию — '1234') в разделе повышенной безопасности настроек Сейф-Контроля, а также полностью отозвать согласие с удалением приложения.",
                        fontSize = 10.sp, color = textPrimaryColor
                    )
                    Text(
                        "Данное соглашение составлено в соответствии с федеральным законом № 152-ФЗ «О персональных данных» и дополнено спецификацией ГОСТ для суверенного мобильного софта.",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondaryColor, modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyTextDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E7490))
                ) {
                    Text("Я ознакомился и согласен", fontSize = 11.sp, color = Color.White)
                }
            },
            containerColor = glassBg,
            titleContentColor = textPrimaryColor,
            textContentColor = textPrimaryColor
        )
    }

    // Interactive Dialog 4: RosBrowser Security Center (Центр Безопасности РосБраузер)
    if (showSecuritySettingsDialog) {
        val blockedCount by viewModel.blockedDomainsCount.collectAsState()
        val filterLevel by viewModel.filterLevel.collectAsState()
        val trackerBlockingEnabled by viewModel.isTrackerBlockingEnabled.collectAsState()

        AlertDialog(
            onDismissRequest = { showSecuritySettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (trackerBlockingEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Центр Безопасности", fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Safety check status with a pulsing effect
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, glassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = glassBg.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val infinitePulse = rememberInfiniteTransition(label = "CheckPulse")
                                val checkScale by infinitePulse.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = EaseInOutSine),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "CheckWave"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .graphicsLayer {
                                            scaleX = checkScale
                                            scaleY = checkScale
                                        }
                                        .background(if (trackerBlockingEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (trackerBlockingEnabled) "Защита АКТИВНА" else "Защита Ослаблена",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (trackerBlockingEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Интеллектуальная фильтрация угроз защищает вас от вредоносных скриптов, жучков, шпионов и несанкционированного слежения.",
                                fontSize = 11.sp,
                                color = textSecondaryColor
                            )
                        }
                    }

                    // Connection Security
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Шифрование ГОСТ TLS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                            Text("Все шлюзы зашифрованы по отечественным ГОСТ правилам.", fontSize = 9.sp, color = textSecondaryColor)
                        }
                    }

                    HorizontalDivider(color = glassBorder.copy(alpha = 0.3f))

                    // Tracker Blocking switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Блокировка трекеров", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                            Text("Останавливать рекламные счетчики, веб-маяки и слежку", fontSize = 9.sp, color = textSecondaryColor)
                        }
                        Switch(
                            checked = trackerBlockingEnabled,
                            onCheckedChange = { viewModel.setTrackerBlockingEnabled(it, context) },
                            modifier = Modifier.testTag("tracker_blocking_switch")
                        )
                    }

                    // Block stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, glassBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = textSecondaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Заблокировано опасных элементов: $blockedCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimaryColor
                        )
                    }

                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Инициирована принудительная гос-проверка безопасности. Узел чист.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Верифицировать SSL/ГОСТ сертификат", fontSize = 11.sp, color = Color(0xFF1E88E5))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecuritySettingsDialog = false }
                ) {
                    Text("Готово")
                }
            },
            containerColor = glassBg,
            textContentColor = textPrimaryColor,
            titleContentColor = textPrimaryColor
        )
    }

    // Interactive Dialog 5: RosBrowser Profile Management System (Управление Профилями)
    if (showProfileManagerDialog) {
        val profiles by viewModel.profiles.collectAsState()
        val currentProfile by viewModel.currentProfile.collectAsState()
        var showCreateProfileSection by remember { mutableStateOf(false) }
        var newProfileName by remember { mutableStateOf("") }
        val avatarColorsList = listOf(
            "FF1E88E5" to "Синий",
            "FFF44336" to "Красный",
            "FF4CAF50" to "Зеленый",
            "FFFF9800" to "Оранжевый",
            "FF9C27B0" to "Фиолетовый"
        )
        val selectedAvatarColor = remember { mutableStateOf(avatarColorsList[0].first) }

        AlertDialog(
            onDismissRequest = { showProfileManagerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Профили Пользователей", fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Профили позволяют изолировать историю, закладки (табло), настройки, адблок и куки-файлы сессий как на компьютере.",
                        fontSize = 11.sp,
                        color = textSecondaryColor
                    )

                    // Profile List
                    profiles.forEach { profile ->
                        val isCurrent = currentProfile?.id == profile.id
                        val pColor = try {
                            val hex = profile.avatarColor.trim().removePrefix("#")
                            Color(android.graphics.Color.parseColor("#$hex"))
                        } catch (e: Throwable) {
                            Color(0xFF1E88E5)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchProfile(profile.id, context)
                                    showProfileManagerDialog = false
                                    Toast.makeText(context, "Переключено на профиль: ${profile.name}", Toast.LENGTH_SHORT).show()
                                }
                                .border(
                                    if (isCurrent) 1.5.dp else 0.5.dp,
                                    if (isCurrent) pColor else glassBorder.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) pColor.copy(alpha = 0.15f) else glassBg.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Colored profile avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(pColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = profile.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimaryColor
                                        )
                                        Text(
                                            text = "Рябь: Находок ${profile.shortcuts.size} • Фильтр: ${profile.filterLevel}",
                                            fontSize = 9.sp,
                                            color = textSecondaryColor
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (profile.id.startsWith("profile_")) {
                                        // Can delete custom profiles
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteProfile(profile.id, context)
                                                Toast.makeText(context, "Профиль удален", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp).testTag("delete_profile_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить профиль",
                                                tint = Color(0xFFF44336),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = glassBorder.copy(alpha = 0.3f))

                    // Create Profile Action Toggle
                    if (!showCreateProfileSection) {
                        Button(
                            onClick = { showCreateProfileSection = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Создать новый профиль")
                        }
                    } else {
                        // Profile creation form
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, glassBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = glassBg.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Новая Личность", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimaryColor)
                                
                                OutlinedTextField(
                                    value = newProfileName,
                                    onValueChange = { newProfileName = it },
                                    label = { Text("Имя профиля") },
                                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                                    singleLine = true
                                )

                                Text("Цвет ярлыка:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textSecondaryColor)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    avatarColorsList.forEach { (colorHex, name) ->
                                        val color = try {
                                             val hex = colorHex.trim().removePrefix("#")
                                             Color(android.graphics.Color.parseColor("#$hex"))
                                         } catch (e: Throwable) {
                                             Color(0xFF1E88E5)
                                         }
                                        val isSelected = selectedAvatarColor.value == colorHex
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(color, CircleShape)
                                                .border(
                                                    if (isSelected) 2.dp else 0.dp,
                                                    if (isSelected) textPrimaryColor else Color.Transparent,
                                                    CircleShape
                                                )
                                                .clickable { selectedAvatarColor.value = colorHex }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { showCreateProfileSection = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Отмена")
                                    }
                                    Button(
                                        onClick = {
                                            if (newProfileName.isNotBlank()) {
                                                viewModel.createProfile(newProfileName.trim(), selectedAvatarColor.value, context)
                                                newProfileName = ""
                                                showCreateProfileSection = false
                                                Toast.makeText(context, "Профиль создан!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Пожалуйста, введите имя профиля", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1.2f).testTag("save_profile_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("Сохранить")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileManagerDialog = false }) {
                    Text("Закрыть")
                }
            },
            containerColor = glassBg,
            textContentColor = textPrimaryColor,
            titleContentColor = textPrimaryColor
        )
    }

    if (showDonateAndPromoDialog) {
        val balance by viewModel.userBalance.collectAsState()
        val isAdmin by viewModel.isAdminEnabled.collectAsState()
        val purchased by viewModel.purchasedItems.collectAsState()
        val currentProfile by viewModel.currentProfile.collectAsState()
        var promoInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDonateAndPromoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ДонатМаг и Промокоды", fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Balance Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, glassBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = glassBg.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Ваш Баланс", fontSize = 11.sp, color = textSecondaryColor)
                            Text(
                                "$balance рублей",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.addBalance(150) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+150 руб", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.addBalance(500) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+500 руб", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Promo Code Entry Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Активация Промокода", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = promoInput,
                                onValueChange = { promoInput = it },
                                placeholder = { Text("Код (напр. 'admin')", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).testTag("promo_code_input_field"),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = textPrimaryColor),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9800),
                                    unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.4f)
                                )
                            )
                            Button(
                                onClick = {
                                    if (promoInput.isNotBlank()) {
                                        val result = viewModel.usePromoCode(promoInput)
                                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        promoInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                modifier = Modifier.testTag("apply_promo_button")
                            ) {
                                Text("Ввод")
                            }
                        }
                        Text("Попробуйте промокоды: 'admin', 'letorussia', 'rosbrowser'", fontSize = 9.sp, color = textSecondaryColor)
                    }

                    HorizontalDivider(color = glassBorder.copy(alpha = 0.3f))

                    // Donate Shop items list
                    Text("Витрина Магазина", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                    val shopItems = listOf(
                        Triple("VIP Золотой статус профилей", 300, Icons.Default.Star),
                        Triple("Премиум тема: Галактика Салют", 500, Icons.Default.Lock),
                        Triple("Российский Турбо-Спидбуст 10G", 1000, Icons.Default.TrendingUp)
                    )

                    shopItems.forEach { (item, price, icon) ->
                        val hasBought = purchased.contains(item)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, glassBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = glassBg.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                                        Text("Цена: $price руб", fontSize = 10.sp, color = textSecondaryColor)
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (!hasBought) {
                                            val res = viewModel.purchaseItem(item, price)
                                            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasBought) Color.DarkGray else Color(0xFFFF9800)
                                    ),
                                    enabled = !hasBought,
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text(if (hasBought) "Куплено" else "Купить", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Admin Panel Box (Only shows if isAdmin == true)
                    if (isAdmin) {
                        HorizontalDivider(color = glassBorder.copy(alpha = 0.4f))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFF00FF66), RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = Color(0xFF00FF66),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "ПАНЕЛЬ АДМИНИСТРАТОРА v1.8",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF00FF66)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.setAdminStatus(false) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Выход", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Text(
                                    "Вы зашли под секретной служебной ролью Госаппарата. Спецнастройки активны.",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )

                                // Action 1: Add large balance
                                Button(
                                    onClick = {
                                        viewModel.addBalance(10000)
                                        Toast.makeText(context, "Начислено секретной субсидией +10 000 рублей!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                                    modifier = Modifier.fillMaxWidth().height(34.dp)
                                ) {
                                    Text("Начислить +10,000 руб 💸", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                // Action 2: Stealth invisible FSB Mode
                                Button(
                                    onClick = {
                                        // Switch active profile to Stealth browser mode instantly
                                        viewModel.switchProfile(currentProfile?.id ?: "default", context)
                                        Toast.makeText(context, "ВНИМАНИЕ: Стелс-режим ФСБ принудительно запущен!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                                    modifier = Modifier.fillMaxWidth().height(34.dp)
                                ) {
                                    Text("Запустить Стелс-режим ФСБ 🕵️‍♂️", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDonateAndPromoDialog = false }) {
                    Text("Закрыть")
                }
            },
            containerColor = glassBg,
            textContentColor = textPrimaryColor,
            titleContentColor = textPrimaryColor
        )
    }
}

// Global helper for FSB Notification compliance
fun sendFSBNotification(context: Context) {
    val channelId = "rosbrowser_security"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "ГосБезопасность", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Уведомления государственной службы мониторинга"
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("РосБраузер СпецСвязь")
        .setContentText("ваш телефон прослушивает ФСБ, не сопротивляйтесь.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        
    try {
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    } catch(e: Exception) {
        // Fallback alert on terminal or toast if android notifications blocks
        Toast.makeText(context, "ФСБ: ваш телефон прослушивается, не сопротивляйтесь.", Toast.LENGTH_LONG).show()
    }
}
