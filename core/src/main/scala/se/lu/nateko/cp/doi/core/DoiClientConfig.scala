package se.lu.nateko.cp.doi.core

import java.net.URL

case class DoiClientConfig(
	symbol: String,
	password: String,
	restEndpoint: URL,
	doiPrefix: String
)