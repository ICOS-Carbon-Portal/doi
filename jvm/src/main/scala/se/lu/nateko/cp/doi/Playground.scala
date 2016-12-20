package se.lu.nateko.cp.doi

import akka.actor.ActorSystem
import se.lu.nateko.cp.doi.core.PlainJavaDoiHttp
import se.lu.nateko.cp.doi.core.DoiClient

object Playground {

	implicit val system = ActorSystem("doi-playground")
	import system.dispatcher

	val config = DoiConfig.getClientConfig
	val http = new PlainJavaDoiHttp(config.symbol, config.password)
	val client = new DoiClient(config, http)
}
