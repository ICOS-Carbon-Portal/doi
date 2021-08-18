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
		doi = testDoi,
		creators = Seq(
			Creator(
				name = GenericName("ICOS CP"),
				// name = "ICOS CP",
				nameIdentifiers = Nil,
				affiliation = Nil
			)
		),
		contributors = Nil,
		titles = Some(Seq(
			Title("Carbon Portal home page", None, None)
		)),
		publisher = Some("ICOS Carbon Portal"),
		publicationYear = Some(2016),
		types = Some(ResourceType(Some("website"), Some(ResourceTypeGeneral.Service))),
		url = Some("https://www.icos-cp.eu/")
	)

	val testDoi2 = client.doi("icosdocs")
	val example2 = DoiMeta(
		doi = testDoi2,
		creators = Seq(
			Creator(
				name = PersonalName("Oleg", "Mirzov"),
				// name = "Oleg Mirzov",
				nameIdentifiers = Seq(NameIdentifier.orcid("0000-0002-4742-958X")),
				affiliation = Seq("Lund University")
			)
		),
		contributors = Nil,
		titles = Some(Seq(
			Title("ICOS Alfresco document handling system", None, None)
		)),
		publisher = Some("ICOS Carbon Portal"),
		publicationYear = Some(2017),
		types = Some(ResourceType(Some("website"), Some(ResourceTypeGeneral.Service))),
		url = Some("https://docs.icos-cp.eu/")
	)

	def testCreate = client.putMetadata(example)
	def testCreate2 = client.putMetadata(example2)
	def testMeta = client.getMetadata(testDoi)
}
