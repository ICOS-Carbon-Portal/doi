package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.Doi
import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.gui.TargetUrlUpdateRequest

object TargetUrlEditWidget {

	def apply(dispatch: DoiAction => Unit, doi: Doi, target: Option[String]) = {
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

	private val urlRegex = """^https://(\w+\.)?icos-cp\.eu/.*$""".r
	def isValidTargetUrl(uri: String): Boolean = urlRegex.findFirstIn(uri).isDefined
}
