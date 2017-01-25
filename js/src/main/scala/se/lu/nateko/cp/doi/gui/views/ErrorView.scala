package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import org.scalajs.dom.document
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ResetErrors

class ErrorView(widthPx: Int, heightPx: Int, d: DoiRedux.Dispatcher) {

	private[this] val errPadding = 15

	private[this] val errorList = div(
		width := widthPx - 2 * errPadding,
		color := Constants.formErrorsTextColor
	).render

	private val continue: () => Unit = () => d.dispatch(ResetErrors)

	private[this] val element = div(
		display := "none", position := "fixed",
		top := 0, bottom := 0,
		left := 0, right := 0,
		width := widthPx, height := heightPx,
		margin := "auto", padding := errPadding,
		zIndex := 10000,
		backgroundColor := Constants.errorInputBackground
	)(
		errorList,
		button(
			tpe := "button", cls := "btn btn-danger",
			onclick := continue,
			position := "absolute",
			right := errPadding, bottom := errPadding
		)("Continue")
	).render

	document.body.appendChild(element)

	def clearErrors(): Unit = {
		element.style.display = "none"
		errorList.innerHTML = ""
	}

	def appendError(msg: String): Unit = {

		val errorMessage = if(msg == null || msg.isEmpty) "Unknown error" else msg

		for(messageLine <- errorMessage.split("\n")){
			errorList.appendChild(
				p(messageLine).render
			)
		}

		element.style.display = "inline-block"
	}
}
