package se.lu.nateko.cp.doi.gui

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import play.api.libs.json.Json
import se.lu.nateko.cp.doi.JsonSupport._
import se.lu.nateko.cp.doi.gui.widgets.generic.TextAreaWidget
import org.scalajs.dom.Event
import scala.concurrent.Future
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.widgets.TabWidget
import se.lu.nateko.cp.doi.gui.views.Constants

class DoiJsonEditor(meta: DoiMeta, updateCb: DoiMeta => Future[Unit], tabsCb: Map[EditorTab.Value, () => Unit]) {

	private val tabs = new TabWidget(EditorTab.json, tabsCb)
	private var _json = Json.prettyPrint(Json.toJson(meta))
	private var lineCount = _json.linesIterator.length
	private val editor = new TextAreaWidget(_json, v => {
		_json = v
		errorMessages.innerHTML = ""
	})(rows := lineCount, whiteSpace := "pre")

	private val errorMessages = p(color := Constants.formErrorsTextColor).render

	private val updateButton = button(tpe := "button", cls := "btn btn-primary")("Update").render
	updateButton.onclick = (_: Event) => {
		try {
			val meta = Json.parse(_json).as[DoiMeta]
			updateCb(meta)
		} catch {
			case e: Throwable => {
				errorMessages.innerText = e.getMessage
			}
		}
	}

	val element = div(
		tabs.element,
		p(
			editor.element
		),
		errorMessages,
		updateButton
	).render
}
