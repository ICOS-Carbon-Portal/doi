package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.Doi

class PublishToProductionWidget(productionPrefix: String, init: Doi, protected val updateCb: Doi => Unit) extends EntityWidget[Doi] {

	private[this] var _doi = init.copy(prefix = productionPrefix)

	private[this] val suffixInput = input(tpe := "text", cls := "form-control", value := _doi.suffix).render
	private[this] val publishButton = button(
		tpe := "button",
		title := "OVERRIDES DOI IN PRODUCTION",
		onclick := {() => updateCb(_doi) }
	)("Publish").render

	private[this] def validateDoi(): Unit = {
		val error = _doi.error

		publishButton.disabled = error.isDefined
		publishButton.className = "btn btn-" + (if(error.isEmpty) "danger" else "default")
		highlightError(suffixInput, error)
	}
	validateDoi()

	suffixInput.onkeyup = (_: Event) => {
		val suff = Option(suffixInput.value).getOrElse("").toUpperCase
		suffixInput.value = suff
		_doi = _doi.copy(suffix = suff)
		validateDoi()
	}

	val element = Bootstrap.defaultCard("Publish DOI with production prefix")(
		div(cls := "input-group")(
			span(cls := "input-group-text")(productionPrefix),
			suffixInput,
			div(cls := "input-group-btn")(
				publishButton
			)
		)
	).render

}
