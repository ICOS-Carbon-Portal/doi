package se.lu.nateko.cp.doi.gui

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import play.api.libs.json.Json
import se.lu.nateko.cp.doi.JsonSupport.given
import se.lu.nateko.cp.doi.gui.widgets.generic.TextAreaWidget
import org.scalajs.dom.Event
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import se.lu.nateko.cp.doi.gui.widgets.UnifiedToolbar
import se.lu.nateko.cp.doi.gui.views.Constants

class DoiJsonEditor(meta: DoiMeta, updateCb: DoiMeta => Future[Unit], toolbar: UnifiedToolbar) {
	private var _json = Json.prettyPrint(Json.toJson(meta))
	private var lineCount = _json.linesIterator.length
	private val editor = new TextAreaWidget(_json, v => {
		_json = v
		errorMessages.innerHTML = ""
	})(rows := lineCount, whiteSpace := "pre")

	private val errorMessages = p(color := Constants.formErrorsTextColor).render

	// Expose method to wire toolbar callbacks
	def wireToolbarCallbacks(): Unit = {
		toolbar.setUpdateButtonEnabled(true)
		toolbar.setUpdateButtonCallback { (_: Event) =>
			try {
				toolbar.setUpdateButtonEnabled(false)
				val meta = Json.parse(_json).as[DoiMeta]
				val updateFuture = updateCb(meta)
				updateFuture.foreach { _ =>
					toolbar.showSaveSuccess()
				}
				updateFuture.failed.foreach { _ => toolbar.setUpdateButtonEnabled(true) }
			} catch {
				case e: Throwable => {
					errorMessages.innerText = e.getMessage
					toolbar.setUpdateButtonEnabled(true)
				}
			}
		}
		// Other callbacks don't apply for JSON editor
		toolbar.setSubmitButtonCallback { (_: Event) => () }
	}

	val element = div(
		p(strong("Note:\u00a0"), "the structure of this JSON does not always match the JSON produced by Datacite"),
		p(
			editor.element
		),
		errorMessages
	).render
}
