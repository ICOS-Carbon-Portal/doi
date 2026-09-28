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
	fromAddress: String,
	toAddresses: Seq[UserId]
)

case class ClientEnvConfig(
	client: DoiClientConfig,
	prefixInfo: String
)

case class DoiConfig(
	envri: Envri,
	httpBindInterface: String,
	httpBindPort: Int,
	envConfigs: Map[String, ClientEnvConfig],
	auth: PublicAuthConfig,
	admins: Seq[UserId],
	mailing: EmailConfig,
	metaHost: String,
	publicHost: String,
	development: Boolean,
	developmentUser: Option[UserId],
	skipCacheInvalidation: Boolean
)

object DoiConfig {

	private val envNames = Seq("test", "production")

	def getConfig: DoiConfig = {
		val allConf = ConfigLoader.appConfig

		val doiConf = allConf.getConfig("cpdoi")
		val envri = Envri.valueOf(doiConf.getString("envri"))
		val development = doiConf.getBoolean("development")
		val developmentUser =
			if development && doiConf.hasPath("developmentUser") then Some(UserId(doiConf.getString("developmentUser")))
			else None
		val envriConf = doiConf.getConfig(s"envriConfigs.$envri")

		val envConfigs = envNames
			.filter(envriConf.hasPath)
			.map(name => name -> getClientEnvConfig(envriConf.getConfig(name)))
			.toMap

		DoiConfig(
			envri = envri,
			httpBindInterface = doiConf.getString("httpBindInterface"),
			httpBindPort = doiConf.getInt("httpBindPort"),
			envConfigs = envConfigs,
			auth = ConfigLoader.authPubConfig(envri),
			admins = allConf.getStringList("cpdoi.admins").asScala.map(UserId(_)).toIndexedSeq,
			mailing = getMailingConfig(envriConf),
			metaHost = envriConf.getString("metaHost"),
			publicHost = envriConf.getString("publicHost"),
			development = development,
			developmentUser = developmentUser,
			skipCacheInvalidation = doiConf.getBoolean("skipCacheInvalidation")
		)
	}

	private def getClientEnvConfig(envConf: Config) = ClientEnvConfig(
		client = DoiClientConfig(
			restEndpoint = new URI(envConf.getString("restEndpoint")),
			member = DoiMemberConfig(
				symbol = envConf.getString("member.symbol"),
				password = envConf.getString("member.password"),
				doiPrefix = envConf.getString("member.prefix")
			)
		),
		prefixInfo = envConf.getString("member.prefix")
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
			fromAddress = mailing.getString("fromAddress"),
			toAddresses = mailing.getStringList("toAddresses").asScala.map(UserId(_)).toIndexedSeq
		)
	}
}