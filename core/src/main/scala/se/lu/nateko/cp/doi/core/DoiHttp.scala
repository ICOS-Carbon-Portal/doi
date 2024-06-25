package se.lu.nateko.cp.doi.core

import java.net.URI
import scala.concurrent.Future

trait DoiHttp {

	case class DoiResponse(status: Int, message: String, body: String)

	def getJson(url: URI): Future[DoiResponse] = getContent(url, "application/vnd.api+json")

	protected def getContent(url: URI, accept: String): Future[DoiResponse]
	def putPayload(url: URI, payload: String, contentType: String): Future[DoiResponse]
	def delete(url: URI): Future[DoiResponse]
}
