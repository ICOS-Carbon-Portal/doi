package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import scala.collection.mutable.Buffer
import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.TypedTag

abstract class MultiEntitiesEditWidget[E, W <: EntityWidget[E]](initValues: Seq[E], cb: Seq[E] => Unit){

	protected val title: String
	protected def makeWidget(value: E, updateCb: E => Unit): W
	protected def defaultValue: E
	protected val minAmount: Int

	private val widgets = Buffer.empty[RemovableEntityWidget[E]]

	private def setRemovability(): Unit = widgets.foreach(_.setRemovability(widgets.length > minAmount))

	private def notifyUpstream(): Unit = cb(widgets.map(_.entityValue))

	private val widgetsParent = div(cls := "col-md-10").render

	private def produceWidget(value: E): Unit = {
		val widgetFactory: (E => Unit) => W = makeWidget(value, _)

		val newWidget = new RemovableEntityWidget[E](widgetFactory, _ => notifyUpstream(), widget => {
			widgets -= widget
			widgetsParent.removeChild(widget.element)
			setRemovability()
			notifyUpstream()
		})
		widgetsParent.appendChild(newWidget.element)
		widgets += newWidget
	}
	initValues.foreach(produceWidget)
	setRemovability()

	private val addWidget: Event => Unit = (_: Event) => {
		produceWidget(defaultValue)
		setRemovability()
		notifyUpstream()
	}

	val element = Bootstrap.basicPanel(
		div(cls := "row")(
			div(cls := "col-md-2")(
				span(strong(title)),
				button(tpe := "button", cls := "btn btn-success pull-right", onclick := addWidget)(
					span(cls := "glyphicon glyphicon-plus")
				)
			),
			widgetsParent
		)
	)

}
