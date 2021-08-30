package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.io.Source
import se.lu.nateko.cp.doi.{Doi,DoiMeta,DoiWrapper}
import se.lu.nateko.cp.doi.{SingleDoiPayload,DoiListPayload}
import scala.util.Try
import scala.collection.Seq
import spray.json._
import JsonSupport._

class DoiClient(config: DoiClientConfig, http: DoiHttp)(implicit ctxt: ExecutionContext) {

	val metaBase: URL = new URL(config.restEndpoint, "dois")
	val clientDois: URL = new URL(s"${config.restEndpoint}dois?client-id=${config.symbol.toLowerCase()}&page[size]=500")

	def doi(suffix: String): Doi = Doi(config.doiPrefix, suffix)
	def metaUrl(doi: Doi) = new URL(s"$metaBase/$doi")

	def listDoisMeta: Future[String] = http.getJson(clientDois).flatMap(
		resp => analyzeResponse{case 200 => Future.successful(resp.body)}(resp)
	)
	def listDoisParsed: Future[Seq[DoiMeta]] = listDoisMeta.map{
		_.parseJson.convertTo[DoiListPayload].data.map(_.attributes)
	}

	def getMetadata(doi: Doi): Future[Option[String]] = http.getJson(metaUrl(doi)).flatMap(
		resp => analyzeResponse{
			case 200 => Future.successful(Some(resp.body))
			case 404 => Future.successful(None)
		}(resp)
	)
	def getMetadataParsed(doi: Doi): Future[Option[DoiMeta]] = getMetadata(doi).map{
		_.map(_.parseJson.convertTo[SingleDoiPayload].data.attributes)
	}

	def putMetadata(meta: DoiMeta): Future[Unit] = http
		.putPayload(
			metaUrl(meta.doi),
			SingleDoiPayload(DoiWrapper(meta)).toJson.compactPrint,
			"application/vnd.api+json"
		).flatMap(analyzeResponse{
			case 200 | 201 => Future.successful(())
		})


	def delete(doi: Doi): Future[Unit] =
		http.delete(metaUrl(doi)).flatMap(
			analyzeResponse{case 204 | 404 => Future.successful(())}
		)

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
