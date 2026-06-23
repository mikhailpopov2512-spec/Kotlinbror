package com.example

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MarketItem(
    val id: String,
    val title: String,
    val category: String, // "Подписки", "Темы", "Эффекты", "Функции", "Бустеры", "Стикеры"
    val description: String,
    val price: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val highlightText: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RosMarketPage(
    viewModel: BrowserViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val balance by viewModel.userBalance.collectAsState()
    val purchasedList by viewModel.purchasedItems.collectAsState()

    var activeCategoryFilter by remember { mutableStateOf("Все") }
    var promoInputText by remember { mutableStateOf("") }
    
    // Payment dialog states
    var nominatedItemToBuy by remember { mutableStateOf<MarketItem?>(null) }
    var selectedPaymentGate by remember { mutableStateOf("SBP") } // "SBP", "RUSTORE", "YAPAY"
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccessTrigger by remember { mutableStateOf(false) }

    // List of 20 detailed digital summer accessories
    val marketInventory = remember {
        listOf(
            // Subscriptions
            MarketItem("sub-month", "РосПлюс Премиум: 1 Месяц", "Подписки", "Максимальная скорость рендеринга сайтов, приоритетная локальная фильтрация рекламы и новые мотивы для свайпов.", 199, Icons.Default.CardMembership, "ХИТ продаж"),
            MarketItem("sub-year", "РосПлюс Премиум: 1 Год", "Подписки", "Годовая подписка на РосПлюс со скидкой 50%. Включает безлимитный трафик ГОСТ-TLS.", 999, Icons.Default.Stars, "-50% Выгода"),
            
            // Themes
            MarketItem("vip-sunset", "VIP Летняя тема «Sunset Dream»", "Темы", "Эксклюзивная палитра малинового заката, золотых пляжей и мягких переливов в настройках браузера.", 149, Icons.Default.Palette),
            MarketItem("sochi-love", "Розовая тема «Свидание в Сочи»", "Темы", "Нежно-розовые акценты сочинских магнолий и ракушек для романтического морского настроения.", 99, Icons.Default.Favorite),
            MarketItem("neon-stealth", "Космический Неон «Stealth-PRO»", "Темы", "Полностью контрастный ночной режим с ядовито-зелеными линиями и бегущими пикселями.", 199, Icons.Default.ModeNight),
            
            // Effects
            MarketItem("butterfly-trail", "Светящийся след бабочки", "Эффекты", "След из сияющей золотистой пыльцы за испуганными бабочками при нажатии на экран.", 49, Icons.Default.Flare),
            MarketItem("sun-corona-pro", "Золотые лучи солнца PRO", "Эффекты", "Удваивает количество лучей нашего летнего солнца, добавляет мягкое радужное гало к короне.", 79, Icons.Default.BrightnessHigh, "Рекомендуем"),
            MarketItem("pond-ripple-60", "Процедурная рябь пруда 60 FPS", "Эффекты", "Физически достоверная трехкратная симуляция интерференции волн на воде.", 59, Icons.Default.Water),
            MarketItem("fireflies-night", "Мерцающие светлячки ночью", "Эффекты", "Множество маленьких зеленых огоньков, взмывающих ввысь при переходе в ночной режим.", 69, Icons.Default.WbIridescent),
            
            // Features
            MarketItem("ad-blocker-pro", "Супер-блокиратор рекламы PRO", "Функции", "Блокирует 99.8% навязчивых баннеров, видеовставок и всплывающих окон по списку РКН.", 149, Icons.Default.Shield, "Супер защита"),
            MarketItem("tracker-blocker-pro", "Супер-блокиратор трекеров ФСБ", "Функции", "Предотвращает сбор аналитики сторонними зарубежными сервисами слежки.", 119, Icons.Default.VisibilityOff),
            MarketItem("gost-256", "Шифрование ГОСТ-TLS 256-бит", "Функции", "Аппаратное шифрование сессий для защиты онлайн-банкинга на территории РФ.", 249, Icons.Default.VpnKey),
            MarketItem("auto-stealth", "Stealth автоматическое скрытие", "Функции", "Мгновенно очищает историю и вводит Stealth-режим при неактивности более 2 минут.", 129, Icons.Default.Timer),
            MarketItem("ros-translate", "Умный РосПереводчик", "Функции", "Быстрый суверенный переводчик веб-страниц без отправки данных за рубеж.", 99, Icons.Default.Translate),
            
            // Boosters
            MarketItem("boost-detective", "Бустер улик в «РосДетективе»", "Бустеры", "Удваивает получаемый опыт и подсвечивает секреты в суверенной мини-игре.", 49, Icons.Default.Search),
            MarketItem("shells-clicker", "Золотые ракушки в кликере", "Бустеры", "Дает 10,000 золотых ракушек в пляжном симуляторе для моментального улучшения курорта.", 49, Icons.Default.AccountBalanceWallet),
            MarketItem("endless-lives", "Бесконечные жизни в «ЛетоЗабег»", "Бустеры", "Бессмертие пляжного краба при преодолении препятствий под летним солнцем.", 99, Icons.Default.FavoriteBorder),
            
            // Stickers
            MarketItem("stickers-cats", "Стикерпак «Летние котики в Анапе»", "Стикеры", "16 эксклюзивных анимированных наклеек с пушистыми котейками на пляже.", 29, Icons.Default.Mood, "Новинка"),
            MarketItem("stickers-memes", "Стикерпак «Пляжные мемы РФ 2026»", "Стикеры", "Самые свежие суверенные юмористические шаблоны пляжного сезона в высоком разрешении.", 29, Icons.Default.AddReaction, "ТОП"),
            MarketItem("stickers-owl", "Стикерпак «Суверенная сова»", "Стикеры", "Поучительные стикеры умной совы для использования в чатах и закладках.", 29, Icons.Default.Face)
        )
    }

    val categories = listOf("Все", "Подписки", "Темы", "Эффекты", "Функции", "Бустеры", "Стикеры")
    val filteredCatalog = remember(activeCategoryFilter, marketInventory) {
        if (activeCategoryFilter == "Все") marketInventory else marketInventory.filter { it.category == activeCategoryFilter }
    }

    // Modern layout utilizing beautiful glass gradients and generous margins
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark cosmic space
                        Color(0xFF0F766E)  // Dark ocean pine green
                    )
                )
            )
    ) {
        // Core layout container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // A. Header Row with back arrow button
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
                        .testTag("market_back_button")
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
                        text = "РосМаркет 🇷🇺",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = "Суверенные летние привилегии и стили",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // B. Balance Board Box (Golden sand glassmorphism)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Ваш Баланс в РФ кошельке:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "$balance ₽",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            )
                        )
                    }

                    // Balance controls or actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.addBalance(250)
                                Toast.makeText(context, "Начислено +250 ₽ на баланс!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Пополнить 250", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // C. Yandex Cloud sync restorations and Promo activations
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Promo-input box
                OutlinedTextField(
                    value = promoInputText,
                    onValueChange = { promoInputText = it },
                    placeholder = { Text("Промокод (admin, letorussia)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("market_promo_input"),
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D9488),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (promoInputText.trim().isNotEmpty()) {
                            val msg = viewModel.usePromoCode(promoInputText)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            promoInputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE185D)),
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ввод", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Yandex ID restore action
            Button(
                onClick = {
                    val msg = viewModel.restoreYandexPurchases(context)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudDownload, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Восстановить покупки из Yandex ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // D. Categories Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = activeCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSel) Color(0xFFFFBB00) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSel) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { activeCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // E. Grid List of 20 elements
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredCatalog) { item ->
                    val isPurchased = purchasedList.contains(item.id)

                    // Card Item Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (isPurchased) Color(0xFF0369A1).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.10f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular icon category
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(
                                        if (isPurchased) Color(0xFF0284C7).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isPurchased) Color(0xFF38BDF8) else Color(0xFFFFD54F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.highlightText.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE11D48), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                item.highlightText,
                                                fontSize = 8.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = item.description,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Category badge
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            item.category,
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (isPurchased) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(10.dp))
                                            Text("Активировано", fontSize = 9.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Action button / Price tag
                            Button(
                                onClick = {
                                    if (!isPurchased) {
                                        nominatedItemToBuy = item
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPurchased) Color.Transparent else Color(0xFFFFB300),
                                    contentColor = if (isPurchased) Color.White.copy(alpha = 0.4f) else Color.Black
                                ),
                                enabled = !isPurchased,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("item_buy_btn_${item.id}")
                            ) {
                                Text(
                                    text = if (isPurchased) "Есть" else "${item.price} ₽",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================== DIALOG: PAYMENT SELECTION AND PROCESSING SIMULATOR ==================
        if (nominatedItemToBuy != null) {
            val item = nominatedItemToBuy!!
            AlertDialog(
                onDismissRequest = {
                    if (!isProcessingPayment) {
                        nominatedItemToBuy = null
                        paymentSuccessTrigger = false
                    }
                },
                title = {
                    Text(
                        text = if (paymentSuccessTrigger) "Оплата Успешна!" else "Выбор способа оплаты",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!paymentSuccessTrigger) {
                            Text(
                                "Вы приобретаете: ",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text(item.description, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("К оплате: ${item.price} ₽", fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F), fontSize = 14.sp)
                                }
                            }

                            Text("Выберите суверенную кассу оплаты:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)

                            // SBP options
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPaymentGate == "SBP") Color(0xFF0F766E).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (selectedPaymentGate == "SBP") Color(0xFF0D9488) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedPaymentGate = "SBP" }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalance, null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Система Быстрых Платежей (СБП)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Оплата через мобильное приложение любого банка РФ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }

                            // RuStore options
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPaymentGate == "RUSTORE") Color(0xFF0F766E).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (selectedPaymentGate == "RUSTORE") Color(0xFF0D9488) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedPaymentGate = "RUSTORE" }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Storefront, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("RuStore Billing Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Оплата со счета мобильного телефона или банковской картой", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }

                            // Yandex Pay options
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPaymentGate == "YAPAY") Color(0xFF0F766E).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (selectedPaymentGate == "YAPAY") Color(0xFF0D9488) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedPaymentGate = "YAPAY" }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CreditCard, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Yandex Pay 🔴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Списание со связанного паспорта Яндекс ID с кэшбэком", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }

                            if (isProcessingPayment) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Защищенная транзакция ГОСТ-34.12...", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        } else {
                            // Success container
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color(0xFF22C55E).copy(alpha = 0.2f), CircleShape)
                                        .border(2.dp, Color(0xFF22C55E), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(40.dp))
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Товар Успешно Активирован!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(
                                    "Вы приобрели «${item.title}». Все функции внедрены в ядро вашего РосБраузера.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (!paymentSuccessTrigger) {
                        Button(
                            onClick = {
                                if (!isProcessingPayment) {
                                    coroutineScope.launch {
                                        isProcessingPayment = true
                                        delay(1800) // Realistic delay
                                        isProcessingPayment = false
                                        
                                        // Execute purchase
                                        val res = viewModel.purchaseItem(item.id, item.price)
                                        Toast.makeText(context, res, Toast.LENGTH_LONG).show()

                                        if (res.startsWith("Поздравляем")) {
                                            paymentSuccessTrigger = true
                                        } else {
                                            nominatedItemToBuy = null // cancel
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            enabled = !isProcessingPayment,
                            modifier = Modifier.testTag("payment_execute_btn")
                        ) {
                            Text("Оплатить ${item.price} ₽", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                nominatedItemToBuy = null
                                paymentSuccessTrigger = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Text("Отлично", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    if (!isProcessingPayment && !paymentSuccessTrigger) {
                        TextButton(onClick = { nominatedItemToBuy = null }) {
                            Text("Отмена", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}
