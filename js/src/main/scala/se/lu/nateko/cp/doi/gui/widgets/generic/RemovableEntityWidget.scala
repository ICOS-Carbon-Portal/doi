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
		button(tpe := "button", cls := "btn btn-sm btn-link m-1")(
			span(cls := "fas fa-trash")
		).render

	removeButton.title = "Remove this item"
	removeButton.onclick = (_: Event) => removeCb(this)

	def setRemovability(removable: Boolean): Unit = {
		removeButton.disabled = !removable
		removeButton.classList.add(if(removable) "text-warning" else "text-secondary")
	}

	private[this] val moveUpButton =
		button(tpe := "button", cls := "btn btn-sm btn-link m-1")(
			span(cls := "fas fa-arrow-up")
		).render

	private[this] val moveDownButton =
		button(tpe := "button", cls := "btn btn-sm btn-link m-1")(
			span(cls := "fas fa-arrow-down")
		).render

	moveUpButton.title = "Move this item up"
	moveUpButton.onclick = (_: Event) => moveCb(this, true)
	moveDownButton.title = "Move this item down"
	moveDownButton.onclick = (_: Event) => moveCb(this, false)

	def setOrderability(orderable: (Boolean, Boolean)): Unit = {
		moveUpButton.disabled = !orderable(0)
		moveDownButton.disabled = !orderable(1)
	}

	val element =
		div(cls := "row row-cols-auto")(
			div(cls := "col-md")(widget.element),
			div(cls := "col-xl-2 col-lg-3 text-end")(div(moveUpButton, moveDownButton, removeButton))
		)
	.render
}
