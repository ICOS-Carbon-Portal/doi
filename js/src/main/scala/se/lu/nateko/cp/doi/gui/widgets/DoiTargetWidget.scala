package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event
import DoiTargetWidget._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.Doi

class DoiTargetWidget(init: Option[String], doi: Doi, protected val updateCb: String => Unit) extends EntityWidget[String] {

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

	private val doiUrl = "http://doi.org/" + doi

	val element = Bootstrap.defaultPanel("DOI Target")(
		Bootstrap.basicPanel(
			span(strong("Test the DOI: ")),
			a(href := doiUrl, target := "_blank")(doiUrl),
			span(" (allow up to 24 hours synchronization time after Target URL update)")
		),
		div(cls := "input-group")(
			span(cls := "input-group-addon")("Target URL"),
			urlInput,
			div(cls := "input-group-btn")(
				updateButton,
				button(cls := "btn btn-default", tpe := "button", onclick := resetTarget)("Reset")
			)
		)
	).render

	def refreshUrl(url: String): Unit = {
		_target = url
		resetTarget(null)
	}
}

object DoiTargetWidget{
	private val urlRegex = """^https://([\w\-]+\.)?([\w\-]+)\.([^\./]+)/.*$""".r
	private val allowed = Set("icos-cp.eu", "icos-etc.eu", "fieldsites.se")

	def targetUrlError(uri: String): Option[String] = urlRegex.unapplySeq(uri).map(_.reverse) match {
		case Some(l1 :: l2 :: _) =>
			val domain = l2 + "." + l1
			if(allowed.contains(domain))
				None
			else
				Some(s"Domain '$domain' is not allowed, must be one of: " + allowed.mkString(", "))
		case _ =>
			Some("Must have format https://[<subdomain>.]<domain>/[<path>]")
	}
}
