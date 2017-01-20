package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event
import TargetUrlWidget._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget

class TargetUrlWidget(init: Option[String], protected val updateCb: String => Unit) extends EntityWidget[String] {

	private[this] var _target = init.getOrElse("")

	private[this] val urlInput = input(tpe := "text", cls := "form-control", value := _target).render
	private[this] val updateButton = button(tpe := "button", disabled := true)("Update").render

	private[this] def validateTargetUrl(): Unit = {
		val candidateUrl = Option(urlInput.value).getOrElse("")

		val targetError = targetUrlError(candidateUrl)
		val canUpdate = targetError.isEmpty && _target != candidateUrl

		updateButton.disabled = !canUpdate
		updateButton.className = "btn btn-" + (if(canUpdate) "primary" else "default")
		urlInput.placeholder = if(_target.isEmpty) "NOT MINTED" else ""
		highlightError(urlInput, targetError)
	}
	validateTargetUrl()

	urlInput.onkeyup = (_: Event) => validateTargetUrl()

	updateButton.onclick = (_: Event) => {
		updateButton.disabled = true
		updateCb(urlInput.value)
	}

	private[this] val resetTarget = (_: Event) => {
		urlInput.value = _target
		validateTargetUrl()
	}

	val element = div(cls := "input-group")(
		span(cls := "input-group-addon")("Target URL"),
		urlInput,
		div(cls := "input-group-btn")(
			updateButton,
			button(cls := "btn btn-default", tpe := "button", onclick := resetTarget)("Reset")
		)
	).render

	def refreshUrl(url: String): Unit = {
		_target = url
		resetTarget(null)
	}
}

object TargetUrlWidget{
	private val urlRegex = """^https://(\w+\.)?icos-cp\.eu/.*$""".r

	def targetUrlError(uri: String): Option[String] =
		if(urlRegex.findFirstIn(uri).isDefined)
			None
		else Some("Mush have format https://[<subdomain>.]icos-cp.eu/[<path>]")
}
