package se.lu.nateko.cp.doi.core

import scala.concurrent.Future
import java.net.URI
import java.net.HttpURLConnection
import scala.concurrent.ExecutionContext
import java.util.Base64
import java.io.InputStream
import java.io.IOException


class PlainJavaDoiHttp(
	username: Option[String],
	password: Option[String]
)(using ExecutionContext) extends DoiHttp{

	protected def getContent(url: URI, accept: String): Future[DoiResponse] = Future{

		val conn = getConnection(url)
		conn.setRequestProperty("Accept", accept)
		try{
			toDoiResponse(conn, conn.getInputStream())
		} catch{
			case _: IOException =>
				toDoiResponse(conn, conn.getErrorStream())
		}
	}

	def putPayload(url: URI, payload: String, contentType: String): Future[DoiResponse] = Future{

		val conn = getConnection(url)
		conn.setDoOutput(true)
		conn.setRequestProperty("Content-Type", contentType)
		conn.setRequestMethod("PUT")

		try{
			val out = conn.getOutputStream()
			out.write(payload.getBytes("UTF-8"))
			out.close()

			toDoiResponse(conn, conn.getInputStream())
		} catch{
			case _: IOException =>
				toDoiResponse(conn, conn.getErrorStream())
		}
	}

	def delete(url: URI): Future[DoiResponse] = Future{

		val conn = getConnection(url)
		conn.setRequestMethod("DELETE")
		try{
			toDoiResponse(conn, conn.getInputStream())
		} catch{
			case _: IOException =>
				toDoiResponse(conn, conn.getErrorStream())
		}
	}

	private def getConnection(url: URI): HttpURLConnection =
		val conn = url.toURL.openConnection().asInstanceOf[HttpURLConnection]
		for user <- username; pass <- password do
			val encoder = Base64.getEncoder()
			val authString = encoder.encodeToString((user + ":" + pass).getBytes)
			conn.setRequestProperty("Authorization", "Basic " + authString)
		conn


	private def toDoiResponse(conn: HttpURLConnection, in: InputStream): DoiResponse = {

		val scanner = new java.util.Scanner(in, "UTF-8").useDelimiter("\\Z")

		try{
			DoiResponse(
				status = conn.getResponseCode,
				message = conn.getResponseMessage,
				body = try{
						scanner.next()
					}catch{
						case _: NoSuchElementException => ""
					}
			)
		} finally{
			scanner.close()
			in.close()
		}
	}

}
