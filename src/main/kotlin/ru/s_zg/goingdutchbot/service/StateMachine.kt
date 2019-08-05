package ru.s_zg.goingdutchbot.service

import me.ivmg.telegram.Bot
import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import me.ivmg.telegram.entities.Update
import me.ivmg.telegram.network.fold
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class StateMachine {
    companion object {
        const val BOT_NAME = "going_dutch_bot"
    }

    private val logger = LoggerFactory.getLogger(StateMachine::class.java.name)
    private var states: MutableMap<Long, String> = mutableMapOf()
    private var users: MutableSet<Long> = mutableSetOf()

    fun start(bot: Bot, update: Update) {
        logger.info("Register update: {}", update)
        users.add(update.message?.from?.id!!)
    }

    fun goDutch(bot: Bot, update: Update) {
        val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Cool!")
        result.fold({
            bot.sendMessage(chatId = update.message!!.chat.id, text = "gogogo! Enter number")
            states[update.message!!.from!!.id] = "going-dutch-0"
        }, {
            bot.sendMessage(chatId = update.message!!.chat.id, text = "Sorry... $it")
        })
    }

    fun letMeIn(bot: Bot, update: Update) {
        if (update.callbackQuery!!.from.id in users) {
            bot.editMessageText(update.callbackQuery!!.message!!.chat.id,
                    messageId = update.callbackQuery!!.message!!.messageId,
                    text = update.callbackQuery!!.from.username!!,
                    replyMarkup = InlineKeyboardMarkup.createSingleButton(InlineKeyboardButton("test", callbackData = "letMeIn")))
        } else {
            bot.answerCallbackQuery(update.callbackQuery!!.id, url = "telegram.me/$BOT_NAME?start=register")
        }
    }

    fun textProcessor(bot: Bot, update: Update) {
        logger.info("update: {}", update)
        if (update.message?.text in setOf("/godutch", "/start register")) {
            return
        }
        when (states[update.message!!.from!!.id]) {
            "going-dutch-0" -> {
                try {
                    val num = update.message!!.text!!.toInt()
                    states[update.message!!.from!!.id] = "going-dutch-1"
                    bot.sendMessage(chatId = update.message!!.chat.id, text = "Got number $num")
                    return
                } catch (e : NumberFormatException) {
                    bot.sendMessage(chatId = update.message!!.chat.id, text = "Invalid number format! Try again!")
                    return
                } catch (e : Exception) {
                    bot.sendMessage(chatId = update.message!!.chat.id, text = "Unknown exception! $e")
                    states.remove(update.message!!.from!!.id)
                    return
                }
            }
            "going-dutch-1" -> {
                states.remove(update.message!!.from!!.id)
                bot.sendMessage(chatId = update.message!!.chat.id, text = "TODO! refreshing state",
                        replyMarkup = InlineKeyboardMarkup.createSingleButton(InlineKeyboardButton("test", callbackData = "letMeIn")))
                return
            }
        }
    }
}