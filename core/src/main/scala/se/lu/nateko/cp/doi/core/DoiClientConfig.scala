package se.lu.nateko.cp.doi.core

import java.net.URI

trait DoiEndpointConfig:
	def restEndpoint: URI

case class DoiMemberConfig(symbol: String, password: String, doiPrefix: String)

case class DoiClientConfig(restEndpoint: URI, member: DoiMemberConfig) extends DoiEndpointConfig
