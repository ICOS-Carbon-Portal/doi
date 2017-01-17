package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import org.scalajs.dom.raw.Text
import org.scalajs.dom.Event

class DoiMetaWidget(init: DoiMeta, protected val updateCb: DoiMeta => Unit, resetCb: () => Unit) extends EntityWidget[DoiMeta] {

	private[this] var _meta = init

	private[this] val creatorsEdit = new CreatorsEditWidget(init.creators, cs => {
		_meta = _meta.copy(creators = cs)
		validateMeta()
	})

	private[this] val titlesEdit = new TitlesEditWidget(init.titles, ts => {
		_meta = _meta.copy(titles = ts)
		validateMeta()
	})

	private[this] val errorMessages = new Text()

	private[this] def validateMeta(): Unit = {
		val error = _meta.error
		errorMessages.data = error.getOrElse("")
		val canSave = error.isEmpty && _meta != init
		updateButton.disabled = !canSave
		updateButton.className = "btn btn-" + (if(canSave) "primary" else "default")
	}

	private[this] val updateButton = button(tpe := "button", disabled := true)("Update").render
	updateButton.onclick = (_: Event) => {
		updateCb(_meta)
	}

	private[this] val resetButton = button(tpe := "button", cls := "btn btn-default")("Reset").render
	resetButton.onclick = (_: Event) => resetCb()

	val element = Bootstrap.defaultPanel("DOI Metadata")(
		creatorsEdit.element,
		titlesEdit.element,
		div(color := "#b00")(errorMessages),
		div(cls := "btn-group")(updateButton, resetButton)
	).render

	validateMeta()
}
