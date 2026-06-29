package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class AdminConfigItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Баланс & VIP", "Безопасность & ФСБ", "Игры & Бустеры", "Кастомизация", "Оптимизация"
    val isAction: Boolean = false,
    val initialLabelValue: String = ""
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RosAdminPanelPage(
    viewModel: BrowserViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val balance by viewModel.userBalance.collectAsState()
    val purchasedList by viewModel.purchasedItems.collectAsState()
    val activeToggles by viewModel.adminToggles.collectAsState()
    val activeFloats by viewModel.adminFloats.collectAsState()
    val activeTexts by viewModel.adminTexts.collectAsState()
    
    val blockedCount by viewModel.blockedDomainsCount.collectAsState()
    val isEasyListEnabled by viewModel.isEasyListRussiaEnabled.collectAsState()
    val isRuAdListEnabled by viewModel.isRuAdListEnabled.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }

    // Exactly 40 detailed industrial-grade admin parameters & functions
    val adminSchema = remember {
        listOf(
            // --- CATEGORY: "Баланс & VIP" (5 functions) ---
            AdminConfigItem("admin_add_10k", "Начислить +10,000 ₽", "Мгновенно пополнить РФ Кошелек на 10,000 рублей из резервного фонда.", "Баланс & VIP", isAction = true),
            AdminConfigItem("admin_reset_balance", "Обнулить баланс в РФ Кошельке", "Сбросить текущий баланс пользователя до 0 рублей.", "Баланс & VIP", isAction = true),
            AdminConfigItem("admin_unlock_all_vip", "Разблокировать все VIP темы и функции", "Мгновенное разблокирование всего каталога в РосМаркете бесплатно.", "Баланс & VIP", isAction = true),
            AdminConfigItem("admin_is_oligarch", "Активировать статус «ОЛИГАРХ»", "Выставить баланс ровно в 1,000,000 рублей для неограниченных покупок.", "Баланс & VIP", isAction = true),
            AdminConfigItem("admin_unlimit_promo_uses", "Безлимитное использование промокодов", "Снимает любые ограничения на повторное использование кодов.", "Баланс & VIP"),

            // --- CATEGORY: "Безопасность & ФСБ" (9 functions) ---
            AdminConfigItem("admin_fsb_agent_mode", "Режим секретного агента ФСБ", "Включает постоянный защищенный канал СПЕЦСВЯЗИ и выводит предупреждения.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_gost_encryption", "Шифрование ГОСТ-TLS 256-бит", "Принудительное криптографическое шифрование трафика по ГОСТ-34.12.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_only_gov_resources", "Режим Госаппарата", "Блокирует или фильтрует любые сайты, не входящие в реестр проверенных РФ ресурсов.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_auto_hist_clean", "Stealth автоматическая очистка истории", "Удаляет записи истории поиска и кэша каждые 10 сек при неактивности.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_tracker_blocker_promax", "Защита от иностранных трекеров слежки", "Полная фильтрация зарубежных трекеров аналитики.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_rusification_active", "Принудительная Рософикация поисковых фраз", "Конвертирует латинские поисковые запросы в суверенную кириллицу.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_rkn_certificate_check", "Антифишинг Роскомнадзора", "Активная сверка скачиваемых файлов по реестру фишинговых сайтов.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_anti_screenshot", "Защита от снимков экрана", "Блокирует системные скриншоты и видеозапись экрана в приложении.", "Безопасность & ФСБ"),
            AdminConfigItem("admin_local_encryption", "Крипто-хранение профилей", "Шифрует файлы баз данных и профилей локального кэша симметричным ключом.", "Безопасность & ФСБ"),

            // --- CATEGORY: "Игры & Бустеры" (8 functions) ---
            AdminConfigItem("admin_infinite_shells", "Бесконечные золотые ракушки", "Выставляет 999k ракушек в пляжном симуляторе Анапы.", "Игры & Бустеры"),
            AdminConfigItem("admin_click_multiplier_100", "Множитель кликов х100", "Увеличивает отдачу каждого клика в летней игре в сто раз.", "Игры & Бустеры"),
            AdminConfigItem("admin_crab_invincible", "Бессмертие краба в «ЛетоЗабег»", "Защищает пляжного песчаного краба от любых столкновений с чайками.", "Игры & Бустеры"),
            AdminConfigItem("admin_game_speed_05", "Замедление игр на х0.5", "Снижает скорость физики игр для безупречных рекордов.", "Игры & Бустеры"),
            AdminConfigItem("admin_high_yield_seeds", "Ускоренный рост фауны Анапы", "Сокращает время созревания суверенных семян и цветов до 1 сек.", "Игры & Бустеры"),
            AdminConfigItem("admin_unlimited_lives_runner", "Бессмертие в раннере", "Предоставляет бесконечное здоровье пляжному герою.", "Игры & Бустеры"),
            AdminConfigItem("admin_unlock_detective_clues", "Автоподсветка улик в «РосДетектив»", "Автоматически подсвечивает местонахождение всех скрытых объектов.", "Игры & Бустеры"),
            AdminConfigItem("admin_instant_sea_level", "Мгновенная очистка побережья", "Удаляет все загрязнения и водоросли с береговой линии Анапы за раз.", "Игры & Бустеры"),

            // --- CATEGORY: "Кастомизация" (9 functions) ---
            AdminConfigItem("admin_extreme_blur", "Сверхсильный Glassmorphism (50dp)", "Активирует размытие оверлеев до эффекта матового толстого стекла.", "Кастомизация"),
            AdminConfigItem("admin_solar_radiation_pro", "Удвоить летнее гало солнца короны", "Увеличивает визуальный диаметр коронного излучения летнего солнца на 100%.", "Кастомизация"),
            AdminConfigItem("admin_pond_density_120", "Рябь пруда на 120 FPS", "Обеспечивает предельную симуляцию водного шейдера на высоких частотах.", "Кастомизация"),
            AdminConfigItem("admin_flag_storm_speed", "Ветер у флага: Шторм (15.0)", "Установить штормовую скорость колебания флага России на главной странице.", "Кастомизация", isAction = true),
            AdminConfigItem("admin_flag_calm_speed", "Ветер у флага: Штиль (0.0)", "Полностью остановить анимацию развевания триколора флага.", "Кастомизация", isAction = true),
            AdminConfigItem("admin_force_dark_mode", "Инверсный темный веб-режим", "Принудительно окрашивает бэкграунд всех открываемых сайтов в темный тон.", "Кастомизация"),
            AdminConfigItem("admin_audio_sea_seagulls", "Звуковое сопровождение «Шум прибоя»", "При каждом нажатии воспроизводит шуршание гальки и крики сочинских чаек.", "Кастомизация"),
            AdminConfigItem("admin_dynamic_neon_glow", "Динамический неон вкладок", "Пускает световую градиентную волну по краям активной панели браузера.", "Кастомизация"),
            AdminConfigItem("admin_parallax_background", "Гироскопический параллакс", "Наклоняет летний пляжный фон в такт изменения ориентации телефона.", "Кастомизация"),

            // --- CATEGORY: "Оптимизация" (9 functions) ---
            AdminConfigItem("admin_traffic_debug_log", "Запись логов веб-трафика", "Логирует все исходящие GET/POST запросы в узел безопасности РосСеть.", "Оптимизация"),
            AdminConfigItem("admin_force_battery_saver", "Экстремальное энергосбережение", "Отключает фоновые волновые эффекты и уменьшает яркость подсветки.", "Оптимизация"),
            AdminConfigItem("admin_max_tabs_100", "Спец-Лимит на 100 открытых вкладок", "Расширяет пределы стандартного кэша браузера для сотен страниц.", "Оптимизация"),
            AdminConfigItem("admin_auto_correct_typos", "Автоисправление в РосПоиск", "При вводе текста исправляет популярные русские орфографические ошибки.", "Оптимизация"),
            AdminConfigItem("admin_speed_cache_ram", "Форсированный RAM-кэш", "Переводит весь дисковый кэш в оперативную память для турбо-скорости.", "Оптимизация"),
            AdminConfigItem("admin_sovereign_dns", "Суверенный DNS-сервер", "Направляет IP запросы через сверхзащищенные узлы РосТелеком.", "Оптимизация"),
            AdminConfigItem("admin_custom_region_sochi", "Локация: Крым и Сочи", "Передает веб-ресурсам геоданные главных курортов страны по умолчанию.", "Оптимизация"),
            AdminConfigItem("admin_debug_fps_counter", "Показывать реальный счетчик FPS", "Закрепляет зеленый цифровой оверлей кадров/сек в верхнем углу меню.", "Оптимизация"),
            AdminConfigItem("admin_factory_reset_all", "🔴 СБРОСИТЬ ВСЕ ДАННЫЕ И НАСТРОЙКИ", "Полный сброс параметров админки, рынка, баланса и профилей до заводских.", "Оптимизация", isAction = true)
        )
    }

    val categories = listOf("Все", "Аналитика 📊", "Баня РФ 🚫", "Баланс & VIP", "Безопасность & ФСБ", "Игры & Бустеры", "Кастомизация", "Оптимизация")

    // Filter items based on search query and category tab
    val filteredSchema = remember(searchQuery, selectedCategory, adminSchema) {
        adminSchema.filter { item ->
            val matchCat = (selectedCategory == "Все" || selectedCategory == "Аналитика 📊" || selectedCategory == "Баня РФ 🚫") || item.category == selectedCategory
            val matchSearch = searchQuery.isEmpty() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    // Interactive fluctuate states for Live CPU and RAM meters
    var liveCpuState by remember { mutableStateOf(42f) }
    var liveRamState by remember { mutableStateOf(61f) }
    
    // Live scrolling log panel inside terminal console
    val consoleLogs = remember {
        mutableStateListOf(
            "⚡️ Система РосБраузер Инициализирована успешно.",
            "🔒 Алгоритм шифрования ГОСТ-TLS 256-бит в режиме ожидания.",
            "🛡️ Облачный щит антифишинга Роскомнадзора запущен.",
            "📂 База данных заблокированных доменов загружена: 12 сайтов."
        )
    }
    
    val consoleListState = rememberLazyListState()

    LaunchedEffect(blockedCount) {
        if (blockedCount > 0) {
            consoleLogs.add("🛡️ [ФИЛЬТР] Перехвачен и заблокирован нежелательный запрос. Всего пресечено: $blockedCount")
        }
    }

    LaunchedEffect(Unit) {
        // CPU and RAM fluctuate loops
        launch {
            while (true) {
                delay(1200)
                liveCpuState = (35f + kotlin.random.Random.nextFloat() * 18f)
            }
        }
        launch {
            while (true) {
                delay(2000)
                liveRamState = (58f + kotlin.random.Random.nextFloat() * 6f)
            }
        }
        // Event logging loop
        launch {
            val logTemplates = listOf(
                "🛡️ [SECURE] Успешно проверено соединение с узлами РосТелеком.",
                "⚡️ [SYSTEM] Кэш RAM-диска очищен на %d Кб.",
                "👁️ [FSB] Проверен сертификат суверенного узла Минцифры РФ.",
                "🚫 [RKN] Предотвращена попытка обращения к заблокированному ресурсу.",
                "🎮 [BOOST] Множитель кликов х100 активен в фоновом режиме.",
                "📈 [NET] Пропускная способность оптимизирована компрессией.",
                "🎨 [THEME] Изменена прозрачность стеклянных оверлеев (Glassmorphism).",
                "🔋 [POWER] Режим энергосбережения: 120 FPS заблокирован."
            )
            while (true) {
                delay(2500)
                val isFsbMode = activeToggles["admin_fsb_agent_mode"] == true
                val randomTemplate = logTemplates.random()
                val logText = if (isFsbMode && kotlin.random.Random.nextFloat() > 0.5f) {
                    "🔴 [СВЯЗЬ ФСБ] Канал спецсвязи зашифрован. Трафик защищён."
                } else {
                    String.format(randomTemplate, kotlin.random.Random.nextInt(120, 1024))
                }
                consoleLogs.add(logText)
                if (consoleLogs.size > 25) {
                    consoleLogs.removeAt(0)
                }
                // Scroll to the bottom safely
                try {
                    consoleListState.animateScrollToItem(consoleLogs.size - 1)
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Premium Dark slate
                        Color(0xFF030712), // Deep black-blue
                        Color(0xFF1E1B4B)  // Cyber indigo glow
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .testTag("admin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ЦЕНТР УПРАВЛЕНИЯ",
                            style = TextStyle(
                                color = Color(0xFF34D399), // Mint green
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("ADMIN PRO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                    Text(
                        text = "Суверенное администрирование систем РосБраузера v2.5",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // --- CATEGORY TABS SCROLLER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val bgBrush = if (isSelected) {
                        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                    } else {
                        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f)))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgBrush)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar (Always visible except when viewing analytics specifically)
            if (selectedCategory != "Аналитика 📊") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск среди параметров и реестров...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_search_input"),
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF34D399),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = Color.White.copy(alpha = 0.04f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // MAIN CONTENT ROUTER
            when (selectedCategory) {
                "Аналитика 📊" -> {
                    // GORGEOUS ANALYTICAL DASHBOARD SCREEN WITH REAL-TIME CUSTOM CANVAS GRAPHS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        // 1. Double Gauge Round Indicators for CPU and RAM (Fluctuating)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "ПОКАЗАТЕЛИ НАГРУЗКИ СИСТЕМЫ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // CPU gauge
                                        SystemResourceGauge(
                                            value = liveCpuState,
                                            label = "ЦП Движка",
                                            color = Color(0xFF10B981)
                                        )
                                        // RAM gauge
                                        SystemResourceGauge(
                                            value = liveRamState,
                                            label = "ОЗУ Кэша",
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Beautiful Line Chart: RKN Threat Blocks Over Time (Canvas)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "БЛОКИРОВКИ УГРОЗ РКН РФ",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "Динамика предотвращения переходов на опасные ресурсы",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text("24 часа", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    ThreatsBlockedLineChart()
                                }
                            }
                        }

                        // 3. Beautiful Wave Chart: Network Bandwidth Compression (Canvas)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "СКОРОСТЬ И СЖАТИЕ ТРАФИКА (ТУРБО)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3B82F6),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "Оптимизация каналов на серверах РосСеть",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text("АКТИВНО", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    NetworkSpeedWaveChart()
                                }
                            }
                        }

                        // 4. Real-time Sovereign Filter Flow Analytics Widget
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "СУВЕРЕННЫЙ ФИЛЬТР ТРАФИКА (EasyList / RU AdList)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF59E0B),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "Статистика фильтрации рекламы и трекеров в реальном времени",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text("ОНЛАЙН", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // EasyList Russia Status Card
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("EasyList Russia 🇷🇺", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (isEasyListEnabled) "АКТИВЕН" else "ОТКЛЮЧЕН",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isEasyListEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }

                                        // RU AdList Status Card
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("RU AdList 🕵️‍♂️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (isRuAdListEnabled) "АКТИВЕН" else "ОТКЛЮЧЕН",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isRuAdListEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("ВСЕГО ЗАБЛОКИРОВАНО ЭЛЕМЕНТОВ:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                                        Text(
                                            text = "$blockedCount",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFBBF24)
                                        )
                                    }
                                }
                            }
                        }

                        // 5. Live Monospace Interactive Terminal Logging Console
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF34D399).copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ИНТЕРАКТИВНЫЙ ЖУРНАЛ АУДИТА СИСТЕМЫ",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF34D399),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Text(
                                            text = "LIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        LazyColumn(
                                            state = consoleListState,
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            items(consoleLogs) { log ->
                                                Text(
                                                    text = log,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF10B981),
                                                    lineHeight = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Баня РФ 🚫" -> {
                    // INTERACTIVE RKN EXCLUSIVE BLOCKED DOMAINS LIST MANAGER
                    var domainInput by remember { mutableStateOf("") }
                    val blockedDomainsSet by viewModel.dbBlockedDomains.collectAsState()
                    val sortedDomainsList = remember(blockedDomainsSet, searchQuery) {
                        blockedDomainsSet.filter { it.contains(searchQuery, ignoreCase = true) }.sorted()
                    }

                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        // Quick input form
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Block, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Внести запрещённый сайт в Реестр РФ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = domainInput,
                                        onValueChange = { domainInput = it },
                                        placeholder = { Text("Пример: youtube.com", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)) },
                                        singleLine = true,
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                            focusedContainerColor = Color.Black.copy(alpha = 0.15f),
                                            unfocusedContainerColor = Color.Black.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    )

                                    Button(
                                        onClick = {
                                            val cleanDomain = domainInput.trim().lowercase()
                                            if (cleanDomain.isNotEmpty()) {
                                                viewModel.addBlockedDomain(cleanDomain, context)
                                                Toast.makeText(context, "Сайт $cleanDomain заблокирован в РФ!", Toast.LENGTH_SHORT).show()
                                                domainInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp),
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Text("БАН 🚫", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls Header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ВСЕГО В РЕЕСТРЕ: ${blockedDomainsSet.size} доменов",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.5f)
                            )

                            Text(
                                text = "СИНХРОНИЗИРОВАТЬ С РКН РФ ⚡️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.clickable {
                                    viewModel.resetBlockedDomains(context)
                                    Toast.makeText(context, "Реестр синхронизирован с серверами РКН!", Toast.LENGTH_LONG).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Lazy List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            if (sortedDomainsList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Нет заблокированных доменов по запросу", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                                    }
                                }
                            } else {
                                items(sortedDomainsList) { domain ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEF4444))
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = domain,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.removeBlockedDomain(domain, context)
                                                Toast.makeText(context, "$domain успешно разблокирован в РФ", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить",
                                                tint = Color.LightGray.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // MAIN LIST FOR CONFIGURABLE PARAMETERS & DYNAMIC CONTROLS
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        
                        // Sovereign VIP Passport Widget inside "Баланс & VIP" tab
                        if (selectedCategory == "Баланс & VIP" || selectedCategory == "Все") {
                            SovereignPassportCard(balance = balance, purchasedListSize = purchasedList.size, isFsbActive = activeToggles["admin_fsb_agent_mode"] == true)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(filteredSchema) { item ->
                                val isToggled = activeToggles[item.id] ?: false

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(
                                            1.dp,
                                            if (isToggled) Color(0xFF10B981).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isToggled) Color(0xFF34D399) else Color.White
                                                )
                                                if (isToggled) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF34D399))
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.description,
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.6f),
                                                lineHeight = 13.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(item.category, fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Interactive control widget
                                        if (item.isAction) {
                                            Button(
                                                onClick = {
                                                    // Handle explicit trigger actions!
                                                    when (item.id) {
                                                        "admin_add_10k" -> {
                                                            viewModel.addBalance(10000)
                                                            Toast.makeText(context, "Начислено субсидией +10,000 ₽!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "admin_reset_balance" -> {
                                                            viewModel.addBalance(-balance) // Adjust balance to exactly 0
                                                            Toast.makeText(context, "Казна РФ очищена! Баланс выставлен на 0 ₽.", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "admin_is_oligarch" -> {
                                                            viewModel.addBalance(1000000 - balance)
                                                            Toast.makeText(context, "Выдан статус Олигарха: 1,000,000 ₽ начислено!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "admin_unlock_all_vip" -> {
                                                            val allItems = listOf(
                                                                "sub-month", "sub-year", "vip-sunset", "sochi-love", "neon-stealth",
                                                                "butterfly-trail", "sun-corona-pro", "pond-ripple-60", "fireflies-night",
                                                                "ad-blocker-pro", "tracker-blocker-pro", "gost-256", "auto-stealth",
                                                                "ros-translate", "boost-detective", "shells-clicker", "endless-lives",
                                                                "stickers-cats", "stickers-memes", "stickers-owl"
                                                            )
                                                            allItems.forEach { purchaseId ->
                                                                if (!purchasedList.contains(purchaseId)) {
                                                                    viewModel.purchaseItem(purchaseId, 0)
                                                                }
                                                            }
                                                            Toast.makeText(context, "Все 20 премиум привилегий и тем успешно разблокированы!", Toast.LENGTH_LONG).show()
                                                        }
                                                        "admin_flag_storm_speed" -> {
                                                            viewModel.setFlagWaveSpeed(15.0f, context)
                                                            Toast.makeText(context, "Сила ветра установлена на Ураган (15.0)!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "admin_flag_calm_speed" -> {
                                                            viewModel.setFlagWaveSpeed(0.0f, context)
                                                            Toast.makeText(context, "Полный штиль флага (0.0) активирован!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "admin_factory_reset_all" -> {
                                                            // Reset SharedPreferences
                                                            context.getSharedPreferences("rosbrowser_admin_pref", Context.MODE_PRIVATE).edit().clear().apply()
                                                            context.getSharedPreferences("rosbrowser_market_pref", Context.MODE_PRIVATE).edit().clear().apply()
                                                            context.getSharedPreferences("rosbrowser_profiles_pref", Context.MODE_PRIVATE).edit().clear().apply()
                                                            
                                                            viewModel.addBalance(500 - balance) // Reset back to default
                                                            viewModel.setAdminStatus(false)
                                                            viewModel.loadAdminSettings(context)
                                                            
                                                            Toast.makeText(context, "ВСЕ ДАННЫЕ ОЧИЩЕНЫ. Браузер переустановлен!", Toast.LENGTH_LONG).show()
                                                            onBack()
                                                        }
                                                    }
                                                    viewModel.saveAllDataCompletely(context)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (item.id == "admin_factory_reset_all") Color(0xFFEF4444) else Color(0xFF10B981),
                                                    contentColor = if (item.id == "admin_factory_reset_all") Color.White else Color.Black
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("action_${item.id}").height(34.dp)
                                            ) {
                                                Text("Запуск", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            // Toggle Switch
                                            Switch(
                                                checked = isToggled,
                                                onCheckedChange = { chk ->
                                                    viewModel.setAdminToggle(item.id, chk)
                                                    
                                                    // Specific toggle effects!
                                                    if (item.id == "admin_fsb_agent_mode") {
                                                        if (chk) {
                                                            sendFSBNotification(context)
                                                            Toast.makeText(context, "Канал СпецСвязи ФСБ АКТИВИРОВАН!", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "Канал СпецСвязи закрыт.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else if (item.id == "admin_anti_screenshot") {
                                                        Toast.makeText(
                                                            context,
                                                            if (chk) "Запрет скриншотов ВКЛЮЧЕН" else "Разрешены обычные снимки экрана",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(context, "Параметр сохранен!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    
                                                    viewModel.saveAllDataCompletely(context)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF10B981),
                                                    checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f),
                                                    uncheckedThumbColor = Color.LightGray,
                                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                                ),
                                                modifier = Modifier.testTag("toggle_${item.id}").scale(0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer backup saver indicator
            Text(
                text = "⚡️ Настройки синхронизируются в реальном времени с реестром Минцифры РФ",
                color = Color(0xFF10B981).copy(alpha = 0.6f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
    }
}

// System Resource Gauge Component (fluctuates dynamically)
@Composable
fun SystemResourceGauge(
    value: Float,
    label: String,
    color: Color
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(1000, easing = EaseInOutQuad),
        label = "GaugeValueAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(110.dp)
    ) {
        Box(
            modifier = Modifier.size(75.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthValue = 7.dp.toPx()
                val radius = (size.minDimension - strokeWidthValue) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // Background track arc
                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = centerOffset - Offset(radius, radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidthValue, cap = StrokeCap.Round)
                )

                // Active foreground arc with gradient glow
                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.5f))
                    ),
                    startAngle = 135f,
                    sweepAngle = (animatedValue / 100f) * 270f,
                    useCenter = false,
                    topLeft = centerOffset - Offset(radius, radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidthValue, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${animatedValue.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

// Gorgeous Line Chart: Threats Blocked (Canvas)
@Composable
fun ThreatsBlockedLineChart() {
    val points = listOf(
        Offset(0f, 15f),
        Offset(1f, 8f),
        Offset(2f, 24f),
        Offset(3f, 12f),
        Offset(4f, 45f),
        Offset(5f, 31f),
        Offset(6f, 65f),
        Offset(7f, 22f),
        Offset(8f, 10f)
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
    ) {
        val width = size.width
        val height = size.height
        val padding = 16f

        val xMin = 0f
        val xMax = 8f
        val yMin = 0f
        val yMax = 80f

        val stepX = (width - padding * 2) / (xMax - xMin)
        val stepY = (height - padding * 2) / (yMax - yMin)

        // Draw horizontal grid lines
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val y = padding + i * (height - padding * 2) / gridLinesCount
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart path
        val path = Path()
        points.forEachIndexed { index, point ->
            val cx = padding + point.x * stepX
            val cy = height - padding - point.y * stepY
            if (index == 0) {
                path.moveTo(cx, cy)
            } else {
                val prevX = padding + points[index - 1].x * stepX
                val prevY = height - padding - points[index - 1].y * stepY
                path.quadraticTo((prevX + cx) / 2f, (prevY + cy) / 2f, cx, cy)
            }
        }

        // Draw filled gradient area below path
        val fillPath = Path().apply {
            addPath(path)
            lineTo(padding + points.last().x * stepX, height - padding)
            lineTo(padding, height - padding)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEF4444).copy(alpha = 0.25f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            color = Color(0xFFF87171),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw node circles with hover effects
        points.forEachIndexed { _, point ->
            val cx = padding + point.x * stepX
            val cy = height - padding - point.y * stepY
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 3.5.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

// Gorgeous Wave Chart: Network Bandwidth Compression (Canvas)
@Composable
fun NetworkSpeedWaveChart() {
    val transition = rememberInfiniteTransition(label = "NetworkWaveTransition")
    val animOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveOffset"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
    ) {
        val width = size.width
        val height = size.height

        val wavePath1 = Path()
        val wavePath2 = Path()

        wavePath1.moveTo(0f, height * 0.5f)
        wavePath2.moveTo(0f, height * 0.6f)

        val resolution = 40
        val stepX = width / resolution

        for (i in 0..resolution) {
            val x = i * stepX
            val angle1 = (i.toFloat() / resolution) * 4 * PI.toFloat() + animOffset
            val angle2 = (i.toFloat() / resolution) * 5 * PI.toFloat() - animOffset

            val y1 = height * 0.5f + sin(angle1) * 20f
            val y2 = height * 0.55f + sin(angle2) * 12f

            wavePath1.lineTo(x, y1)
            wavePath2.lineTo(x, y2)
        }

        // Draw filled gradient for Wave 1
        val fillPath1 = Path().apply {
            addPath(wavePath1)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath1,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.2f), Color.Transparent)
            )
        )

        // Draw Wave Lines
        drawPath(
            path = wavePath1,
            color = Color(0xFF60A5FA),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = wavePath2,
            color = Color(0xFF34D399).copy(alpha = 0.4f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )
    }
}

// Sovereign VIP Passport Card - Glowing Visa-like Dashboard Widget
@Composable
fun SovereignPassportCard(balance: Int, purchasedListSize: Int, isFsbActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "PassportGlow")
    val cardRotation by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CardFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer(rotationZ = cardRotation)
            .shadow(16.dp, RoundedCornerShape(20.dp), clip = false)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Dark Royal Blue
                        Color(0xFF3B82F6), // Intense Blue
                        Color(0xFF1D4ED8), // Blue Accent
                        Color(0xFF8B5CF6)  // Cosmic Violet hue
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.1f),
                        Color(0xFFFFD54F).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        // Diagonal sweeping Tricolor flag watermark ribbon
        Canvas(modifier = Modifier.fillMaxSize()) {
            clipRect {
                val angleRad = Math.toRadians(25.0).toFloat()
                val ribbonWidth = 65.dp.toPx()
                val startX = size.width * 0.45f

                val tricolorColors = listOf(Color.White, Color(0xFF1E88E5), Color(0xFFE53935))
                tricolorColors.forEachIndexed { index, color ->
                    val offset = index * (ribbonWidth / 3f)
                    val path = Path().apply {
                        moveTo(startX + offset, -50f)
                        lineTo(startX + offset + (ribbonWidth / 3f), -50f)
                        lineTo(startX + offset + (ribbonWidth / 3f) - size.height * kotlin.math.tan(angleRad), size.height + 50f)
                        lineTo(startX + offset - size.height * kotlin.math.tan(angleRad), size.height + 50f)
                        close()
                    }
                    drawPath(path, color.copy(alpha = 0.12f))
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("РФ", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "СУВЕРЕННЫЙ VIP ПАСПОРТ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFD54F).copy(alpha = 0.22f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ГОСЗНАК РФ",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            // Central balance slot
            Column {
                Text(
                    text = "СОЮЗНЫЙ БАЛАНС:",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$balance ₽",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFEE58) // Gold sand glowing color
                )
            }

            // Card Footer / Cardholder details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "СТАТУС ПОЛЬЗОВАТЕЛЯ:",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (balance >= 1000000) "ОЛИГАРХ РОССИИ 👑" else if (purchasedListSize > 5) "VIP РЕЗИДЕНТ" else "СУВЕРЕННЫЙ ГРАЖДАНИН",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "КАСТОМИЗАЦИЯ:",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$purchasedListSize / 20 ПРИВИЛЕГИЙ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
            }
        }
    }
}
