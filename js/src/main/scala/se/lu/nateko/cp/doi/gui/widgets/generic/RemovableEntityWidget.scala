package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap

class RemovableEntityWidget[E](
	widgetFactory: (E => Unit) => EntityWidget[E],
	init: E,
	updateCb: E => Unit,
	removeCb: RemovableEntityWidget[E] => Unit,
	moveCb: (RemovableEntityWidget[E], Boolean) => Unit
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

	private[this] val moveUpButton =
		button(tpe := "button", cls := "btn btn-info")(
			span(cls := "fas fa-chevron-up")
		).render

	private[this] val moveDownButton =
		button(tpe := "button", cls := "btn btn-info")(
			span(cls := "fas fa-chevron-down")
		).render

	moveUpButton.onclick = (_: Event) => moveCb(this, true)
	moveDownButton.onclick = (_: Event) => moveCb(this, false)

	def setOrderability(orderable: (Boolean, Boolean)): Unit = {
		moveUpButton.disabled = !orderable(0)
		moveDownButton.disabled = !orderable(1)
	}

	val element = Bootstrap.basicCard(
		div(cls := "row")(
			div(cls := "col-md-11")(widget.element),
			div(cls := "col-md-1 spacyrow")(moveUpButton, removeButton, moveDownButton)
		)
	).render
}
