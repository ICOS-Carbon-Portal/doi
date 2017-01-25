package se.lu.nateko.cp.doi.core

import scala.concurrent.Future
import java.net.URL
import java.net.HttpURLConnection
import scala.concurrent.ExecutionContext
import java.util.Base64
import java.io.InputStream
import java.io.IOException


class PlainJavaDoiHttp(
	protected val username: String,
	protected val password: String
)(implicit ctxt: ExecutionContext) extends DoiHttp{

	protected def getContent(url: URL, accept: String): Future[DoiResponse] = Future{

		val conn = getConnection(url)
		conn.setRequestProperty("Accept", accept)
		try{
			toDoiResponse(conn, conn.getInputStream())
		} catch{
			case _: IOException =>
				toDoiResponse(conn, conn.getErrorStream())
		}
	}

	def postPayload(url: URL, payload: String, contentType: String): Future[DoiResponse] = Future{

		val conn = getConnection(url)
		conn.setDoOutput(true)
		conn.setRequestProperty("Content-Type", contentType)
		conn.setRequestMethod("POST")

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

	def delete(url: URL): Future[DoiResponse] = Future{

		val conn = getConnection(url)
		conn.setRequestMethod("DELETE")
		try{
			toDoiResponse(conn, conn.getInputStream())
		} catch{
			case _: IOException =>
				toDoiResponse(conn, conn.getErrorStream())
		}
	}

	private def getConnection(url: URL): HttpURLConnection = {
		val conn = url.openConnection().asInstanceOf[HttpURLConnection]
		val encoder = Base64.getEncoder()
		val authString = encoder.encodeToString((username + ":" + password).getBytes)
		conn.setRequestProperty("Authorization", "Basic " + authString)
		conn
	}

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
