package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Rights

object Constants {

	val errorInputBackground = "#faa"

	val formErrorsTextColor = "#b00"

	val ccBy4Rights = Rights(
		rights = "Creative Commons Attribution 4.0 International",
		rightsUri = Some("https://creativecommons.org/licenses/by/4.0/legalcode"),
		schemeUri = Some("https://spdx.org/licenses/"),
		rightsIdentifier = Some("CC-BY-4.0"),
		rightsIdentifierScheme = Some("SPDX"),
		lang = Some("eng")
	)

	val cc0Rights = Rights(
		rights = "Creative Commons Zero v1.0 Universal",
		rightsUri = Some("https://creativecommons.org/publicdomain/zero/1.0/legalcode"),
		schemeUri = Some("https://spdx.org/licenses/"),
		rightsIdentifier = Some("CC0-1.0"),
		rightsIdentifierScheme = Some("SPDX"),
		lang = Some("eng")
	)
}
