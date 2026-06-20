package com.example

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

enum class BrowserMode {
    REGULAR,
    INCOGNITO,
    GUEST,
    KIDS,
    STEALTH
}

data class PebbleShortcut(
    val id: String,
    val title: String,
    val url: String,
    val isYandexService: Boolean = false
)

data class DzenArticle(
    val id: Int,
    val title: String,
    val category: String,
    val summary: String,
    val imageUrl: String,
    val dismissState: String = "visible" // "visible", "seagull" (dismissed), "later"
)

data class SearchResult(
    val title: String,
    val snippet: String,
    val url: String
)

class BrowserViewModel : ViewModel() {

    // URL and search state
    private val _currentUrl = MutableStateFlow("")
    val currentUrl = _currentUrl.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _webViewProgress = MutableStateFlow(0f)
    val webViewProgress = _webViewProgress.asStateFlow()

    private val _isWebViewLoading = MutableStateFlow(false)
    val isWebViewLoading = _isWebViewLoading.asStateFlow()

    // Mode state
    private val _browserMode = MutableStateFlow(BrowserMode.REGULAR)
    val browserMode = _browserMode.asStateFlow()

    // Screen and overlay variables
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward = _canGoForward.asStateFlow()

    // Embedded Blocklist (RKN)
    val blocklistedDomains = listOf(
        "instagram.com", "facebook.com", "twitter.com", "blocked.ru", "prohibited.org", "rkn-ban.com"
    )

    // Beach pebble shortcuts (oval shaped, sand color)
    private val _shortcuts = MutableStateFlow<List<PebbleShortcut>>(emptyList())
    val shortcuts = _shortcuts.asStateFlow()

    // Widgets states
    private val _trafficScore = MutableStateFlow(4) // 0 to 10
    val trafficScore = _trafficScore.asStateFlow()

    private val _weatherTemp = MutableStateFlow(24)
    val weatherTemp = _weatherTemp.asStateFlow()

    private val _weatherCondition = MutableStateFlow("Солнечно") // "Солнечно", "Облачно", "Дождь"
    val weatherCondition = _weatherCondition.asStateFlow()

    private val _isCoinSpinning = MutableStateFlow(false)
    val isCoinSpinning = _isCoinSpinning.asStateFlow()

    private val _rubToUsd = MutableStateFlow(92.40)
    val rubToUsd = _rubToUsd.asStateFlow()

    private val _rubToEur = MutableStateFlow(99.15)
    val rubToEur = _rubToEur.asStateFlow()

    private val _rubToCny = MutableStateFlow(12.75)
    val rubToCny = _rubToCny.asStateFlow()

    // Dzen News Feed
    private val _dzenFeed = MutableStateFlow<List<DzenArticle>>(emptyList())
    val dzenFeed = _dzenFeed.asStateFlow()

    // RosPoisk engine state
    private val _searchQueryResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchQueryResults = _searchQueryResults.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading = _isSearchLoading.asStateFlow()

    // History and suggestions
    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history = _history.asStateFlow()

    init {
        resetShortcuts()
        loadDefaultDzenFeed()
        _history.update { listOf("https://yandex.ru", "https://gosuslugi.ru", "https://rustore.ru", "https://gost-crypto.ru") }
    }

