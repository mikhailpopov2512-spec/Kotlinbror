package com.example

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    // Profile Management System
    private val _isTrackerBlockingEnabled = MutableStateFlow(true)
    val isTrackerBlockingEnabled = _isTrackerBlockingEnabled.asStateFlow()

    private val _profiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val profiles = _profiles.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile = _currentProfile.asStateFlow()

    private var profilePersistence: UserProfilePersistence? = null
    private var appContext: Context? = null

    fun initPersistence(context: Context) {
        appContext = context.applicationContext
        if (profilePersistence != null) return
        val persistence = UserProfilePersistence(context.applicationContext)
        profilePersistence = persistence

        var loaded = persistence.loadProfiles()
        if (loaded.isEmpty()) {
            loaded = listOf(
                UserProfile(
                    id = "sahalin",
                    name = "Сахалин (Основной)",
                    avatarColor = "#FF388E3C",
                    shortcuts = defaultShortcutsList(),
                    history = listOf("https://yandex.ru", "https://gosuslugi.ru"),
                    filterLevel = "Рекомендуемая"
                ),
                UserProfile(
                    id = "baikal",
                    name = "Байкал (Рабочий)",
                    avatarColor = "#FF1E88E5",
                    shortcuts = defaultShortcutsList().filter { !it.isYandexService },
                    history = listOf("https://gosuslugi.ru", "https://ru.ruwiki.ru"),
                    filterLevel = "Максимальная"
                ),
                UserProfile(
                    id = "altai",
                    name = "Алтай (Семейный)",
                    avatarColor = "#FFD84315",
                    shortcuts = defaultShortcutsList().take(4),
                    history = listOf("https://rustore.ru"),
                    filterLevel = "Строгая"
                )
            )
            persistence.saveProfiles(loaded)
        }

        _profiles.value = loaded
        val activeId = persistence.loadActiveProfileId() ?: "sahalin"
        val active = loaded.find { it.id == activeId } ?: loaded.firstOrNull()
        if (active != null) {
            switchProfile(active.id, context)
        }

        // Load custom advanced settings
        _isSummerBgAnimEnabled.value = persistence.loadSummerBgAnimEnabled()
        _flagWaveSpeed.value = persistence.loadFlagWaveSpeed()
        _searchEnginePreset.value = persistence.loadSearchEnginePreset()
        _fontSizeScale.value = persistence.loadFontSizeScale()
        _isHapticVibeEnabled.value = persistence.loadHapticVibeEnabled()

        // Load balance & purchased items
        val marketPrefs = context.applicationContext.getSharedPreferences("rosbrowser_market_pref", Context.MODE_PRIVATE)
        _userBalance.value = marketPrefs.getInt("user_balance", 500)
        val purchasedSaved = marketPrefs.getStringSet("purchased_items", null)
        if (purchasedSaved != null) {
            _purchasedItems.value = purchasedSaved.toList()
        }
    }

    fun switchProfile(profileId: String, context: Context) {
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        persistence.saveActiveProfileId(profileId)
        val target = _profiles.value.find { it.id == profileId } ?: _profiles.value.firstOrNull() ?: return

        _currentProfile.value = target

        // Restore target states to VM flows
        _history.value = target.history
        _shortcuts.value = target.shortcuts
        _filterLevel.value = target.filterLevel
        _isAdBlockActive.value = target.isAdBlockActive
        _isTrackerBlockingEnabled.value = target.isTrackerBlockingEnabled
        _isLoggedInYandex.value = target.isLoggedInYandex
        _yandexUsername.value = target.yandexUsername
        _browserMode.value = target.browserMode
        _bookmarksList.value = target.bookmarks

        // Partition simulation: clean up web cookies on switch
        try {
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()
        } catch (e: Throwable) {
            // Safe fallback
        }
    }

    fun saveCurrentProfileState(context: Context) {
        val current = _currentProfile.value ?: return
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        val updated = current.copy(
            history = _history.value,
            shortcuts = _shortcuts.value,
            filterLevel = _filterLevel.value,
            isAdBlockActive = _isAdBlockActive.value,
            isTrackerBlockingEnabled = _isTrackerBlockingEnabled.value,
            isLoggedInYandex = _isLoggedInYandex.value,
            yandexUsername = _yandexUsername.value,
            browserMode = _browserMode.value,
            bookmarks = _bookmarksList.value
        )
        _currentProfile.value = updated
        val list = _profiles.value.map { p -> if (p.id == updated.id) updated else p }
        _profiles.value = list
        
        viewModelScope.launch(Dispatchers.IO) {
            persistence.saveProfiles(list)
        }
    }

    fun saveCurrentProfileState() {
        val context = appContext ?: return
        saveCurrentProfileState(context)
    }

    fun createProfile(name: String, avatarColor: String, context: Context) {
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        val newProfile = UserProfile(
            id = "profile_" + System.currentTimeMillis(),
            name = name,
            avatarColor = avatarColor,
            shortcuts = defaultShortcutsList(),
            history = listOf("https://yandex.ru"),
            filterLevel = "Рекомендуемая"
        )
        val newList = _profiles.value + newProfile
        _profiles.value = newList
        persistence.saveProfiles(newList)
        switchProfile(newProfile.id, context)
    }

    fun deleteProfile(profileId: String, context: Context) {
        if (_profiles.value.size <= 1) return
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        val newList = _profiles.value.filter { it.id != profileId }
        _profiles.value = newList
        persistence.saveProfiles(newList)
        if (_currentProfile.value?.id == profileId) {
            newList.firstOrNull()?.let {
                switchProfile(it.id, context)
            }
        }
    }

    fun setTrackerBlockingEnabled(enabled: Boolean, context: Context) {
        _isTrackerBlockingEnabled.value = enabled
        saveCurrentProfileState(context)
    }

    fun defaultShortcutsList(): List<PebbleShortcut> {
        return listOf(
            PebbleShortcut("rosmarket", "РосМаркет 🌸", "chrome-native://market"),
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

        val lower = trimmed.lowercase()
        if (lower == "rosmarket" || lower == "market" || lower == "chrome-native://market" || lower == "market://") {
            _currentUrl.value = "chrome-native://market"
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
            val engine = _searchEnginePreset.value
            if (engine == "RosPoisk") {
                performRosPoisk(trimmed)
            } else {
                try {
                    val encoded = java.net.URLEncoder.encode(trimmed, "UTF-8")
                    val targetSearchUrl = when (engine) {
                        "Yandex" -> "https://yandex.ru/search/?text=$encoded"
                        "Google" -> "https://www.google.com/search?q=$encoded"
                        "Sputnik" -> "https://sputnik.ru/search?q=$encoded"
                        "Mail.ru" -> "https://go.mail.ru/search?q=$encoded"
                        else -> "https://yandex.ru/search/?text=$encoded"
                    }
                    _currentUrl.value = targetSearchUrl
                } catch (e: Exception) {
                    performRosPoisk(trimmed)
                }
            }
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
                saveCurrentProfileState()
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
        saveCurrentProfileState()
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

    // Modern tracking variables
    private val _connectionSecurityStatus = MutableStateFlow("Защищено (ГОСТ TLS / Protect)")
    val connectionSecurityStatus = _connectionSecurityStatus.asStateFlow()

    private val _blockedDomainsCount = MutableStateFlow(142)
    val blockedDomainsCount = _blockedDomainsCount.asStateFlow()

    private val _filterLevel = MutableStateFlow("Рекомендуемая") // "Слабая", "Рекомендуемая", "Максимальная", "Строгая"
    val filterLevel = _filterLevel.asStateFlow()

    private val _isBiometricsEnabled = MutableStateFlow(true)
    val isBiometricsEnabled = _isBiometricsEnabled.asStateFlow()

    private val _isPasswordManagerUnlocked = MutableStateFlow(false)
    val isPasswordManagerUnlocked = _isPasswordManagerUnlocked.asStateFlow()

    // Dynamic Live Background Animations & Custom Settings Properties
    private val _isSummerBgAnimEnabled = MutableStateFlow(true)
    val isSummerBgAnimEnabled = _isSummerBgAnimEnabled.asStateFlow()

    private val _flagWaveSpeed = MutableStateFlow(1.0f)
    val flagWaveSpeed = _flagWaveSpeed.asStateFlow()

    private val _searchEnginePreset = MutableStateFlow("RosPoisk")
    val searchEnginePreset = _searchEnginePreset.asStateFlow()

    private val _fontSizeScale = MutableStateFlow(1.0f)
    val fontSizeScale = _fontSizeScale.asStateFlow()

    private val _isHapticVibeEnabled = MutableStateFlow(true)
    val isHapticVibeEnabled = _isHapticVibeEnabled.asStateFlow()

    private val _isLoggedInYandex = MutableStateFlow(false)
    val isLoggedInYandex = _isLoggedInYandex.asStateFlow()

    private val _yandexUsername = MutableStateFlow("")
    val yandexUsername = _yandexUsername.asStateFlow()

    private val _isAdBlockActive = MutableStateFlow(true)
    val isAdBlockActive = _isAdBlockActive.asStateFlow()

    private val _isEasyListRussiaEnabled = MutableStateFlow(true)
    val isEasyListRussiaEnabled = _isEasyListRussiaEnabled.asStateFlow()

    private val _isRuAdListEnabled = MutableStateFlow(true)
    val isRuAdListEnabled = _isRuAdListEnabled.asStateFlow()

    // Setter and toggle functions
    fun setFilterLevel(level: String, context: Context) {
        _filterLevel.value = level
        saveCurrentProfileState(context)
    }

    fun setBiometricsEnabled(enabled: Boolean, context: Context) {
        _isBiometricsEnabled.value = enabled
        if (!enabled) _isPasswordManagerUnlocked.value = false
        saveCurrentProfileState(context)
    }

    fun setPasswordManagerUnlocked(unlocked: Boolean) {
        _isPasswordManagerUnlocked.value = unlocked
    }

    fun logInYandex(username: String, context: Context) {
        if (username.isNotBlank()) {
            _yandexUsername.value = username
            _isLoggedInYandex.value = true
            saveCurrentProfileState(context)
        }
    }

    fun logOutYandex(context: Context) {
        _yandexUsername.value = ""
        _isLoggedInYandex.value = false
        saveCurrentProfileState(context)
    }

    fun setAdBlockActive(active: Boolean, context: Context) {
        _isAdBlockActive.value = active
        saveCurrentProfileState(context)
    }

    fun setEasyListRussiaEnabled(enabled: Boolean) {
        _isEasyListRussiaEnabled.value = enabled
    }

    fun setRuAdListEnabled(enabled: Boolean) {
        _isRuAdListEnabled.value = enabled
    }

    fun setSummerBgAnimEnabled(enabled: Boolean, context: Context) {
        _isSummerBgAnimEnabled.value = enabled
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        persistence.saveSummerBgAnimEnabled(enabled)
    }

    fun setFlagWaveSpeed(speed: Float, context: Context) {
        _flagWaveSpeed.value = speed
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        persistence.saveFlagWaveSpeed(speed)
    }

    fun setSearchEnginePreset(preset: String, context: Context) {
        _searchEnginePreset.value = preset
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        persistence.saveSearchEnginePreset(preset)
    }

    fun setFontSizeScale(scale: Float, context: Context) {
        _fontSizeScale.value = scale
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        persistence.saveFontSizeScale(scale)
    }

    fun setHapticVibeEnabled(enabled: Boolean, context: Context) {
        _isHapticVibeEnabled.value = enabled
        val persistence = profilePersistence ?: UserProfilePersistence(context.applicationContext)
        persistence.saveHapticVibeEnabled(enabled)
    }

    fun incrementBlockedCount() {
        _blockedDomainsCount.update { it + 1 }
    }

    // Checking blocklists
    fun isBlocklisted(url: String): Boolean {
        val cleanUrl = url.lowercase()
        val level = _filterLevel.value
        
        val trackers = if (_isTrackerBlockingEnabled.value) {
            listOf(
                "google-analytics.com", "doubleclick.net", "yandex-metrika.ru", 
                "analytics.google.com", "metric-track.ru", "mc.yandex.ru", 
                "telemetry", "tracking", "ads-tracker", "spyware"
            )
        } else {
            emptyList()
        }

        val targets = when (level) {
            "Слабая" -> listOf("blocked.ru", "rkn-ban.com") + trackers
            "Рекомендуемая" -> blocklistedDomains + trackers
            "Максимальная" -> blocklistedDomains + listOf("ads-tracker.com", "telemetry-analytics.ru") + trackers
            "Строгая" -> blocklistedDomains + listOf("ads-tracker.com", "telemetry-analytics.ru", "untrusted-proxy.net", "malicious-spyware.ru", "adware-network.com") + trackers
            else -> blocklistedDomains + trackers
        }
        val blocked = targets.any { cleanUrl.contains(it) }
        if (blocked) {
            _blockedDomainsCount.value += 1
        }
        return blocked
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
    fun addShortcut(title: String, url: String, context: Context) {
        val newShort = PebbleShortcut(
            id = System.currentTimeMillis().toString(),
            title = title,
            url = url
        )
        _shortcuts.update { it + newShort }
        saveCurrentProfileState(context)
    }

    fun deleteShortcut(id: String, context: Context) {
        _shortcuts.update { list -> list.filter { it.id != id } }
        saveCurrentProfileState(context)
    }

    fun resetShortcuts(context: Context? = null) {
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
        context?.let { saveCurrentProfileState(it) }
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

    // Bookmarks management
    private val _bookmarksList = MutableStateFlow<List<String>>(emptyList())
    val bookmarksList = _bookmarksList.asStateFlow()

    fun toggleBookmark(url: String, context: Context) {
        val currentList = _bookmarksList.value
        val newList = if (currentList.contains(url)) {
            currentList.filter { it != url }
        } else {
            currentList + url
        }
        _bookmarksList.value = newList
        saveCurrentProfileState(context)
    }

    // Promo codes & Admin Panel & Donate Shop states
    private val _isAdminEnabled = MutableStateFlow(false)
    val isAdminEnabled = _isAdminEnabled.asStateFlow()

    private val _userBalance = MutableStateFlow(500) // Initial balance is 500 rubles
    val userBalance = _userBalance.asStateFlow()

    private val _purchasedItems = MutableStateFlow<List<String>>(emptyList())
    val purchasedItems = _purchasedItems.asStateFlow()

    fun addBalance(amount: Int) {
        _userBalance.update { it + amount }
        appContext?.let { ctx ->
            val p = ctx.getSharedPreferences("rosbrowser_market_pref", Context.MODE_PRIVATE)
            p.edit().putInt("user_balance", _userBalance.value).apply()
        }
    }

    fun usePromoCode(code: String): String {
        val trimmed = code.trim().lowercase()
        if (trimmed == "admin") {
            _isAdminEnabled.value = true
            addBalance(5000)
            return "Успешно! Активирован промокод 'admin'. Вы получили ПРАВА АДМИНИСТРАТОРА и +5000 рублей!"
        }
        if (trimmed == "letorussia") {
            addBalance(1000)
            return "Успешно! Активирован промокод 'letorussia'. Начислено +1000 рублей!"
        }
        if (trimmed == "rosbrowser") {
            addBalance(500)
            return "Успешно! Активирован промокод 'rosbrowser'. Начислено +500 рублей!"
        }
        return "Ошибка: Неверный или использованный промокод!"
    }

    fun purchaseItem(item: String, price: Int): String {
        if (_purchasedItems.value.contains(item)) {
            return "У вас уже приобретена эта функция!"
        }
        if (_userBalance.value < price) {
            return "Недостаточно средств! Пополните баланс в меню или введите промокод."
        }
        _userBalance.update { it - price }
        _purchasedItems.update { it + item }
        appContext?.let { ctx ->
            val p = ctx.getSharedPreferences("rosbrowser_market_pref", Context.MODE_PRIVATE)
            p.edit()
                .putInt("user_balance", _userBalance.value)
                .putStringSet("purchased_items", _purchasedItems.value.toSet())
                .apply()
        }
        return "Поздравляем! Вы приобрели '$item' успешно!"
    }

    fun restoreYandexPurchases(context: Context): String {
        if (!_isLoggedInYandex.value) {
            return "Сначала авторизуйте Яндекс ID для восстановления покупок!"
        }
        val restored = listOf("vip-sunset", "butterfly-trail", "gost-256", "ad-blocker-pro")
        restored.forEach { item ->
            if (!_purchasedItems.value.contains(item)) {
                _purchasedItems.update { it + item }
            }
        }
        val p = context.applicationContext.getSharedPreferences("rosbrowser_market_pref", Context.MODE_PRIVATE)
        p.edit()
            .putStringSet("purchased_items", _purchasedItems.value.toSet())
            .apply()
        return "Успешно восстановлено 4 покупки из облака Яндекс ID!"
    }

    fun setAdminStatus(enabled: Boolean) {
        _isAdminEnabled.value = enabled
    }
}

