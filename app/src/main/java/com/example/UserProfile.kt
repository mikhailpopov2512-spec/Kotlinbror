package com.example

import android.content.Context
import android.content.SharedPreferences
import com.example.PebbleShortcut

data class UserProfile(
    val id: String,
    val name: String,
    val avatarColor: String, // Hex color for avatar
    val history: List<String> = emptyList(),
    val shortcuts: List<PebbleShortcut> = emptyList(),
    val filterLevel: String = "Рекомендуемая",
    val isAdBlockActive: Boolean = true,
    val isTrackerBlockingEnabled: Boolean = true,
    val isLoggedInYandex: Boolean = false,
    val yandexUsername: String = "",
    val browserMode: BrowserMode = BrowserMode.REGULAR,
    val cookiePartitionId: String = "",
    val bookmarks: List<String> = emptyList()
) {
    fun toPreferenceString(): String {
        // Simple robust custom key-value line storage to avoid import/class compilation problems
        val historyStr = history.joinToString(";;;")
        val shortcutsStr = shortcuts.joinToString(";;;") { "${it.id}|||${it.title}|||${it.url}|||${it.isYandexService}" }
        val bookmarksStr = bookmarks.joinToString(";;;")
        return listOf(
            id,
            name,
            avatarColor,
            historyStr,
            shortcutsStr,
            filterLevel,
            isAdBlockActive.toString(),
            isTrackerBlockingEnabled.toString(),
            isLoggedInYandex.toString(),
            yandexUsername,
            browserMode.name,
            cookiePartitionId,
            bookmarksStr
        ).joinToString("=====")
    }

    companion object {
        fun fromPreferenceString(str: String): UserProfile? {
            try {
                val parts = str.split("=====")
                if (parts.size < 11) return null
                val id = parts[0]
                val name = parts[1]
                val avatarColor = parts[2]
                val history = if (parts[3].isEmpty()) emptyList() else parts[3].split(";;;")
                
                val shortcuts = if (parts[4].isEmpty()) emptyList() else {
                    parts[4].split(";;;").mapNotNull { s ->
                        val subParts = s.split("|||")
                        if (subParts.size >= 4) {
                            PebbleShortcut(
                                id = subParts[0],
                                title = subParts[1],
                                url = subParts[2],
                                isYandexService = subParts[3].toBoolean()
                            )
                        } else null
                    }
                }
                val filterLevel = parts[5]
                val isAdBlockActive = parts[6].toBoolean()
                val isTrackerBlockingEnabled = parts[7].toBoolean()
                val isLoggedInYandex = parts[8].toBoolean()
                val yandexUsername = parts[9]
                val browserMode = try { BrowserMode.valueOf(parts[10]) } catch(e: Exception) { BrowserMode.REGULAR }
                val cookiePartitionId = if (parts.size >= 12) parts[11] else ""
                val bookmarks = if (parts.size >= 13 && parts[12].isNotEmpty()) parts[12].split(";;;") else emptyList()

                return UserProfile(
                    id = id,
                    name = name,
                    avatarColor = avatarColor,
                    history = history,
                    shortcuts = shortcuts,
                    filterLevel = filterLevel,
                    isAdBlockActive = isAdBlockActive,
                    isTrackerBlockingEnabled = isTrackerBlockingEnabled,
                    isLoggedInYandex = isLoggedInYandex,
                    yandexUsername = yandexUsername,
                    browserMode = browserMode,
                    cookiePartitionId = cookiePartitionId,
                    bookmarks = bookmarks
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}

class UserProfilePersistence(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("rosbrowser_profiles_pref", Context.MODE_PRIVATE)

    fun saveProfiles(profiles: List<UserProfile>) {
        val serialized = profiles.joinToString("#####") { it.toPreferenceString() }
        prefs.edit().putString("profiles_list", serialized).apply()
    }

    fun loadProfiles(): List<UserProfile> {
        val serialized = prefs.getString("profiles_list", null) ?: return emptyList()
        if (serialized.isEmpty()) return emptyList()
        return serialized.split("#####").mapNotNull { UserProfile.fromPreferenceString(it) }
    }

    fun saveActiveProfileId(id: String) {
        prefs.edit().putString("active_profile_id", id).apply()
    }

    fun loadActiveProfileId(): String? {
        return prefs.getString("active_profile_id", null)
    }
}