    fun setUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            _currentUrl.value = ""
            return
        }

        // Check if it's a blocked word/address inKids mode
        if (_browserMode.value == BrowserMode.KIDS) {
            val isWhiteListed = checkIfKidsFriendly(trimmed)
            if (!isWhiteListed) {
                _currentUrl.value = "chrome-native://blocked"
                return
            }
        }

        // Check if we should block (RKN)
        if (isBlocklisted(trimmed)) {
            _currentUrl.value = "chrome-native://blocked"
            return
        }

        // Is it chrome-native?
        if (trimmed == "chrome-native://newtab" || trimmed == "about:blank") {
            _currentUrl.value = ""
            return
        }
        if (trimmed.startsWith("chrome-native://")) {
            _currentUrl.value = trimmed
            return
        }

        // Treat search if it does not look like a domain URL
        if (isSearchQuery(trimmed)) {
            performRosPoisk(trimmed)
            return
        }

        // Formulate correct URL
        var finalUrl = trimmed
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        _currentUrl.value = finalUrl

        // Save history in regular/kids modes
        if (_browserMode.value != BrowserMode.INCOGNITO && _browserMode.value != BrowserMode.STEALTH && _browserMode.value != BrowserMode.GUEST) {
            if (!_history.value.contains(finalUrl)) {
                _history.update { (listOf(finalUrl) + it).take(15) }
            }
        }
    }

    private fun isSearchQuery(trimmed: String): Boolean {
        if (trimmed.contains(".") && !trimmed.contains(" ") && !trimmed.endsWith(".")) return false
        return true
    }

    fun changeMode(mode: BrowserMode) {
        _browserMode.value = mode
        if (mode == BrowserMode.GUEST) {
            // Tempest profile: washed by waves
            resetShortcuts()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setWebViewLoading(loading: Boolean) {
        _isWebViewLoading.value = loading
    }

    fun setWebViewProgress(progress: Float) {
        _webViewProgress.value = progress
    }

    fun setCanGoBack(can: Boolean) {
        _canGoBack.value = can
    }

    fun setCanGoForward(can: Boolean) {
        _canGoForward.value = can
    }

    // Checking blocklists
    fun isBlocklisted(url: String): Boolean {
        val cleanUrl = url.lowercase()
        return blocklistedDomains.any { cleanUrl.contains(it) }
    }

    // Kids friendly white list
    fun checkIfKidsFriendly(url: String): Boolean {
        val cleanUrl = url.lowercase()
        val whitelist = listOf("gosuslugi", "rustore", "kids", "disney", "smeshariki", "yandex", "dolphins", "mult", "deti")
        return whitelist.any { cleanUrl.contains(it) } || url.isEmpty() || url.startsWith("chrome-native://")
    }

    // Widgets activities
    fun setTrafficScore(score: Int) {
        _trafficScore.value = score.coerceIn(0, 10)
    }

    fun hitTrafficLight() {
        val current = _trafficScore.value
        if (current >= 10) {
            _trafficScore.value = 1 // reset
        } else {
            _trafficScore.value = current + 3
        }
    }

    fun spinCoin() {
        _isCoinSpinning.value = true
        // update rates slightly
        _rubToUsd.value += Random.nextDouble(-0.5, 0.5)
        _rubToEur.value += Random.nextDouble(-0.6, 0.6)
        _rubToCny.value += Random.nextDouble(-0.1, 0.1)
    }

    fun stopCoinSpinning() {
        _isCoinSpinning.value = false
    }

    fun incrementWeather() {
        _weatherTemp.value = (_weatherTemp.value + 1)
        if (_weatherTemp.value > 38) _weatherTemp.value = 16
        val conds = listOf("Солнечно", "Облачно", "Дождь")
        _weatherCondition.value = conds[Random.nextInt(conds.size)]
    }

    // Sea Pebbles Action
    fun addShortcut(title: String, url: String) {
        val newShort = PebbleShortcut(
            id = System.currentTimeMillis().toString(),
            title = title,
            url = url
        )
        _shortcuts.update { it + newShort }
    }

    fun deleteShortcut(id: String) {
        _shortcuts.update { list -> list.filter { it.id != id } }
    }

    fun resetShortcuts() {
        _shortcuts.value = listOf(
            PebbleShortcut("ya", "Яндекс", "https://yandex.ru", isYandexService = true),
            PebbleShortcut("gu", "Госуслуги", "https://gosuslugi.ru"),
            PebbleShortcut("ru", "RuStore", "https://rustore.ru"),
            PebbleShortcut("vk", "ВКонтакте", "https://vk.com"),
            PebbleShortcut("ya-map", "Яндекс.Карты", "https://yandex.ru/maps", isYandexService = true),
            PebbleShortcut("ya-music", "Яндекс.Музыка", "https://music.yandex.ru", isYandexService = true),
            PebbleShortcut("ya-market", "Яндекс.Маркет", "https://market.yandex.ru", isYandexService = true),
            PebbleShortcut("wiki", "Рувики", "https://ru.ruwiki.ru")
        )
    }

    // RosPoisk executing
    fun performRosPoisk(query: String) {
        _searchQuery.value = query
        _isSearchLoading.value = true
        _currentUrl.value = "chrome-native://rossearx"

        // Simulated results with high visual details
        val simulatedResults = listOf(
            SearchResult(
                title = "Красные цветущие маки России: Летние пейзажи",
                snippet = "Поля цветущих диких маков раскинулись на юге России. Как доехать до лучших полей Крыма и Краснодарского края летом 2026 года.",
                url = "https://nature-russia.ru/poppy-fields-summer-2026"
            ),
            SearchResult(
                title = "РосБраузер (RosBrowser) — Современный импортозамещенный движок Blink",
                snippet = "Стабильный Android веб—клиент с ГОСТ—шифрованием, летним дизайном, виджетами со спасательными буйками и блокировкой запрещенных сайтов.",
                url = "https://rosbrowser.ru/features-and-downloads"
            ),
            SearchResult(
                title = "Единый Реестр РКН — Проверить статус блокировки сайтов",
                snippet = "Официальный сайт Федеральной службы по надзору в сфере связи, информационных технологий и массовых коммуникаций. Статьи 149-ФЗ о блокировках.",
                url = "https://rkn.gov.ru/blocking-checker"
            ),
            SearchResult(
                title = "Яндекс Маркет — Летняя распродажа товаров для пляжного отдыха",
                snippet = "Скидки до 70% на надувные матрасы, плавательные круги, крутые бочонки, кремы SPF и пляжные лежаки. Доставка в пункты выдачи.",
                url = "https://market.yandex.ru/summer-sale-beach-items"
            ),
            SearchResult(
                title = "Сертификаты Минцифры России — Как установить в браузер",
                snippet = "Гид по установке корневых и выпускающих сертификатов НУЦ РФ для стабильной работы Госуслуг и российских банковских систем со встроенным ГОСТом.",
                url = "https://digital.gov.ru/ru/activity/nuc-root-certs"
            )
        )

        // Filter based on query if it contains keywords to make it look super functional!
        val filtered = if (query.lowercase().contains("мак") || query.lowercase().contains("цве") || query.lowercase().contains("summer")) {
            simulatedResults.filter { it.title.contains("маки") || it.snippet.contains("маков") }
        } else if (query.lowercase().contains("гост") || query.lowercase().contains("серт")) {
            simulatedResults.filter { it.title.contains("Сертификаты") || it.title.contains("РосБраузер") }
        } else if (query.lowercase().contains("ркн") || query.lowercase().contains("блоки")) {
            simulatedResults.filter { it.title.contains("Реестр") || it.title.contains("РосБраузер") }
        } else {
            simulatedResults
        }

        _searchQueryResults.value = if (filtered.isEmpty()) simulatedResults else filtered
        _isSearchLoading.value = false
    }

    // Dzen News Feed setup
    private fun loadDefaultDzenFeed() {
        _dzenFeed.value = listOf(
            DzenArticle(
                id = 1,
                title = "Летняя магия полей: Почему поле внизу экрана украшено маками?",
                category = "Путешествия",
                summary = "Цветущее поле маков — символ южного русского лета. Эти яркие цветы покачиваются на легком теплом ветру и создают основу нашей летней триколорной темы.",
                imageUrl = "https://images.unsplash.com/photo-1500627869374-13cd993b1115"
            ),
            DzenArticle(
                id = 2,
                title = "Эффект Стеклянного Стекла (Glassmorphic Interface) в деталях",
                category = "Технологии",
                summary = "РосБраузер использует размытие Гаусса 24dp с градиентным бордером. В сочетании с динамическим живым фоном это придает интерфейсу летнюю прозрачность и легкость.",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe"
            ),
            DzenArticle(
                id = 3,
                title = "РосПоиск запускает автономный индексатор сайтов",
                category = "Сделано в России",
                summary = "Новая поисковая система обещает мгновенный асинхронный ответ с таймаутом в 3 секунды, защиту от слежки сторонних трекеров и чистую выдачу важных ресурсов.",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa"
            ),
            DzenArticle(
                id = 4,
                title = "Спецрежим Stealth: Как защитить сессию в один клик",
                category = "Безопасность",
                summary = "Новый режим делает экран браузера абсолютно черным с неоновыми индикаторами, активирует FLAG_SECURE (защиту от скриншотов) и стирает кэш при сворачивании.",
                imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5"
            )
        )
    }

    fun dismissArticleSeagull(articleId: Int) {
        _dzenFeed.update { feed ->
            feed.map {
                if (it.id == articleId) it.copy(dismissState = "seagull") else it
            }
        }
    }

    fun deferArticleLater(articleId: Int) {
        _dzenFeed.update { feed ->
            feed.map {
                if (it.id == articleId) it.copy(dismissState = "later") else it
            }
        }
    }

    fun reloadDzenFeed() {
        _dzenFeed.update { feed ->
            feed.map { it.copy(dismissState = "visible") }
        }
    }
}
