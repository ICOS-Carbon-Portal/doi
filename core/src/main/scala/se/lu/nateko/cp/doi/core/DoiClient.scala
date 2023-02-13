package se.lu.nateko.cp.doi.core

import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiListPayload
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.DoiWrapper
import se.lu.nateko.cp.doi.SingleDoiPayload
import spray.json._

import java.net.URL
import java.net.URLEncoder
import scala.collection.Seq
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

import JsonSupport.given

trait DoiReadonlyClient(conf: DoiEndpointConfig, protected val http: DoiHttp)(using ExecutionContext):

	protected val metaBase: URL = new URL(conf.restEndpoint, "dois")

	def metaUrl(doi: Doi) = new URL(s"$metaBase/$doi")

	def getMetadata(doi: Doi): Future[Option[DoiMeta]] = http.getJson(metaUrl(doi)).flatMap(
		resp => analyzeResponse{
			case 200 => Future.successful(
				Some(resp.body.parseJson.convertTo[SingleDoiPayload].data.attributes)
			)
			case 404 => Future.successful(None)
		}(resp)
	)

	protected def analyzeResponse[T](pf: PartialFunction[Int, Future[T]])(resp: http.DoiResponse): Future[T] =
		pf.applyOrElse(
			resp.status,
			(status: Int) => {
				val msg = s"""Problem communicating with DataCite:
					|HTTP status code: $status
					|Response message: ${resp.message}
					|Response content: ${resp.body}""".stripMargin
				Future.failed(new Exception(msg))
			}
		)

end DoiReadonlyClient

class DoiClient(conf: DoiClientConfig, doiHttp: DoiHttp)(using ExecutionContext) extends DoiReadonlyClient(conf, doiHttp: DoiHttp):

	private val config = conf.member

	def clientDois(query: String, page: Int): URL = new URL(
		//TODO Move page size into the API, too
		s"${conf.restEndpoint}dois?query=${URLEncoder.encode(query, "UTF-8")}&client-id=${config.symbol.toLowerCase()}&page[size]=25&page[number]=$page"
	)

	def doi(suffix: String): Doi = Doi(config.doiPrefix, suffix)

	def listDoisMeta(query: Option[String] = None, page: Option[Int]): Future[DoiListPayload] = http
		.getJson(clientDois(query.getOrElse(""), page.getOrElse(1))).flatMap(
			resp => analyzeResponse{
				case 200 => Future.successful(resp.body.parseJson.convertTo[DoiListPayload])
			}(resp)
		)

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
