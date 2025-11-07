package se.lu.nateko.cp.doi

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import se.lu.nateko.cp.cpauth.core.ConfigLoader
import se.lu.nateko.cp.doi.core.DoiClientConfig
import se.lu.nateko.cp.doi.core.DoiMemberConfig
import java.net.URI
import se.lu.nateko.cp.cpauth.core.PublicAuthConfig
import se.lu.nateko.cp.cpauth.core.UserId
import scala.jdk.CollectionConverters.ListHasAsScala
import eu.icoscp.envri.Envri

case class EmailConfig(
	smtpServer: String,
	username: String,
	password: String,
	fromAddress: String
)

case class DoiConfig(
	httpBindInterface: String,
	httpBindPort: Int,
	client: DoiClientConfig,
	prefixInfo: String,
	auth: PublicAuthConfig,
	admins: Seq[UserId],
	mailing: EmailConfig,
	metaHost: String,
	development: Boolean
)

object DoiConfig {

	def getConfig(using envri: Envri): DoiConfig = {
		val allConf = ConfigLoader.appConfig

		val doiConf = allConf.getConfig("cpdoi")
		DoiConfig(
			httpBindInterface = doiConf.getString("httpBindInterface"),
			httpBindPort = doiConf.getInt("httpBindPort"),
			client = getClientConfig(doiConf),
			prefixInfo = doiConf.getString("member.prefix"),
			auth = ConfigLoader.authPubConfig(envri),
			admins = allConf.getStringList("cpdoi.admins").asScala.map(UserId(_)).toIndexedSeq,
			mailing = getMailingConfig(doiConf),
			metaHost = doiConf.getString("metaHost"),
			development = if doiConf.hasPath("development") then doiConf.getBoolean("development") else false
		)
	}

	private def getClientConfig(doiConf: Config) = DoiClientConfig(
		restEndpoint = new URI(doiConf.getString("restEndpoint")),
		member = DoiMemberConfig(
			symbol = doiConf.getString("member.symbol"),
			password = doiConf.getString("member.password"),
			doiPrefix = doiConf.getString("member.prefix")
		)
	)

	private def getAuthConfig(allConf: Config): PublicAuthConfig = {
		val auth = allConf.getConfig("cpauthAuthPub")
		PublicAuthConfig(
			authCookieName = auth.getString("authCookieName"),
			authCookieDomain = auth.getString("authCookieDomain"),
			authHost = auth.getString("authHost"),
			publicKeyPath = auth.getString("publicKeyPath")
		)
	}

	private def getMailingConfig(allConf: Config): EmailConfig = {
		val mailing = allConf.getConfig("mailing")
		EmailConfig(
			smtpServer = mailing.getString("smtpServer"),
			username = mailing.getString("username"),
			password = mailing.getString("password"),
			fromAddress = mailing.getString("fromAddress")
		)
	}
}