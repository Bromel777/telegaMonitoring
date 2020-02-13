package com.github.bromel777.config

import com.github.bromel777.config.Config.TelegramConfig
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class Config(tg: TelegramConfig)

object Config {

  case class TelegramConfig(token: String)

  val configPath: String = "bot"

  def loadConfig(configName: String): Config =
    ConfigFactory
      .load(configName)
      .withFallback(ConfigFactory.load())
      .as[Config](configPath)
}



