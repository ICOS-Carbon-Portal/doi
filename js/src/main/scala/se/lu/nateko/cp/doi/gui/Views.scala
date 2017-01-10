package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.document
import org.scalajs.dom.Element
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi

class Views(dispatch: DoiAction => Unit) {

	val doiListId = "doilist"

	val refreshDoiList: Event => Unit = e => dispatch(DoiListRefreshRequest)
	def selectDoi(doi: Doi): Event => Unit = e => dispatch(SelectDoi(doi))

	def mainLayout = div(id := "main")(
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

	def doiInfoPanelBody(info: DoiInfo) = {
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

	def doiElem(doi: Doi, selected: Option[SelectedDoi]) = {
		val heading = div(cls := "panel-heading", onclick := selectDoi(doi))(
			doiListIcon(selected.exists(_.doi == doi)),
			span(" " + doi.toString)
		)

		val body = selected.filter(_.doi == doi).flatMap(_.info).map(doiInfoPanelBody).toList

		div(
			cls := "panel panel-default",
			id := doi.toString
		)(
			heading +: body
		)
	}

	def getListElem = document.getElementById(doiListId)
	def getDoiElem(doi: Doi) = Option(document.getElementById(doi.toString))

}
