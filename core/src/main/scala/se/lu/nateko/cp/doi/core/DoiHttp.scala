package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future

trait DoiHttp {

	case class DoiResponse(status: Int, message: String, body: String)

	protected val username: String
	protected val password: String

	def getJson(url: URL): Future[DoiResponse] = {
		println("get json from " + url)
		getContent(url, "application/vnd.api+json")
	}

	protected def getContent(url: URL, accept: String): Future[DoiResponse]
	def putPayload(url: URL, payload: String, contentType: String): Future[DoiResponse]
	def delete(url: URL): Future[DoiResponse]
}
