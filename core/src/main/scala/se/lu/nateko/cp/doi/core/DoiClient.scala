package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.io.Source
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import scala.xml.XML
import scala.util.Try

class DoiClient(config: DoiClientConfig, http: DoiHttp)(implicit ctxt: ExecutionContext) {

	val doiBase: URL = new URL(config.endpoint, "doi")
	val metaBase: URL = new URL(config.endpoint, "metadata")

	def doi(suffix: String): Doi = Doi(config.doiPrefix, suffix)
	def doiUrl(doi: Doi) = new URL(doiBase + "/" + doi.toString)
	def metaUrl(doi: Doi) = new URL(metaBase + "/" + doi.toString)

	def listDois: Future[Seq[Doi]] = {

		http.getText(doiBase).flatMap(response => analyzeResponse{
			case 200 =>
				val doiLines = Source.fromString(response.body).getLines
				val doiTries = doiLines.map(DoiMetaParser.parseDoi)
				Future.fromTry(DoiMetaParser.tryAll(doiTries))
			case 204 =>
				Future.successful(Seq.empty)
		}(response))
	}

	def setDoi(meta: DoiMeta, targetUrl: URL): Future[Unit] = {
		postMetadata(meta).flatMap(_ => setUrl(meta.id, targetUrl))
	}

	def getMetadata(doi: Doi): Future[DoiMeta] = http.getXml(metaUrl(doi)).flatMap(response =>
		analyzeResponse{
			case 200 =>
				val metaXmlTry = Try(XML.loadString(response.body))
				Future.fromTry(metaXmlTry.flatMap(DoiMetaParser.parse))
		}(response)
	)

	def checkIfKnown(doi: Doi): Future[Boolean] = http.getText(doiUrl(doi)).flatMap(
		analyzeResponse{
			case 200 | 204 => Future.successful(true)
			case 404 => Future.successful(false)
		}
	)

	def getUrl(doi: Doi): Future[Option[URL]] = http.getText(doiUrl(doi)).flatMap(response =>
		analyzeResponse{
			case 200 =>
				Future.fromTry(Try(Some(new URL(response.body))))
			case 204 =>
				Future.successful(None)
		}(response)
	)

	def setUrl(doi: Doi, targetUrl: URL): Future[Unit] = {
		val payload = s"doi=$doi\nurl=$targetUrl"

		http.postPayload(doiBase, payload, "text/plain;charset=UTF-8").flatMap(analyzeResponse{
			case 201 => Future.successful(())
		})
	}

	def postMetadata(meta: DoiMeta): Future[Unit] = {
		val xml = views.xml.doi.DoiMeta(meta)

		http.postPayload(metaBase, xml.body, "application/xml;charset=UTF-8").flatMap(analyzeResponse{
			case 201 => Future.successful(())
		})
	}

	def deactivate(doi: Doi): Future[Unit] = http.delete(metaUrl(doi)).flatMap(
		analyzeResponse{case 200 => Future.successful(())}
	)

	private def makeFailure[T](response: http.DoiResponse): Future[T] = {
		val msg = response.message + ": " + response.body
		Future.failed(new Exception(msg))
	}

	private def analyzeResponse[T](pf: PartialFunction[Int, Future[T]])(resp: http.DoiResponse): Future[T] = {
		pf.applyOrElse(
			resp.status,
			(status: Int) => {
				val msg = s"""Problem communicating with DateCite:
					|HTTP status code: $status
					|Response message: ${resp.message}
					|Response content: ${resp.body}""".stripMargin
				Future.failed(new Exception(msg))
			}
		)
	}
}

object DoiClient{
	val mask = (1L << 35) - 1

	val encoder: Map[Int, Char] = Map(
		0 -> '0',
		1 -> '1',
		2 -> '2',
		3 -> '3',
		4 -> '4',
		5 -> '5',
		6 -> '6',
		7 -> '7',
		8 -> '8',
		9 -> '9',
		10 -> 'A',
		11 -> 'B',
		12 -> 'C',
		13 -> 'D',
		14 -> 'E',
		15 -> 'F',
		16 -> 'G',
		17 -> 'H',
		18 -> 'J',
		19 -> 'K',
		20 -> 'M',
		21 -> 'N',
		22 -> 'P',
		23 -> 'Q',
		24 -> 'R',
		25 -> 'S',
		26 -> 'T',
		27 -> 'V',
		28 -> 'W',
		29 -> 'X',
		30 -> 'Y',
		31 -> 'Z',
		32 -> '*',
		33 -> '~',
		34 -> '$',
		35 -> '=',
		36 -> 'U'
	)

	/**
	 * Crockford's base32 encoding, 8 symbols, the last one is a check symbol
	 */
	def randomCoolSuffix: String = {

		val v = new java.util.Random().nextLong() & mask
		val check = (v % 37).toInt

		val byte32values = (30 to 0 by -5).map{shift =>
			((v >> shift) & 31L).toInt
		}
		(byte32values :+ check).map(encoder).mkString
	}
}
