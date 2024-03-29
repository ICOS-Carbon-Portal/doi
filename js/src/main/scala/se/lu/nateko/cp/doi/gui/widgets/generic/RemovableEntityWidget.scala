package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap

class RemovableEntityWidget[E](
	widgetFactory: (E => Unit) => EntityWidget[E],
	init: E,
	updateCb: E => Unit,
	removeCb: RemovableEntityWidget[E] => Unit
){
	private[this] var _entityValue: E = init
	def entityValue = _entityValue

	private[this] val widget = widgetFactory((e: E) => {
		_entityValue = e
		updateCb(e)
	})

	private[this] val removeButton =
		button(tpe := "button", cls := "btn btn-warning")(
			span(cls := "fas fa-trash")
		).render

	removeButton.onclick = (_: Event) => removeCb(this)

	def setRemovability(removable: Boolean): Unit = {
		removeButton.disabled = !removable
	}

	val element = Bootstrap.basicCard(
		div(cls := "row")(
			div(cls := "col-md-11")(widget.element),
			div(cls := "col-md-1 spacyrow")(removeButton)
		)
	).render
}
