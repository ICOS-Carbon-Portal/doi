package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import org.scalajs.dom.document

object DoiMetaHelpers {

	def stateDotClass(state: DoiPublicationState): String = state match {
		case DoiPublicationState.draft => "state-dot state-dot-draft"
		case DoiPublicationState.registered => "state-dot state-dot-registered"
		case DoiPublicationState.findable => "state-dot state-dot-findable"
	}

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
