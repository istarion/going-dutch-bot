package ru.s_zg.goingdutchbot.service

import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.text
import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup.Companion.createSingleButton
import me.ivmg.telegram.network.fold
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.s_zg.goingdutchbot.configuration.AppConfig
import java.lang.Exception
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import javax.annotation.PreDestroy

@Service
class BotService(val appConfig: AppConfig, val stateMachine: StateMachine) {
    private val logger = LoggerFactory.getLogger(BotService::class.java.name)
    private val bot: Bot

    init {
        if (appConfig.proxyEnabled) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(appConfig.proxyUser, appConfig.proxyPassword!!.toCharArray())
                }
            })
        }
        bot = bot {
            token = appConfig.telegramToken
            dispatch {
                command("godutch", stateMachine::goDutch)
                command("start", stateMachine::start)
                callbackQuery("letMeIn", stateMachine::letMeIn)
                text(body = stateMachine::textProcessor)
            }
            if (appConfig.proxyEnabled) {
                proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(appConfig.proxyUrl, appConfig.proxyPort!!))
            }
        }
    }

    fun loop() {
        bot.startPolling()
        Thread.currentThread().join()
    }
}