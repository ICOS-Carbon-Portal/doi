package se.lu.nateko.cp.doi

import org.scalajs.dom.html.Div
import scalatags.JsDom.TypedTag
import org.scalajs.dom.Element
import scalatags.JsDom.all._

object FrontendViews {

	def basicPanel(body: TypedTag[Element]*): TypedTag[Div] =
		div(cls := "panel panel-default")(
			div(cls := "panel-body")(body)
		)
}