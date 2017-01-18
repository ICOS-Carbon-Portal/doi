package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.GenericName
import se.lu.nateko.cp.doi.meta.Title
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.MultiEntitiesEditWidget
import se.lu.nateko.cp.doi.gui.views.Bootstrap

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

	private[this] val errorMessages = div(color := "#b00").render

	private[this] def validateMeta(): Unit = {
		val errors = _meta.error.toSeq.flatMap(_.split("\n"))

		errorMessages.innerHTML = ""
		errors.foreach(err => errorMessages.appendChild(p(err).render))

		val canSave = errors.isEmpty && _meta != init

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
		errorMessages,
		div(cls := "btn-group")(updateButton, resetButton)
	).render

	validateMeta()
}


class CreatorsEditWidget(init: Seq[Creator], cb: Seq[Creator] => Unit) extends {
	protected val title = "Creators"
	protected val minAmount = 1
} with MultiEntitiesEditWidget[Creator, CreatorWidget](init, cb){

	protected def makeWidget(value: Creator, updateCb: Creator => Unit) = new CreatorWidget(value, updateCb)

	protected def defaultValue = Creator(GenericName(""), Nil, Nil)
}

class TitlesEditWidget(initTitles: Seq[Title], cb: Seq[Title] => Unit) extends {
	protected val title = "Titles"
	protected val minAmount = 1
} with MultiEntitiesEditWidget[Title, TitleWidget](initTitles, cb){

	protected def makeWidget(value: Title, updateCb: Title => Unit) = new TitleWidget(value, updateCb)

	protected def defaultValue = Title("", None, None)
}
