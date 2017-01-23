package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.GenericName
import se.lu.nateko.cp.doi.meta.Title
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.MultiEntitiesEditWidget
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.views.Constants
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.IntInputWidget
import se.lu.nateko.cp.doi.meta.Subject

class DoiMetaWidget(init: DoiMeta, protected val updateCb: DoiMeta => Unit) extends EntityWidget[DoiMeta] {

	private[this] var _meta = init

	private def formElements: Seq[Div] = Seq(

		new CreatorsEditWidget(init.creators, cb(cs => _.copy(creators = cs))).element.render,

		new TitlesEditWidget(init.titles, cb(ts => _.copy(titles = ts))).element.render,

		Bootstrap.basicPropValueWidget("Publisher")(
			new TextInputWidget(init.publisher, cb(pub => _.copy(publisher = pub))).element
		).render,

		Bootstrap.basicPropValueWidget("Publication year")(
			new IntInputWidget(init.publicationYear, cb(pub => _.copy(publicationYear = pub))).element
		).render,

		Bootstrap.basicPropValueWidget("Resource type")(
			new ResourceTypeWidget(init.resourceType, cb(rt => _.copy(resourceType = rt))).element
		).render,

		new SubjectsEditWidget(init.subjects, cb(ss => _.copy(subjects = ss))).element.render
	)

	private def cb[T](upd: T => DoiMeta => DoiMeta): T => Unit = prop => {
		_meta = upd(prop)(_meta)
		validateMeta()
	}

	private[this] val errorMessages = div(color := Constants.formErrorsTextColor).render

	private[this] def validateMeta(): Unit = {
		val errors = _meta.error.toSeq.flatMap(_.split("\n"))

		errorMessages.innerHTML = ""
		errors.foreach(err => errorMessages.appendChild(p(err).render))

		val canSave = errors.isEmpty && _meta != init

		updateButton.disabled = !canSave
		updateButton.className = "btn btn-" + (if(canSave) "primary" else "default")
	}

	private[this] def resetForms(): Unit = {
		_meta = init
		formElems.innerHTML = ""
		formElements.foreach(formElems.appendChild)
		validateMeta()
	}

	private[this] val updateButton = button(tpe := "button", disabled := true)("Update").render
	updateButton.onclick = (_: Event) => {
		updateButton.disabled = true
		updateCb(_meta)
	}

	private[this] val resetButton = button(tpe := "button", cls := "btn btn-default")("Reset").render
	resetButton.onclick = (_: Event) => resetForms()

	private[this] val formElems = div.render

	val element = Bootstrap.defaultPanel("DOI Metadata")(
		formElems,
		errorMessages,
		div(cls := "btn-group")(updateButton, resetButton)
	).render

	resetForms()
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

class SubjectsEditWidget(init: Seq[Subject], cb: Seq[Subject] => Unit) extends {
	protected val title = "Subjects"
	protected val minAmount = 0
} with MultiEntitiesEditWidget[Subject, SubjectWidget](init, cb){

	protected def makeWidget(value: Subject, updateCb: Subject => Unit) = new SubjectWidget(value, updateCb)

	protected def defaultValue = Subject("", None, None)
}
