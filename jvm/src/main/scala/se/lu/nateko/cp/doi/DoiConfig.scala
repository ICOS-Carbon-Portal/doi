package se.lu.nateko.cp.doi

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import se.lu.nateko.cp.doi.core.DoiClientConfig
import java.net.URL
import se.lu.nateko.cp.cpauth.core.PublicAuthConfig
import se.lu.nateko.cp.cpauth.core.UserId
import scala.collection.JavaConverters.asScalaBufferConverter

case class DoiConfig(client: DoiClientConfig, auth: PublicAuthConfig, admins: Seq[UserId])

object DoiConfig {

	def getConfig: DoiConfig = {
		val allConf = getAppConfig
		DoiConfig(
			client = getClientConfig(allConf),
			auth = getAuthConfig(allConf),
			admins = allConf.getStringList("cpdoi.admins").asScala.map(UserId(_))
		)
	}

	private def getAppConfig: Config = {
		val default = ConfigFactory.load
		val confFile = new java.io.File("application.conf").getAbsoluteFile
		if(!confFile.exists) default
		else ConfigFactory.parseFile(confFile).withFallback(default)
	}

	private def getClientConfig(allConf: Config): DoiClientConfig = {
		val client = allConf.getConfig("cpdoi")
		DoiClientConfig(
			symbol = client.getString("symbol"),
			password = client.getString("password"),
			endpoint = new URL(client.getString("endpoint")),
			doiPrefix = client.getString("doiPrefix")
		)
	}

	private def getAuthConfig(allConf: Config): PublicAuthConfig = {
		val auth = allConf.getConfig("cpauth.auth.pub")
		PublicAuthConfig(
			authCookieName = auth.getString("authCookieName"),
			authCookieDomain = auth.getString("authCookieDomain"),
			cpauthHost = auth.getString("cpauthHost"),
			publicKeyPath = auth.getString("publicKeyPath")
		)
	}
}