package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.html
import scalatags.JsDom.all._
import org.scalajs.dom.HTMLElement
import se.lu.nateko.cp.doi.gui.views.Constants

trait EntityWidget[E]{
	def element: html.Element
	protected def updateCb: E => Unit

	def highlightError(elem: HTMLElement, errorOpt: Option[String]): Unit = errorOpt match{
		case Some(err) =>
			elem.title = err
			elem.className = "form-control is-invalid"
		case None =>
			elem.title = ""
			elem.className = "form-control is-valid"
	}
}
