package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import scalatags.JsDom.Modifier
import org.scalajs.dom.Element
import org.scalajs.dom.html

object Bootstrap {

	def propValueRow(propHtml: Modifier*)(valHtml: Modifier*) =
		div(cls := "row gy-2")(
			div(cls := "col-md-2")(propHtml),
			div(cls := "col-md-10")(valHtml)
		)

	def basicPropValueWidget(name: String)(valHtml: Modifier*) =
		propValueRow(strong(name))(valHtml)
	
	def singlePropValueWidget(name: String)(valHtml: Modifier*) =
		propValueRow(div(cls := "fw-bold pt-2")(name))(
			div(cls := "row")(
				div(cls := "col-xl-10 col-lg-9")(valHtml)
			)
		)
}
