package com.github.bromel777.config

import com.comcast.ip4s.{Ipv4Address, Port, SocketAddress}
import com.github.bromel777.config.BotConfig.TelegramConfig
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

case class BotConfig(tg: TelegramConfig)

object BotConfig {

  case class TelegramConfig(token: String)
  case class Nodes(nodes: List[SocketAddress[Ipv4Address]])

  val configPath: String = "bot"

  implicit val inetSocketAddressReader: ValueReader[SocketAddress[Ipv4Address]] = { (config: Config, path: String) =>
    val split = config.getString(path).split(":")
    //todo: remove get
    SocketAddress(Ipv4Address(split(0)).get, Port(split(1).toInt).get)
  }

  def loadConfig(configName: String): BotConfig =
    ConfigFactory
      .load(configName)
      .withFallback(ConfigFactory.load())
      .as[BotConfig](configPath)
}



