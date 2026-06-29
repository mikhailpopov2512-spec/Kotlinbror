package com.example

import android.util.Log

object AdBlockEngine {
    // Top-tier ad and popup networks in Russia and globally (corresponds to EasyList Russia)
    private val easylistRussiaDomains = hashSetOf(
        "an.yandex.ru",
        "partner.yandex.ru",
        "direct.yandex.ru",
        "direct.yandex.com",
        "direct.yandex.by",
        "direct.yandex.kz",
        "relap.io",
        "target.my.com",
        "ad.mail.ru",
        "rs.mail.ru",
        "begun.ru",
        "marketgid.com",
        "teasernet.com",
        "kadam.ru",
        "redtram.com",
        "googleads.g.doubleclick.net",
        "pubads.g.doubleclick.net",
        "securepubads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "adservice.google.ru",
        "ads.google.com",
        "adnxs.com",
        "adtech.de",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "mgid.com",
        "exoclick.com",
        "adsterra.com",
        "rtb.com",
        "adriver.ru",
        "gnezdo.ru",
        "advertur.ru",
        "mediatoday.ru"
    )

    // Russian & Global trackers, telemetry, and analytics networks (corresponds to RU AdList)
    private val ruAdListDomains = hashSetOf(
        "mc.yandex.ru",
        "mc.yandex.by",
        "mc.yandex.co.il",
        "mc.yandex.com",
        "mc.yandex.kz",
        "mc.yandex.ua",
        "top.mail.ru",
        "top-fwz1.mail.ru",
        "tns-counter.ru",
        "rambler.ru/cnt",
        "liveinternet.ru/click",
        "hotlog.ru",
        "google-analytics.com",
        "analytics.google.com",
        "googletagmanager.com",
        "googletagservices.com",
        "segment.io",
        "amplitude.com",
        "mixpanel.com",
        "hotjar.com",
        "bugsnag.com",
        "sentry.io",
        "facebook.net/en_US/fbevents.js",
        "connect.facebook.net",
        "pixel.facebook.com",
        "vk.com/rtrg",
        "vk.com/js/api/openapi.js"
    )

    // Regex-style substrings for ad files, banner assets, and tracking parameters
    private val adPathKeywords = listOf(
        "/ads/", "/banners/", "/adrotate/", "/adserver/", "is_ad=true",
        "advert-", "/blockad", "/popunder", "/clickunder", "-ad-banner-",
        "yandex.ru/clck", "ad_partner", "banner_id", "advertisement",
        "show_ads", "ad_type", "ad_client", "adv_code", "ads_box"
    )

    private val trackerPathKeywords = listOf(
        "/telemetry/", "/tracker/", "/metrics/", "/logs/", "/counter.js",
        "analytics.js", "ga.js", "metrika.js", "tracking-pixel", "user-metric",
        "pingback", "/telemetry-analytics", "/stats/", "/click-counter"
    )

    /**
     * Matches a URL against Russia-focused and global ad blocking rules (EasyList Russia).
     */
    fun matchesEasyListRussia(url: String): Boolean {
        val lowercaseUrl = url.lowercase()
        
        // 1. Direct host match or suffix match
        for (domain in easylistRussiaDomains) {
            if (lowercaseUrl.contains("://$domain") || lowercaseUrl.contains(".$domain")) {
                Log.d("AdBlockEngine", "Blocked Ad (EasyList Russia): $url")
                return true
            }
        }

        // 2. Keyword path and query match
        for (keyword in adPathKeywords) {
            if (lowercaseUrl.contains(keyword)) {
                Log.d("AdBlockEngine", "Blocked Ad Path (EasyList Russia): $url with keyword $keyword")
                return true
            }
        }

        return false
    }

    /**
     * Matches a URL against Russia-focused and global tracker blocking rules (RU AdList).
     */
    fun matchesRuAdList(url: String): Boolean {
        val lowercaseUrl = url.lowercase()

        // 1. Direct host match or suffix match
        for (domain in ruAdListDomains) {
            if (lowercaseUrl.contains("://$domain") || lowercaseUrl.contains(".$domain")) {
                Log.d("AdBlockEngine", "Blocked Tracker (RU AdList): $url")
                return true
            }
        }

        // 2. Keyword path and query match
        for (keyword in trackerPathKeywords) {
            if (lowercaseUrl.contains(keyword)) {
                Log.d("AdBlockEngine", "Blocked Tracker Path (RU AdList): $url with keyword $keyword")
                return true
            }
        }

        return false
    }
}
