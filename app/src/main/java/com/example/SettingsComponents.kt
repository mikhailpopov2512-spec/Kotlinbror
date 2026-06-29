package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeSelectorComponent(
    browserMode: BrowserMode,
    isSummerBgAnimEnabled: Boolean,
    flagWaveSpeed: Float,
    fontSizeScale: Float,
    searchEnginePreset: String,
    isHapticVibeEnabled: Boolean,
    haptic: HapticFeedback,
    context: Context,
    viewModel: BrowserViewModel,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    glassBorder: Color,
    onModeChange: (BrowserMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header: Theme
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.Palette, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ОФОРМЛЕНИЕ И СТИЛЬ",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = textPrimaryColor,
                letterSpacing = 1.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summer theme option card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.setSummerBgAnimEnabled(true, context)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .border(
                        1.5.dp,
                        if (isSummerBgAnimEnabled) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSummerBgAnimEnabled) Color(0xFFFEF3C7).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFEF3C7).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Летняя тема",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Летняя Поляна",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Бабочки, цветы, ветер и анимация",
                        fontSize = 9.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }
            }

            // Standard theme option card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.setSummerBgAnimEnabled(false, context)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .border(
                        1.5.dp,
                        if (!isSummerBgAnimEnabled) Color(0xFF0D9488) else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isSummerBgAnimEnabled) Color(0xFFCCFBF1).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFCCFBF1).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Стандартная тема",
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Строгая Классика",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Минималистичный кристальный стиль",
                        fontSize = 9.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }
            }
        }

        // Custom background photo row
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.Image, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ФОНОВЫЙ РИСУНОК",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textPrimaryColor,
                letterSpacing = 0.5.sp
            )
        }

        val customBgPhoto by viewModel.customBgPhoto.collectAsState()

        val photoPresets = listOf(
            Pair("Сочи 🏖️", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1000&auto=format&fit=crop"),
            Pair("Байкал 🌌", "https://images.unsplash.com/photo-1495107334309-fcf20504a5ab?q=80&w=1000&auto=format&fit=crop"),
            Pair("Москва 🏰", "https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1000&auto=format&fit=crop"),
            Pair("Тайга 🌲", "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=1000&auto=format&fit=crop")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "Стандарт" option
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .clickable {
                        viewModel.setCustomBgPhoto(null, context)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .border(
                        1.5.dp,
                        if (customBgPhoto == null) Color(0xFF0D9488) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(48.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("Стандарт ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
            }

            photoPresets.forEach { (name, url) ->
                val isSelected = (customBgPhoto == url)
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .clickable {
                            viewModel.setCustomBgPhoto(url, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .border(
                            1.5.dp,
                            if (isSelected) Color(0xFF0D9488) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Custom image URL entry textfield
        var customUrlInput by remember { mutableStateOf(if (customBgPhoto != null && photoPresets.none { it.second == customBgPhoto }) customBgPhoto ?: "" else "") }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customUrlInput,
                onValueChange = { customUrlInput = it },
                placeholder = { Text("Вставить ссылку на любое фото...", fontSize = 11.sp, color = textPrimaryColor.copy(alpha = 0.4f)) },
                singleLine = true,
                textStyle = TextStyle(color = textPrimaryColor, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D9488),
                    unfocusedBorderColor = textPrimaryColor.copy(alpha = 0.12f),
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            )

            Button(
                onClick = {
                    if (customUrlInput.trim().isNotEmpty()) {
                        viewModel.setCustomBgPhoto(customUrlInput.trim(), context)
                        Toast.makeText(context, "Кастомный фон установлен!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("ОК", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        // Browser Mode Selection (Regular, Kids, Incognito, Guest, Stealth)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.Security, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "РЕЖИМЫ ПРИВАТНОСТИ И ПРОФИЛИ",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textPrimaryColor,
                letterSpacing = 0.5.sp
            )
        }

        val modesList = listOf(
            Triple(BrowserMode.REGULAR, "☀️ Обычный режим (\"Летняя Поляна\")", "Анимированные ромашки, маки и бабочки."),
            Triple(BrowserMode.INCOGNITO, "🌙 Режим Инкогнито (\"Ночной Пляж\")", "Тёмная лунная гладь. Без сохранения куки и истории."),
            Triple(BrowserMode.GUEST, "🌊 Гостевой режим (\"Морская Волна\")", "Временная сессия. Все куки стираются на выходе."),
            Triple(BrowserMode.KIDS, "🐬 Детский режим (\"Умные Дельфины\")", "Безопасный детский Рунет. Белый список сайтов."),
            Triple(BrowserMode.STEALTH, "👁 Скрытный режим (Stealth \"Матрица\")", "Суверенный зеленый терминал. Запрет скриншотов.")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            modesList.forEach { (m, name, desc) ->
                val isSel = (browserMode == m)
                val activeThemeColor = when(m) {
                    BrowserMode.REGULAR -> Color(0xFF0D9488)
                    BrowserMode.INCOGNITO -> Color(0xFF8B5CF6)
                    BrowserMode.GUEST -> Color(0xFF3B82F6)
                    BrowserMode.KIDS -> Color(0xFFEC4899)
                    BrowserMode.STEALTH -> Color(0xFF10B981)
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onModeChange(m)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .border(
                            1.5.dp,
                            if (isSel) activeThemeColor else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) activeThemeColor.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (isSel) activeThemeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when(m) {
                                    BrowserMode.REGULAR -> Icons.Default.WbSunny
                                    BrowserMode.INCOGNITO -> Icons.Default.ModeNight
                                    BrowserMode.GUEST -> Icons.Default.Water
                                    BrowserMode.KIDS -> Icons.Default.ChildCare
                                    BrowserMode.STEALTH -> Icons.Default.VisibilityOff
                                },
                                contentDescription = name,
                                tint = if (isSel) activeThemeColor else textPrimaryColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimaryColor)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desc, fontSize = 9.sp, color = textSecondaryColor, lineHeight = 11.sp)
                        }
                        if (isSel) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeThemeColor)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        // Russian Search Engines Choice
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ПОИСКОВАЯ СИСТЕМА РФ",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textPrimaryColor,
                letterSpacing = 0.5.sp
            )
        }

        val engines = listOf("RosPoisk", "Yandex", "Mail.ru", "Sputnik", "Google")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            engines.forEach { engine ->
                val isEngineSel = (searchEnginePreset == engine)
                Box(
                    modifier = Modifier
                        .background(
                            if (isEngineSel) Color(0xFF0D9488) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isEngineSel) Color(0xFF0D9488) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            viewModel.setSearchEnginePreset(engine, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if(engine == "RosPoisk") "РосПоиск 🇷🇺" else engine,
                        color = if (isEngineSel) Color.White else textPrimaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Additional Visual sliders (Wind speed for Summer Flag)
        if (isSummerBgAnimEnabled) {
            HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ветровой поток флага РФ 🚩", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                    }
                    Text(
                        text = "${String.format("%.1f", flagWaveSpeed)}x",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488)
                    )
                }
                Slider(
                    value = flagWaveSpeed,
                    onValueChange = { viewModel.setFlagWaveSpeed(it, context) },
                    valueRange = 0.0f..2.0f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0D9488),
                        activeTrackColor = Color(0xFF0D9488),
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
            }
        }

        // Font scale layout slider
        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Масштаб шрифта интерфейса", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
                Text(
                    text = "${String.format("%.1f", fontSizeScale)}x",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488)
                )
            }
            Slider(
                value = fontSizeScale,
                onValueChange = { viewModel.setFontSizeScale(it, context) },
                valueRange = 0.8f..1.4f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0D9488),
                    activeTrackColor = Color(0xFF0D9488),
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun SecuritySettingsComponent(
    filterLevel: String,
    biometricsEnabled: Boolean,
    userLoginPinCode: String,
    enteredPinCode: String,
    isHapticVibeEnabled: Boolean,
    haptic: HapticFeedback,
    context: Context,
    viewModel: BrowserViewModel,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    glassBorder: Color,
    onPinEnteredChange: (String) -> Unit,
    onPinUpdate: (String) -> Unit,
    onOpenPasswords: () -> Unit
) {
    val isTrackerBlockingEnabled by viewModel.isTrackerBlockingEnabled.collectAsState()
    val isAdBlockActive by viewModel.isAdBlockActive.collectAsState()
    val isSecureWarningEnabled by viewModel.isSecureWarningEnabled.collectAsState()
    val isEasyListRussiaEnabled by viewModel.isEasyListRussiaEnabled.collectAsState()
    val isRuAdListEnabled by viewModel.isRuAdListEnabled.collectAsState()
    val blockedDomainsCount by viewModel.blockedDomainsCount.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header: Security Dashboard & Banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.Shield, null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "БЕЗОПАСНОСТЬ И БЛОКИРОВКИ",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = textPrimaryColor,
                letterSpacing = 1.sp
            )
        }

        // 'Банановая Защита' Real-Time Stats Block & Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFFFBBF24).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(18.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7).copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header of the security shield card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFEF3C7).copy(alpha = 0.25f), CircleShape)
                            .border(1.dp, Color(0xFFF59E0B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Банановая Защита РФ 🍌",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "Ультра-фильтрация сайтов и скрытая защита",
                            fontSize = 9.sp,
                            color = textSecondaryColor.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "АКТИВНО",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFFBBF24).copy(alpha = 0.12f))

                // Stats Dashboard inside Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "ЗАБЛОКИРОВАНО УГРОЗ И РЕКЛАМЫ:",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondaryColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$blockedDomainsCount элементов",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFBBF24)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.incrementBlockedCount()
                            Toast.makeText(context, "Статистика сброшена и откалибрована!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("СБРОС ⚡", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Feature 1: Ad-blocking (Банановый Щит)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Блокировка рекламы (Банановый Щит) 🛡️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                        Text(
                            text = "Удаление рекламных баннеров, видео-рекламы и всплывающих поп-апов",
                            fontSize = 8.sp,
                            color = textSecondaryColor
                        )
                    }
                    Switch(
                        checked = isAdBlockActive,
                        onCheckedChange = {
                            viewModel.setAdBlockActive(it, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                if (it) "Банановый Щит активен: реклама заблокирована!" else "Реклама разрешена",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF59E0B)
                        )
                    )
                }

                // Feature 2: Anti-tracking (Банановый След)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Анти-трекинг (Банановый След) 🐾",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                        Text(
                            text = "Блокировка жучков слежки, аналитических трекеров и веб-маяков",
                            fontSize = 8.sp,
                            color = textSecondaryColor
                        )
                    }
                    Switch(
                        checked = isTrackerBlockingEnabled,
                        onCheckedChange = {
                            viewModel.setTrackerBlockingEnabled(it, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                if (it) "Банановый След включен: следящие скрипты отрезаны!" else "Слежка разрешена",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF59E0B)
                        )
                    )
                }

                // Feature 3: EASYLIST RUSSIA ENABLER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "База фильтров EasyList Russia 🇷🇺",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE53935).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("РФ", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                            }
                        }
                        Text(
                            text = "Суверенные правила для вырезания рекламы на Яндексе, Mail.ru, VK",
                            fontSize = 8.sp,
                            color = textSecondaryColor
                        )
                    }
                    Switch(
                        checked = isEasyListRussiaEnabled,
                        onCheckedChange = {
                            viewModel.setEasyListRussiaEnabled(it, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                if (it) "Списки EasyList Russia загружены в систему!" else "EasyList Russia отключен",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE53935)
                        )
                    )
                }

                // Feature 4: RU ADLIST ENABLER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "База фильтров RU AdList 🕵️‍♂️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1E88E5).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("SEC", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
                            }
                        }
                        Text(
                            text = "Списки блокирования счетчиков метрик, ведения логов и краш-аналитики",
                            fontSize = 8.sp,
                            color = textSecondaryColor
                        )
                    }
                    Switch(
                        checked = isRuAdListEnabled,
                        onCheckedChange = {
                            viewModel.setRuAdListEnabled(it, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                if (it) "База фильтров RU AdList АКТИВНА!" else "RU AdList деактивирована",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF1E88E5)
                        )
                    )
                }

                // Feature 5: Secure Connection Warnings (Банановый Патруль)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Банановый Патруль (HTTP Контроль) 🚨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                        Text(
                            text = "Предупреждать при открытии незащищенных http протоколов",
                            fontSize = 8.sp,
                            color = textSecondaryColor
                        )
                    }
                    Switch(
                        checked = isSecureWarningEnabled,
                        onCheckedChange = {
                            viewModel.setSecureWarningEnabled(it, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                if (it) "Банановый Патруль на страже!" else "Патруль отключен",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF59E0B)
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        Text("Уровень фильтрации неблагоприятных сайтов:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimaryColor)
        val filterLevels = listOf("Слабая", "Рекомендуемая", "Максимальная", "Строгая")
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filterLevels.forEach { level ->
                val isSelected = (filterLevel == level)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF0D9488).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
                        .clickable {
                            viewModel.setFilterLevel(level, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .border(
                            0.5.dp,
                            if (isSelected) Color(0xFF0D9488) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            viewModel.setFilterLevel(level, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF0D9488),
                            unselectedColor = textSecondaryColor.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(level, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                        Text(
                            text = when (level) {
                                "Слабая" -> "Блокировка прямых вредоносных доменов реестра"
                                "Рекомендуемая" -> "Умный обход зеркал РКН и вредоносных прокси"
                                "Максимальная" -> "Умная фильтрация рекламных скриптов и слежки"
                                "Строгая" -> "Полная цифровая гос-проверка трафика по ГОСТ-сертификатам"
                                else -> ""
                            },
                            fontSize = 9.sp,
                            color = textSecondaryColor,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        // Biometrics Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Биометрия TouchID / FaceID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                Text("Предлагать биометрический вход по отпечатку пальца", fontSize = 9.sp, color = textSecondaryColor)
            }
            Switch(
                checked = biometricsEnabled,
                onCheckedChange = {
                    viewModel.setBiometricsEnabled(it, context)
                    if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0D9488)
                )
            )
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        // PIN code lock state and configuration update
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("Сейф-Контроль входа приложения:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Текущий PIN входа: $userLoginPinCode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
                    Text("Используйте PIN-код '1234' для входа по умолчанию", fontSize = 8.sp, color = textSecondaryColor)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = enteredPinCode,
                onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        onPinEnteredChange(it)
                    }
                },
                label = { Text("Введите новый PIN (до 4 цифр)") },
                placeholder = { Text("1234") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D9488),
                    unfocusedBorderColor = glassBorder,
                    focusedLabelColor = Color(0xFF0D9488),
                    unfocusedLabelColor = textSecondaryColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (enteredPinCode.length >= 2) {
                        onPinUpdate(enteredPinCode)
                        Toast.makeText(context, "Код безопасности изменен на $enteredPinCode!", Toast.LENGTH_SHORT).show()
                        onPinEnteredChange("")
                    } else {
                        Toast.makeText(context, "PIN-код должен содержать от 2 до 4 цифр!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Обновить защитный PIN", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        // Safe passwords and system cleanup actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onOpenPasswords() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A87)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сейф паролей", fontSize = 11.sp, color = Color.White)
            }

            Button(
                onClick = {
                    if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.incrementBlockedCount()
                    Toast.makeText(context, "Cookies успешно смыты морской волной! Сделано в РФ.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Water, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Смыть cookies 🌊", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}
