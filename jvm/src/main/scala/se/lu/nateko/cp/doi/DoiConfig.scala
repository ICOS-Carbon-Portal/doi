package se.lu.nateko.cp.doi

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import se.lu.nateko.cp.doi.core.DoiClientConfig
import java.net.URL
import se.lu.nateko.cp.cpauth.core.PublicAuthConfig
import se.lu.nateko.cp.cpauth.core.UserId
import scala.collection.JavaConverters.asScalaBufferConverter

case class DoiConfig(
	client: DoiClientConfig,
	prefixInfo: PrefixInfo,
	auth: PublicAuthConfig,
	admins: Seq[UserId]
)

object DoiConfig {

	def getConfig: DoiConfig = {
		val allConf = getAppConfig
		val doiConf = allConf.getConfig("cpdoi")
		DoiConfig(
			client = getClientConfig(doiConf),
			prefixInfo = getPrefixInfo(doiConf),
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

	private def getClientConfig(doiConf: Config) = DoiClientConfig(
		symbol = doiConf.getString("symbol"),
		password = doiConf.getString("password"),
		endpoint = new URL(doiConf.getString("endpoint")),
		doiPrefix = getPrefixInfo(doiConf).staging
	)

	private def getPrefixInfo(doiConf: Config) = PrefixInfo(
		staging = doiConf.getString("stagingPrefix"),
		production = doiConf.getString("productionPrefix")
	)

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