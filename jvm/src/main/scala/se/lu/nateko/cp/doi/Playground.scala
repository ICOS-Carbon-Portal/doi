// package se.lu.nateko.cp.doi

import java.net.URL

import se.lu.nateko.cp.doi.*
import akka.actor.ActorSystem
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.core.JsonSupport.given
import se.lu.nateko.cp.doi.meta._
import spray.json.*
import se.lu.nateko.cp.doi.core.PlainJavaDoiHttp
import scala.concurrent.Await
import scala.concurrent.duration.Duration



object Playground {

	given system: ActorSystem = ActorSystem("doi-playground")
	import system.dispatcher

	val config = DoiConfig.getConfig.client
	val akkaHttp = new AkkaDoiHttp(config.member.symbol, config.member.password)
	val javaHttp = new PlainJavaDoiHttp(Some(config.member.symbol), Some(config.member.password))

	val akkaClient = new DoiClient(config, akkaHttp)
	val javaClient = new DoiClient(config, javaHttp)

	val testDoi = akkaClient.doi("carbonportal")
	val testDoi2 = javaClient.doi("MJAP-FD4W")

	val example = DoiMeta(
		doi = testDoi2,
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

	val testDoi3 = akkaClient.doi("icosdocs")
	val example2 = DoiMeta(
		doi = testDoi3,
		creators = Seq(
			Creator(
				name = PersonalName("Oleg", "Mirzov"),
				// name = "Oleg Mirzov",
				nameIdentifiers = Seq(NameIdentifier.orcid("0000-0002-4742-958X")),
				affiliation = Seq(Affiliation("Lund University"))
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

	def testCreate = 
		println("---------------- akka client --------------------------")
		Await.ready(akkaClient.putMetadata(example).andThen(res => println(res)), Duration.Inf)

		println("---------------- java client --------------------------")
		Await.ready(javaClient.putMetadata(example).map(res => println(res)), Duration.Inf)

		// fut foreach println
		// fut.failed.foreach(err => println(err.getMessage))

	def testCreate2 = akkaClient.putMetadata(example2)
	def testMeta = Await.ready(javaClient.getMetadata(testDoi2).andThen(res => println(res)), Duration.Inf)

	def printPrettyJson(suffix: String): Unit = {
		val doi = akkaClient.doi(suffix)
		val fut = akkaClient.getMetadata(doi).map{_
			.getOrElse(throw new Exception(s"doi not found: $doi"))
			.toJson
			.prettyPrint
		} 
		fut foreach println
		fut.failed.foreach(err => println(err.getMessage))
	}
}
