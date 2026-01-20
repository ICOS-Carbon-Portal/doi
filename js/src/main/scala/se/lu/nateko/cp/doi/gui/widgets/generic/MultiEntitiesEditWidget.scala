package se.lu.nateko.cp.doi.gui.widgets.generic

import scalatags.JsDom.all.{title => htmlTitle, _}
import scala.collection.mutable.Buffer
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import scala.collection.Seq

abstract class MultiEntitiesEditWidget[E, W <: EntityWidget[E]](
	initValues: Seq[E], cb: Seq[E] => Unit
)(protected val title: String, protected val minAmount: Int = 0, protected val maxAmount: Int = 0){

	protected def makeWidget(value: E, updateCb: E => Unit): W
	protected def defaultValue: E

	private val widgets = Buffer.empty[RemovableEntityWidget[E]]

	private def setRemovability(): Unit = 
		widgets.foreach(_.setRemovability(widgets.length > minAmount))

	private def setOrderability(): Unit = {
		widgets.foreach(widget => {
			var orderability = (false, false)
			if (widget.element.previousSibling != null) {
				orderability = (true, orderability(1))
			}
			if (widget.element.nextSibling != null) {
				orderability = (orderability(0), true)
			}
			widget.setOrderability(orderability)
		})
	}

	private def setAppendability(): Unit = if(maxAmount > 0) {
		addWidgetButton.disabled = widgets.length >= maxAmount
	}

	private def notifyUpstream(): Unit = cb(widgets.map(_.entityValue))

	private val widgetsParent = div().render

	private def produceWidget(value: E): Unit = {
		val widgetFactory: (E => Unit) => W = makeWidget(value, _)

		val newWidget = new RemovableEntityWidget[E](widgetFactory, value, _ => notifyUpstream(), widget => {
			widgets -= widget
			widgetsParent.removeChild(widget.element)
			setRemovability()
			setOrderability()
			setAppendability()
			notifyUpstream()
		}, (initiatingWidget, moveWidgetUp) => {
			val initiatingWidgetIndex = widgets.indexOf(initiatingWidget)
			if (moveWidgetUp) {
				widgetsParent.insertBefore(initiatingWidget.element, initiatingWidget.element.previousSibling)
				val targetWidget = widgets.apply(initiatingWidgetIndex - 1)
				widgets.update(initiatingWidgetIndex, targetWidget)
				widgets.update(initiatingWidgetIndex - 1, initiatingWidget)
			} else {
				widgetsParent.insertBefore(initiatingWidget.element.nextSibling, initiatingWidget.element)
				val targetWidget = widgets.apply(initiatingWidgetIndex + 1)
				widgets.update(initiatingWidgetIndex, targetWidget)
				widgets.update(initiatingWidgetIndex + 1, initiatingWidget)
			}
			setOrderability()
			notifyUpstream()
		})
		widgetsParent.appendChild(newWidget.element)
		widgets += newWidget
	}

	private val addWidget: Event => Unit = (_: Event) => {
		produceWidget(defaultValue)
		setAppendability()
		setRemovability()
		setOrderability()
		notifyUpstream()
	}

	private val addWidgetButton = button(
			tpe := "button", cls := "btn btn-sm btn-outline-primary mt-2",
			htmlTitle := "Add another item to the list",
			onclick := addWidget, marginBottom := 5
		)(
			span(cls := "fas fa-plus me-1"),
			"Add " + title.dropRight(1).toLowerCase
		).render

	val element =
		div(cls := "row")(
			div(cls := "col-md-2")(
				div(cls := "fw-bold pt-2")(title)
			),
			div(cls := "col-md-10")(
				widgetsParent,
				addWidgetButton
			)
		)

	initValues.foreach(produceWidget)
	setRemovability()
	setOrderability()
	setAppendability()

}
