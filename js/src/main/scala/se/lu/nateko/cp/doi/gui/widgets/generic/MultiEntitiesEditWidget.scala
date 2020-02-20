package se.lu.nateko.cp.doi.gui.widgets.generic

import scalatags.JsDom.all.{title => htmlTitle, _}
import scala.collection.mutable.Buffer
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import scala.collection.Seq

abstract class MultiEntitiesEditWidget[E, W <: EntityWidget[E]](
	initValues: Seq[E], cb: Seq[E] => Unit
)(protected val title: String, protected val minAmount: Int = 0){

	protected def makeWidget(value: E, updateCb: E => Unit): W
	protected def defaultValue: E
	private var isCollapsed = false

	private val widgets = Buffer.empty[RemovableEntityWidget[E]]

	private def setRemovability(): Unit = if(minAmount > 0) {
		widgets.foreach(_.setRemovability(widgets.length > minAmount))
	}

	private def notifyUpstream(): Unit = cb(widgets.map(_.entityValue))

	private val widgetsParent = div(cls := "col-md-11").render

	private def produceWidget(value: E): Unit = {
		val widgetFactory: (E => Unit) => W = makeWidget(value, _)

		val newWidget = new RemovableEntityWidget[E](widgetFactory, value, _ => notifyUpstream(), widget => {
			widgets -= widget
			widgetsParent.removeChild(widget.element)
			setRemovability()
			setCollapsedness()
			notifyUpstream()
		})
		widgetsParent.appendChild(newWidget.element)
		widgets += newWidget
	}

	private val addWidget: Event => Unit = (_: Event) => {
		produceWidget(defaultValue)
		setRemovability()
		setCollapsedness()
		notifyUpstream()
	}

	private val collapseWidget: Event => Unit = (_: Event) => {
		isCollapsed = !isCollapsed
		setCollapsedness()
	}

	private val collapseIcon = span().render
	private val collapseButton = button(
			tpe := "button",
			cls := "btn btn-default",
			onclick := collapseWidget,
			marginBottom := 5
		)(collapseIcon).render

	private val addWidgetButton = button(
			tpe := "button", cls := "btn btn-success",
			htmlTitle := "Add another item to the list",
			onclick := addWidget, marginBottom := 5
		)(
			span(cls := "glyphicon glyphicon-plus")
		).render

	private def setCollapsedness(): Unit = {
		val canCollapse: Boolean = widgetsParent.childNodes.length > 0
		collapseButton.style.display = if(canCollapse) "inline-block" else "none"
		collapseIcon.className = "glyphicon glyphicon-collapse-" + (if(isCollapsed) "down" else "up")
		collapseButton.title = if(isCollapsed) "Expand this list back down" else "Collapse this list up"
		widgetsParent.style.display = if(isCollapsed) "none" else "block"
		addWidgetButton.style.display = if(isCollapsed) "none" else "inline-block"
	}

	val element = Bootstrap.basicPanel(
		div(cls := "row")(
			div(cls := "col-md-1")(
				div(strong(title)),
				div(addWidgetButton, collapseButton)
			),
			widgetsParent
		)
	)

	initValues.foreach(produceWidget)
	setRemovability()
	setCollapsedness()

}
