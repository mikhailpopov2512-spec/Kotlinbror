package com.example

import android.content.Context
import android.widget.Toast
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Core Theme Type selection: Summer vs Standard
        Text(
            text = "Выберите оформление (Тему):",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textPrimaryColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        2.dp,
                        if (isSummerBgAnimEnabled) Color(0xFFFBBF24) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSummerBgAnimEnabled) Color(0xFFFEF3C7).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Летняя тема",
                        tint = if (isSummerBgAnimEnabled) Color(0xFFFBBF24) else textPrimaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Летняя Тема",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textPrimaryColor
                    )
                    Text(
                        text = "Ромашки, бабочки, солнце, ветер",
                        fontSize = 8.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center
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
                        2.dp,
                        if (!isSummerBgAnimEnabled) Color(0xFF0E7490) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isSummerBgAnimEnabled) Color(0xFFE0F2FE).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Стандартная тема",
                        tint = if (!isSummerBgAnimEnabled) Color(0xFF0E7490) else textPrimaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Обычная Тема",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textPrimaryColor
                    )
                    Text(
                        text = "Строгий плоский кристальный стиль",
                        fontSize = 8.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Custom background photo row
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Свой фоновый рисунок (Фото):",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textPrimaryColor
        )

        val customBgPhoto by viewModel.customBgPhoto.collectAsState()

        val photoPresets = listOf(
            Pair("Сочи Пляж 🏖️", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1000&auto=format&fit=crop"),
            Pair("Байкал Ночь 🌌", "https://images.unsplash.com/photo-1495107334309-fcf20504a5ab?q=80&w=1000&auto=format&fit=crop"),
            Pair("Красная Площадь 🏰", "https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1000&auto=format&fit=crop"),
            Pair("Сибирский Бор 🌲", "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=1000&auto=format&fit=crop"),
            Pair("Неон Глоу ⚡", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=1000&auto=format&fit=crop")
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
                    .width(110.dp)
                    .clickable {
                        viewModel.setCustomBgPhoto(null, context)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .border(
                        2.dp,
                        if (customBgPhoto == null) Color(0xFFFBBF24) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(42.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("Без фото ❌", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                }
            }

            photoPresets.forEach { (name, url) ->
                val isSelected = (customBgPhoto == url)
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .clickable {
                            viewModel.setCustomBgPhoto(url, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .border(
                            2.dp,
                            if (isSelected) Color(0xFFFBBF24) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(42.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                        Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Custom image URL entry textfield
        var customUrlInput by remember { mutableStateOf(if (customBgPhoto != null && photoPresets.none { it.second == customBgPhoto }) customBgPhoto ?: "" else "") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customUrlInput,
                onValueChange = { customUrlInput = it },
                placeholder = { Text("Вставить ссылку на любое фото...", fontSize = 9.sp, color = textPrimaryColor.copy(alpha = 0.4f)) },
                singleLine = true,
                textStyle = TextStyle(color = textPrimaryColor, fontSize = 11.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D9488),
                    unfocusedBorderColor = textPrimaryColor.copy(alpha = 0.15f),
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(46.dp)
            )

            Button(
                onClick = {
                    if (customUrlInput.trim().isNotEmpty()) {
                        viewModel.setCustomBgPhoto(customUrlInput.trim(), context)
                        Toast.makeText(context, "Кастомный фон установлен!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(46.dp)
            ) {
                Text("ОК", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.15f))

        // Browser Mode Selection (Regular, Kids, Incognito, Guest, Stealth)
        Text(
            text = "Режимы приватности / профили отображения:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textPrimaryColor
        )

        val modesList = listOf(
            Triple(BrowserMode.REGULAR, "☀️ Обычный (\"Летняя Поляна\")", "Анимированные ромашки, маки, бабочки."),
            Triple(BrowserMode.INCOGNITO, "🌙 Инкогнито (\"Ночной Пляж\")", "Тёмная лунная гладь. Без сохранения истории."),
            Triple(BrowserMode.GUEST, "🌊 Гостевой (\"Морская Волна\")", "Временный профиль. Куки стираются."),
            Triple(BrowserMode.KIDS, "🐬 Детский (\"Умные Дельфины\")", "Безопасный детский Рунет."),
            Triple(BrowserMode.STEALTH, "👁 Скрытный (Stealth \"Матрица\")", "Зеленый терминал. Блок скриншотов.")
        )

        modesList.forEach { (m, name, desc) ->
            val isSel = (browserMode == m)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onModeChange(m)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .border(
                        1.5.dp,
                        if (isSel) Color(0xFF0D9488) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSel) Color(0xFFCCFBF1).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                        tint = if (isSel) Color(0xFF0D9488) else textPrimaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimaryColor)
                        Text(desc, fontSize = 8.sp, color = textSecondaryColor)
                    }
                }
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.15f))

        // Russian Search Engines Choice
        Text("Поисковая Система РФ:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimaryColor)
        val engines = listOf("RosPoisk", "Yandex", "Mail.ru", "Sputnik", "Google")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            engines.forEach { engine ->
                val isEngineSel = (searchEnginePreset == engine)
                Box(
                    modifier = Modifier
                        .background(
                            if (isEngineSel) Color(0xFF0D9488) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            viewModel.setSearchEnginePreset(engine, context)
                            if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ветровой поток (флаг РФ) 🚩", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                    Text(
                        text = "${String.format("%.1f", flagWaveSpeed)}x",
                        fontSize = 9.sp,
                        color = textSecondaryColor
                    )
                }
                Slider(
                    value = flagWaveSpeed,
                    onValueChange = { viewModel.setFlagWaveSpeed(it, context) },
                    valueRange = 0.0f..2.0f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0D9488),
                        activeTrackColor = Color(0xFF0D9488)
                    )
                )
            }
        }

        // Font scale layout slider
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Размер шрифта веб-интерфейса", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                Text(
                    text = "${String.format("%.1f", fontSizeScale)}x",
                    fontSize = 9.sp,
                    color = textSecondaryColor
                )
            }
            Slider(
                value = fontSizeScale,
                onValueChange = { viewModel.setFontSizeScale(it, context) },
                valueRange = 0.8f..1.4f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0D9488),
                    activeTrackColor = Color(0xFF0D9488)
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Уровень фильтрации неблагоприятных сайтов:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimaryColor)
        val filterLevels = listOf("Слабая", "Рекомендуемая", "Максимальная", "Строгая")
        filterLevels.forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setFilterLevel(level, context)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (filterLevel == level),
                    onClick = {
                        viewModel.setFilterLevel(level, context)
                        if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF0D9488)
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
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
                        color = textSecondaryColor
                    )
                }
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.15f))

        // Biometrics Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Биометрия TouchID / FaceID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
                Text("Предлагать биометрический вход по отпечатку пальца", fontSize = 8.sp, color = textSecondaryColor)
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

        HorizontalDivider(color = glassBorder.copy(alpha = 0.15f))

        // PIN code lock state and configuration update
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Защита входа приложения (Сейф-Контроль):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
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

            Spacer(modifier = Modifier.height(4.dp))

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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
            ) {
                Text("Обновить защитный PIN", fontSize = 11.sp, color = Color.White)
            }
        }

        HorizontalDivider(color = glassBorder.copy(alpha = 0.15f))

        // Safe passwords and system cleanup actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onOpenPasswords() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A87))
            ) {
                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Сейф паролей", fontSize = 10.sp, color = Color.White)
            }

            Button(
                onClick = {
                    if (isHapticVibeEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.incrementBlockedCount()
                    Toast.makeText(context, "Cookies успешно смыты морской волной! Сделано в РФ.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Icon(Icons.Default.Water, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Смыть cookies 🌊", fontSize = 10.sp, color = Color.White)
            }
        }
    }
}
