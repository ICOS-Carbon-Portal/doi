package se.lu.nateko.cp.doi.core

import java.net.URL

trait DoiEndpointConfig:
	def restEndpoint: URL

case class DoiMemberConfig(symbol: String, password: String, doiPrefix: String)

case class DoiClientConfig(restEndpoint: URL, member: DoiMemberConfig) extends DoiEndpointConfig
