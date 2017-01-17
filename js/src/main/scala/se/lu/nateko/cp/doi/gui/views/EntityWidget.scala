package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.all._
import org.scalajs.dom.raw.HTMLElement

trait EntityWidget[E]{
	def element: html.Element
	protected def updateCb: E => Unit

	def highlightError(elem: HTMLElement, errorOpt: Option[String]): Unit = errorOpt match{
		case Some(err) =>
			elem.title = err
			elem.style.background = Constants.errorInputBackground
		case None =>
			elem.title = ""
			elem.style.background = ""
	}
}
