package se.lu.nateko.cp.doi

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.JSApp
import org.scalajs.dom
import dom.document
import org.scalajs.dom.raw.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta.Title
import se.lu.nateko.cp.doi.meta.TitleType

object DoiApp extends JSApp {
	def main(): Unit = {
		val mainDiv = document.getElementById("main")
		mainDiv.innerHTML = mainLayout.toString
	}

	def mainLayout = div(
		div(cls := "page-header")(
			h1("Carbon Portal DOI minting service"),
			p(depTest)
		)
	)

	def depTest: String = {
		val title = Title("Some title", Some("en-us"), Some(TitleType.Subtitle))
		title.toString
	}

}


