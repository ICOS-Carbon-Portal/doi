package se.lu.nateko.cp.doi

import java.net.URL

import akka.actor.ActorSystem
import se.lu.nateko.cp.doi.core.PlainJavaDoiHttp
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.meta._

object Playground {

	implicit val system = ActorSystem("doi-playground")
	import system.dispatcher

	val config = DoiConfig.getClientConfig
	val http = new PlainJavaDoiHttp(config.symbol, config.password)
	val client = new DoiClient(config, http)

	val example = DoiMeta(
		id = client.doi("carbonportal"),
		creators = Seq(
			Creator(
				name = GenericName("ICOS CP"),
				nameIds = Nil,
				affiliations = Nil
			)
		),
		contributors = Nil,
		titles = Seq(
			Title("Carbon Portal home page", None, None)
		),
		publisher = "ICOS Carbon Portal",
		publicationYear = 2016,
		resourceType = ResourceType("website", ResourceTypeGeneral.Service)
	)

	def testCreate = client.setDoi(example, new URL("https://www.icos-cp.eu/"))
}
