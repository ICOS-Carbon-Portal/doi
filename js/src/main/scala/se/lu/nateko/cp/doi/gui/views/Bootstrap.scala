package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import scalatags.JsDom.Modifier
import org.scalajs.dom.Element
import org.scalajs.dom.html

object Bootstrap {

	def defaultPanel(title: String)(body: Modifier*): TypedTag[html.Div] =
		div(cls := "panel panel-default")(
			div(cls := "panel-heading")(
				span(cls := "panel-title")(title)
			),
			div(cls := "panel-body")(body)
		)

	def basicPanel(body: Modifier*): TypedTag[html.Div] =
		div(cls := "panel panel-default")(
			div(cls := "panel-body")(body)
		)

	def propValueRow(propHtml: Modifier*)(valHtml: Modifier*) =
		div(cls := "row")(
			div(cls := "col-md-2")(propHtml),
			div(cls := "col-md-10")(valHtml)
		)
}