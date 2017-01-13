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

	def doiListIcon(selected: Boolean) = {
		val iconClass = "glyphicon glyphicon-triangle-" +
			(if(selected) "bottom" else "right")
		span(cls := iconClass)
	}

	def doiInfoPanelBody(info: DoiInfo) = {
		val doi = info.meta.id
		val doiUrl = "http://doi.org/" + doi

		div(cls := "panel-body")(
			Bootstrap.defaultPanel("DOI Metadata")(
				new TitlesEditWidget(info.meta.titles, ts => {console.log(ts.toString)}).element
			),
			Bootstrap.defaultPanel("DOI Target")(
				Bootstrap.basicPanel(
					span(strong("Test the DOI: ")),
					a(href := doiUrl, target := "_blank")(doiUrl)
				),
				TargetUrlEditWidget(dispatch, doi, info.target)
			)
		)
	}

	def doiElem(doi: Doi, selected: Option[SelectedDoi]) = {

		val selectDoi: Event => Unit = e => dispatch(SelectDoi(doi))

		val heading = div(cls := "panel-heading", onclick := selectDoi)(
			doiListIcon(selected.exists(_.doi == doi)),
			span(cls := "panel-title")(" " + doi.toString)
		)

		val body = selected.filter(_.doi == doi).flatMap(_.info).map(doiInfoPanelBody).toList

		div(
			cls := "panel panel-default",
			id := doi.toString
		)(
			heading +: body
		)
	}

	def getDoiElem(doi: Doi) = Option(document.getElementById(doi.toString))

}
