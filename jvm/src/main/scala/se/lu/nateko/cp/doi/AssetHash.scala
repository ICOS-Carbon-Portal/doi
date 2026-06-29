package se.lu.nateko.cp.doi

import java.security.MessageDigest
import scala.util.Using

object AssetHash {

	private val hashes = collection.concurrent.TrieMap.empty[String, String]

	def jsFileName(isDevelopment: Boolean): String =
		if isDevelopment then "doi-js-fastopt.js"
		else s"doi.${forResource("doi-js-opt.js")}.js"

	def cssFileName(isDevelopment: Boolean): String =
		if isDevelopment then "styles.css"
		else s"styles.${forResource("styles.css")}.css"

	def forResource(resourceName: String): String =
		hashes.getOrElseUpdate(resourceName, computeHash(resourceName))

	private def computeHash(resourceName: String): String =
		val stream = getClass.getClassLoader.getResourceAsStream(resourceName)
		if stream == null then sys.error(s"Resource not found: $resourceName")
		Using.resource(stream){is =>
			MessageDigest.getInstance("SHA-256")
				.digest(is.readAllBytes())
				.take(12)
				.map(b => f"${b & 0xff}%02x")
				.mkString
		}
}
