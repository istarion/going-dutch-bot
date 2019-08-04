package ru.s_zg.goingdutchbot

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.s_zg.goingdutchbot.configuration.AppConfig
import ru.s_zg.goingdutchbot.service.BotService
import kotlin.system.exitProcess

@SpringBootApplication
@EnableConfigurationProperties(AppConfig::class)
class GoingDutchBotApplication(val botService: BotService) : CommandLineRunner{
	override fun run(vararg args: String?) {
		botService.loop()
	}

}

fun main(args: Array<String>) {
	runApplication<GoingDutchBotApplication>(*args)
	println("here")
	exitProcess(0)
}
