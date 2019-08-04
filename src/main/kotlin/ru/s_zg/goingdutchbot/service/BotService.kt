package ru.s_zg.goingdutchbot.service

import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.text
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
class BotService(val appConfig: AppConfig) {
    private val logger = LoggerFactory.getLogger(BotService::class.java.name)
    private val bot: Bot
    private var states : MutableMap<Long, String> = mutableMapOf()

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
                text { bot, update ->
                    logger.info("update: {}", update)
                    when (states[update.message!!.from!!.id]) {
                        "going-dutch-0" -> {
                            try {
                                val num = update.message!!.text!!.toInt()
                                states[update.message!!.from!!.id] = "going-dutch-1"
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "Got number $num")
                                return@text
                            } catch (e : NumberFormatException) {
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "Invalid number format! Try again!")
                                return@text
                            } catch (e : Exception) {
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "Unknown exception! $e")
                                states.remove(update.message!!.from!!.id)
                                return@text
                            }
                        }
                        "going-dutch-1" -> {
                            states.remove(update.message!!.from!!.id)
                            bot.sendMessage(chatId = update.message!!.chat.id, text = "TODO! refreshing state")
                            return@text
                        }
                        else -> {
                            bot.sendMessage(chatId = update.message!!.chat.id, text = "Unknown command!")
                        }
                    }
                }
                command("godutch") { bot, update ->
                    val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Cool!")
                    result.fold({
                        bot.sendMessage(chatId = update.message!!.chat.id, text = "gogogo! Enter number")
                        states[update.message!!.from!!.id] = "going-dutch-0"
                    }, {
                        bot.sendMessage(chatId = update.message!!.chat.id, text = "Sorry... $it")
                    })
                }
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