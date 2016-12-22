package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.io.Source
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi

class DoiClient(config: DoiClientConfig, http: DoiHttp)(implicit ctxt: ExecutionContext) {

	val doiBase: URL = new URL(config.endpoint, "doi")
	val metaBase: URL = new URL(config.endpoint, "metadata")

	def doi(suffix: String): Doi = Doi(config.doiPrefix, suffix)
	def doiUrl(doi: Doi) = new URL(doiBase, doi.toString)
	def metaUrl(doi: Doi) = new URL(metaBase, doi.toString)

	def listDois: Future[Seq[String]] = {

		http.getText(doiBase).flatMap(response => analyzeResponse{
			case 200 => Future.successful(
				Source.fromString(response.body).getLines.toSeq
			)
			case 204 =>
				Future.successful(Seq.empty)
		}(response))
	}

	def setDoi(meta: DoiMeta, targetUrl: URL): Future[Unit] = {
		postMetadata(meta).flatMap(_ => setUrl(meta.id, targetUrl))
	}

	def getMetadata(doi: Doi): Future[DoiMeta] = ???

	private def setUrl(doi: Doi, targetUrl: URL): Future[Unit] = {
		val payload = s"doi=$doi\nurl=$targetUrl"

		http.postPayload(doiBase, payload, "text/plain;charset=UTF-8").flatMap(analyzeResponse{
			case 201 => Future.successful(())
		})
	}

	private def postMetadata(meta: DoiMeta): Future[Unit] = {
		val xml = views.xml.doi.DoiMeta(meta)

		http.postPayload(metaBase, xml.body, "application/xml;charset=UTF-8").flatMap(analyzeResponse{
			case 201 => Future.successful(())
		})
	}

	private def makeFailure[T](response: http.DoiResponse): Future[T] = {
		val msg = response.message + ": " + response.body
		Future.failed(new Exception(msg))
	}

	private def analyzeResponse[T](pf: PartialFunction[Int, Future[T]])(resp: http.DoiResponse): Future[T] = {
		pf.applyOrElse(resp.status, (status: Int) => Future.failed(new Exception(resp.message + ": " + resp.body)))
	}
}


