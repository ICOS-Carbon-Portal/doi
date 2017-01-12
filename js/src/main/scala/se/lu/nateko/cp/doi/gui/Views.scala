package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.console
import org.scalajs.dom.document
import org.scalajs.dom.Element
import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi

import Views._

class Views(dispatch: DoiAction => Unit) {

	val listElem = ul(cls := "list-unstyled").render

	def mainLayout = {

		val refreshDoiList = (_: Event) => dispatch(DoiListRefreshRequest)

		div(id := "main")(
			div(cls := "page-header")(
				h1("Carbon Portal DOI minting service")
			),
			basicPanel(
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


	def targetUrlEditWidget(doi: Doi, target: Option[String]) = {

		val urlValOrPlaceholder = target match{
			case None => placeholder := "NOT MINTED"
			case Some(url) => value := url
		}

		val targetUrlInput = input(tpe := "text", cls := "form-control", urlValOrPlaceholder).render
		val updateTargetButton = button(tpe := "button", disabled := true)("Update").render

		def validateTargetUrl(): Unit = {
			val candidateUrl = Option(targetUrlInput.value).getOrElse("")

			val isValid = isValidTargetUrl(candidateUrl)
			val canUpdate = isValid && !target.contains(candidateUrl)

			updateTargetButton.disabled = !canUpdate
			updateTargetButton.className = "btn btn-" + (if(canUpdate) "primary" else "default")
			if(!isValid){
				targetUrlInput.title = "Mush have format https://[<subdomain>.]icos-cp.eu/[<path>]"
				targetUrlInput.style.backgroundColor = "#ffaaaa"
			} else {
				targetUrlInput.title = ""
				targetUrlInput.style.backgroundColor = ""
			}
		}
		validateTargetUrl()

		targetUrlInput.onkeyup = (_: Event) => validateTargetUrl()

		updateTargetButton.onclick = (_: Event) => {
			val url = targetUrlInput.value
			dispatch(TargetUrlUpdateRequest(doi, url))
		}

		val resetTarget = (_: Event) => {
			targetUrlInput.value = target.getOrElse("")
			validateTargetUrl()
		}

		div(cls := "input-group")(
			span(cls := "input-group-addon")("Target URL"),
			targetUrlInput,
			div(cls := "input-group-btn")(
				updateTargetButton,
				button(cls := "btn btn-default", tpe := "button", onclick := resetTarget)("Reset")
			)
		)
	}

	def doiInfoPanelBody(info: DoiInfo) = {
		div(cls := "panel-body")(
			basicPanel(
				targetUrlEditWidget(info.meta.id, info.target)
			),
			div(cls := "input-group")(
				span(cls := "input-group-addon")("Publication year"),
				input(tpe := "text", cls := "form-control", value := info.meta.publicationYear)
			)
			//basicPanel(textarea(info.meta.toString))
		)
	}

	def doiElem(doi: Doi, selected: Option[SelectedDoi]) = {

		val selectDoi: Event => Unit = e => dispatch(SelectDoi(doi))

		val heading = div(cls := "panel-heading", onclick := selectDoi)(
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

	def getDoiElem(doi: Doi) = Option(document.getElementById(doi.toString))

}

object Views{

	def basicPanel(body: TypedTag[Element]*): TypedTag[html.Div] =
		div(cls := "panel panel-default")(
			div(cls := "panel-body")(body)
		)

	private val urlRegex = """^https://(\w+\.)?icos-cp\.eu/.*$""".r
	def isValidTargetUrl(uri: String): Boolean = urlRegex.findFirstIn(uri).isDefined
}
