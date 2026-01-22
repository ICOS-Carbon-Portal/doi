package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.DoiMeta
import org.scalajs.dom.document

object DoiMetaHelpers {
	def extractTitle(meta: DoiMeta): String = {
		meta.titles
			.flatMap(_.headOption)
			.map(_.title)
			.getOrElse("No title")
	}

	// Page title utilities
	private lazy val mainWrapper = document.getElementById("main-wrapper")

	private lazy val devIcon: String =
		if (mainWrapper != null && mainWrapper.getAttribute("data-development") == "true") "🚧 " else ""

	private lazy val envriSuffix: String =
		if (mainWrapper != null) {
			val envri = mainWrapper.getAttribute("data-envri")
			if (envri != null && envri.nonEmpty) s" | $envri" else ""
		} else ""

	def pageTitle(title: String): String = s"$devIcon$title$envriSuffix"
}
