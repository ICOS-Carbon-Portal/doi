package se.lu.nateko.cp.doi

import org.scalajs.dom.Element
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._

object FrontendViews {

	val doiListId = "doilist"

	def mainLayout(refreshDoiList: Event => Unit) = div(id := "main")(
		div(cls := "page-header")(
			h1("Carbon Portal DOI minting service")
		),
		basicPanel(
			button(cls := "btn btn-default", onclick := refreshDoiList)("Refresh DOI list")
		),
		ul(cls := "list-unstyled", id := doiListId)
	)

	def basicPanel(body: TypedTag[Element]*): TypedTag[Div] =
		div(cls := "panel panel-default")(
			div(cls := "panel-body")(body)
		)

	def doiListIcon(selected: Boolean) = {
		val iconClass = "glyphicon glyphicon-triangle-" +
			(if(selected) "bottom" else "right")
		span(cls := iconClass)
	}

	def doiInfoPanelBody(info: DoiApp.DoiInfo) = {
		val valOrPlaceholder = info.target match{
			case None => placeholder := "NOT MINTED"
			case Some(target) => value := target
		}

		div(cls := "panel-body")(
			div(cls := "input-group")(
				span(cls := "input-group-addon")("Target URL"),
				input(tpe := "text", cls := "form-control", valOrPlaceholder)
			),
			basicPanel(textarea(info.meta.toString))
		)
	}
}
