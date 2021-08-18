package se.lu.nateko.cp.doi

import java.net.URL

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.util.ByteString
import se.lu.nateko.cp.doi.core.DoiHttp

class AkkaDoiHttp(
	protected val username: String,
	protected val password: String
)(implicit system: ActorSystem) extends DoiHttp {

	private val http = Http(system)
	private val authHeader = new Authorization(BasicHttpCredentials(username, password))
	import system.dispatcher


	protected def getContent(url: URL, accept: String): Future[DoiResponse] = {

		val acceptHeader = Accept.parseFromValueString(accept).getOrElse(
			throw new Exception("Invalid accept header value: " + accept)
		)

		val request = basicRequest(url).addHeader(acceptHeader)
		http.singleRequest(request).flatMap(responseToDoi)
	}


	def putPayload(url: URL, payload: String, contentType: String): Future[DoiResponse] = {
		val cType = ContentType.parse(contentType).getOrElse(
			throw new Exception("Invalid content type: " + contentType)
		)
		val content = ByteString.apply(payload, "UTF-8")
		val entity = HttpEntity(cType, content)

		val request = basicRequest(url).withMethod(HttpMethods.PUT).withEntity(entity)
		http.singleRequest(request).flatMap(responseToDoi)
	}

	def delete(url: URL): Future[DoiResponse] = {
		val request = basicRequest(url).withMethod(HttpMethods.DELETE)
		http.singleRequest(request).flatMap(responseToDoi)
	}

	private def basicRequest(url: URL) = HttpRequest(
		uri = Uri(url.toString),
		headers = List(authHeader)
	)

	private def responseToDoi(resp: HttpResponse): Future[DoiResponse] = {

		val bodyFut = resp.entity.toStrict(10.seconds).map(_.data.utf8String)

		bodyFut.map(DoiResponse(
			resp.status.intValue,
			resp.status.defaultMessage,
			_
		))
	}
}
