package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.all._

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
			span(cls := "glyphicon glyphicon-remove")
		).render

	removeButton.onclick = (_: Event) => removeCb(this)

	def setRemovability(removable: Boolean): Unit = {
		removeButton.disabled = !removable
	}

	val element = Bootstrap.basicPanel(
		div(cls := "row")(
			div(cls := "col-md-11")(widget.element),
			div(cls := "col-md-1")(removeButton)
		)
	).render
}
