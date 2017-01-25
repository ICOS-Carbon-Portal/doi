package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future

trait DoiHttp {

	case class DoiResponse(status: Int, message: String, body: String)

	protected val username: String
	protected val password: String

	def getText(url: URL): Future[DoiResponse] = getContent(url, "text/plain;charset=UTF-8")
	def getXml(url: URL): Future[DoiResponse] = getContent(url, "application/xml")

	protected def getContent(url: URL, accept: String): Future[DoiResponse]
	def postPayload(url: URL, payload: String, contentType: String): Future[DoiResponse]
	def delete(url: URL): Future[DoiResponse]
}
