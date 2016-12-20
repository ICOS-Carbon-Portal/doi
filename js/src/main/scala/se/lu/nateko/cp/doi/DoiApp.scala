package se.lu.nateko.cp.doi

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.JSApp
import org.scalajs.dom
import dom.document
import org.scalajs.dom.raw.Event
import scalatags.JsDom.all._

object DoiApp extends JSApp {
	def main(): Unit = {
		val mainDiv = document.getElementById("main")
		mainDiv.innerHTML = mainLayout.toString
	}

	def mainLayout = div(
		div(cls := "page-header")(
			h1("Carbon Portal DOI minting service"),
			p(attr("hidden") := "false")("I am hidden")
		)
	)

}


