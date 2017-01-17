package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.console
import org.scalajs.dom.document
import org.scalajs.dom.Element
import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi

import views._
import se.lu.nateko.cp.doi.meta._

class Views(dispatch: DoiAction => Unit) {

	val listElem = ul(cls := "list-unstyled").render

	def mainLayout = {

		val refreshDoiList = (_: Event) => dispatch(DoiListRefreshRequest)

		div(id := "main")(
			div(cls := "page-header")(
				h1("Carbon Portal DOI minting service")
			),
			Bootstrap.basicPanel(
				button(cls := "btn btn-default", onclick := refreshDoiList)("Refresh DOI list")
			),
			listElem
		)
	}

}
