package se.lu.nateko.cp.doi

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import se.lu.nateko.cp.doi.core.DoiClientConfig
import java.net.URL

object DoiConfig {

	def getAppConfig: Config = {
		val default = ConfigFactory.load
		val confFile = new java.io.File("application.conf").getAbsoluteFile
		println("Looking for config in " + confFile)
		if(!confFile.exists) default
		else ConfigFactory.parseFile(confFile).withFallback(default)
	}

	def getClientConfig: DoiClientConfig = {
		val conf = getAppConfig.getConfig("cpdoi")
		DoiClientConfig(
			symbol = conf.getString("symbol"),
			password = conf.getString("password"),
			endpoint = new URL(conf.getString("endpoint")),
			doiPrefix = conf.getString("doiPrefix")
		)
	}
}