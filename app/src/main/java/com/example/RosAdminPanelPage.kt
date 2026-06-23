package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class AdminConfigItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Баланс & VIP", "Безопасность & ФСБ", "Игры & Бустеры", "Кастомизация", "Оптимизация"
    val isAction: Boolean = false,
    val initialLabelValue: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RosAdminPanelPage(
    viewModel: BrowserViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val balance by viewModel.userBalance.collectAsState()
    val purchasedList by viewModel.purchasedItems.collectAsState()
    val activeToggles by viewModel.adminToggles.collectAsState()
    val activeFloats by viewModel.adminFloats.collectAsState()
    val activeTexts by viewModel.adminTexts.collectAsState()

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

    val categories = listOf("Все", "Баланс & VIP", "Безопасность & ФСБ", "Игры & Бустеры", "Кастомизация", "Оптимизация")

    // Filter items based on search query and category tab
    val filteredSchema = remember(searchQuery, selectedCategory, adminSchema) {
        adminSchema.filter { item ->
            val matchCat = selectedCategory == "Все" || item.category == selectedCategory
            val matchSearch = searchQuery.isEmpty() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark slate
                        Color(0xFF023E8A), // Rich ocean blue
                        Color(0xFF00FF66).copy(alpha = 0.05f) // Matrix glow
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .testTag("admin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "ПАНЕЛЬ УПРАВЛЕНИЯ v2.0 ⚙️",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF00FF66),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Конфигурация 40 суверенных функций РосБраузера",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Quick Stats Bar (Glass design)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color(0xFF00FF66).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("РФ Кошелек:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("$balance ₽", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Режим СпецУправление:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text(
                            text = if (activeToggles["admin_fsb_agent_mode"] == true) "ФСБ АКТИВНО" else "НОРМАЛЬНЫЙ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeToggles["admin_fsb_agent_mode"] == true) Color.Red else Color(0xFF00FF66)
                        )
                    }
                    Column {
                        Text("Куплено:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${purchasedList.size} привилегий", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Filter Rows
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск среди 40 параметров управления...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_search_input"),
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FF66),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Flow Scroll inside Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) Color(0xFF00FF66) else Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 40 List of parameters and interactive settings
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredSchema) { item ->
                    val isToggled = activeToggles[item.id] ?: false

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isToggled) Color(0xFF00FF66).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToggled) Color(0xFF00FF66) else Color.White
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
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
                                                val p = context.applicationContext.getSharedPreferences("rosbrowser_market_pref", Context.MODE_PRIVATE)
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
                                                        // Bypass purchase check and inject direct
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
                                                
                                                Toast.makeText(context, "ВСЕ ДАННЫЕ ОЧИЩЕНЫ. Приложение возвращено к заводскому состоянию!", Toast.LENGTH_LONG).show()
                                                onBack()
                                            }
                                        }
                                        // Save changed stats completely immediately!
                                        viewModel.saveAllDataCompletely(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (item.id == "admin_factory_reset_all") Color(0xFFEF4444) else Color(0xFF00FF66),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("action_${item.id}").height(32.dp)
                                ) {
                                    Text("Запуск", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                            Toast.makeText(context, "Параметр изменен!", Toast.LENGTH_SHORT).show()
                                        }
                                        
                                        // Ensure all states are instantly saved to SharedPreferences!
                                        viewModel.saveAllDataCompletely(context)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF00FF66),
                                        checkedTrackColor = Color(0xFF00FF66).copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.testTag("toggle_${item.id}").scale(0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer backup saver indicator
            Text(
                text = "⚡️ Все настройки сохраняются мгновенно и страхуются при выходе",
                color = Color(0xFF00FF66).copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Scaled using import direct
