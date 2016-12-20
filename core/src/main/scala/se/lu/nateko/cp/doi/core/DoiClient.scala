package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.io.Source
import se.lu.nateko.cp.doi.DoiMeta

class DoiClient(config: DoiClientConfig, http: DoiHttp)(implicit ctxt: ExecutionContext) {

	val doiBase: URL = new URL(config.endpoint, "doi")
	val metaBase: URL = new URL(config.endpoint, "metadata")

	def doi(suffix: String) = config.doiPrefix + "/" + suffix
	def doiUrl(suffix: String) = new URL(doiBase, doi(suffix))
	def metaUrl(suffix: String) = new URL(metaBase, doi(suffix))

	def listDois: Future[Seq[String]] = {

		http.getText(doiBase).flatMap(response => analyzeResponse{
			case 200 => Future.successful(
				Source.fromString(response.body).getLines.toSeq
			)
			case 204 =>
				Future.successful(Seq.empty)
		}(response))
	}

	def createDoi(suffix: String, targetUrl: URL): Future[Unit] = {
		val payload = s"doi=${doi(suffix)}\nurl=$targetUrl"

		http.postUtf8Text(doiBase, payload).flatMap(analyzeResponse{
			case 201 => Future.successful(())
		})
	}

	def getMetadata(suffix: String): Future[DoiMeta] = ???

	private def makeFailure[T](response: http.DoiResponse): Future[T] = {
		val msg = response.message + ": " + response.body
		Future.failed(new Exception(msg))
	}

	private def analyzeResponse[T](pf: PartialFunction[Int, Future[T]])(resp: http.DoiResponse): Future[T] = {
		pf.applyOrElse(resp.status, (status: Int) => Future.failed(new Exception(resp.message + ": " + resp.body)))
	}
}


