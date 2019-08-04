package ru.s_zg.goingdutchbot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
data class AppConfig (
        val telegramToken: String,
        val proxyEnabled: Boolean,
        val proxyUrl: String?,
        val proxyPort: Int?,
        val proxyUser: String?,
        val proxyPassword: String?
)