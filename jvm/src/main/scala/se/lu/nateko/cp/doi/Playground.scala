package se.lu.nateko.cp.doi

import java.net.URL

import akka.actor.ActorSystem
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.meta._

object Playground {

	implicit val system = ActorSystem("doi-playground")
	import system.dispatcher

	val config = DoiConfig.getConfig.client
	val http = new AkkaDoiHttp(config.symbol, config.password)
	val client = new DoiClient(config, http)

	val testDoi = client.doi("carbonportal")
	val example = DoiMeta(
		id = testDoi,
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

	val testDoi2 = client.doi("icosdocs")
	val example2 = DoiMeta(
		id = testDoi2,
		creators = Seq(
			Creator(
				name = PersonalName("Oleg", "Mirzov"),
				nameIds = Seq(NameIdentifier.orcid("0000-0002-4742-958X")),
				affiliations = Seq("Lund University")
			)
		),
		contributors = Nil,
		titles = Seq(
			Title("ICOS Alfresco document handling system", None, None)
		),
		publisher = "ICOS Carbon Portal",
		publicationYear = 2017,
		resourceType = ResourceType("website", ResourceTypeGeneral.Service)
	)

	def testCreate = client.setDoi(example, new URL("https://www.icos-cp.eu/"))
	def testCreate2 = client.setDoi(example2, new URL("https://docs.icos-cp.eu/"))
	def testMeta = client.getMetadata(testDoi)
}
