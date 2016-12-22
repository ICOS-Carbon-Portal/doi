package se.lu.nateko.cp.doi.core

import java.net.URL
import scala.concurrent.Future

trait DoiHttp {

	case class DoiResponse(status: Int, message: String, body: String)

	protected val username: String
	protected val password: String

	def getText(url: URL): Future[DoiResponse]

	def postPayload(url: URL, payload: String, contentType: String): Future[DoiResponse]
}
