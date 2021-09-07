package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import scalatags.JsDom.Modifier
import org.scalajs.dom.Element
import org.scalajs.dom.html

object Bootstrap {

	def defaultCard(title: String)(body: Modifier*): TypedTag[html.Div] =
		div(cls := "card")(
			div(cls := "card-header")(
				span(cls := "card-title")(title)
			),
			div(cls := "card-body")(body)
		)

	def basicCard(body: Modifier*): TypedTag[html.Div] =
		div(cls := "card")(
			div(cls := "card-body")(body)
		)

	def propValueRow(propHtml: Modifier*)(valHtml: Modifier*) =
		div(cls := "row")(
			div(cls := "col-md-1")(propHtml),
			div(cls := "col-md-11")(valHtml)
		)

	def basicPropValueWidget(name: String)(valHtml: Modifier*) = basicCard{
		propValueRow(strong(name))(valHtml)
	}
}