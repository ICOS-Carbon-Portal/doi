package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.Doi
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import se.lu.nateko.cp.doi.gui.widgets.generic._
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.views.Constants
import se.lu.nateko.cp.doi.gui.Backend
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.collection.Seq

import DoiMetaWidget._
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class DoiMetaWidget(
	init: DoiMeta,
	updater: DoiMeta => Future[Unit],
	cloneCb: DoiMeta => Unit,
	deleteCb: Doi => Unit
) extends EntityWidget[DoiMeta] with SelfValidating{

	private[this] var _meta = init
	def error = withUrlError(_meta.error)

	protected val updateCb: DoiMeta => Unit = _ => ???//dummy, not used here

	private def withUrlError(err: Option[String]): Option[String] =
		joinErrors(err, _meta.url.flatMap(DoiTargetWidget.targetUrlError))

	private def formElements: Seq[Div] = Seq(

		new CreatorsEditWidget(init.creators, cb(cs => _.copy(creators = cs))).element.render,

		new TitlesEditWidget(init.titles.getOrElse(Seq()), cb(ts => _.copy(titles = Some(ts)))).element.render,

		Bootstrap.basicPropValueWidget("Publisher")(
			new TextInputWidget(init.publisher.getOrElse(""), cb(pub => _.copy(publisher = Some(pub)))).element
		).render,

		Bootstrap.basicPropValueWidget("Publication year")(
			new IntInputWidget(init.publicationYear.getOrElse(0), cb(pub => _.copy(publicationYear = Some(pub)))).element
		).render,

		Bootstrap.basicPropValueWidget("Resource type")(
			new ResourceTypeWidget(init.types.getOrElse(ResourceType(None, None)), cb(rt => _.copy(types = Some(rt)))).element
		).render,

		new SubjectsEditWidget(init.subjects, cb(ss => _.copy(subjects = ss))).element.render,

		new ContributorsEditWidget(init.contributors, cb(cs => _.copy(contributors = cs))).element.render,

		new DatesEditWidget(init.dates, cb(ds => _.copy(dates = ds))).element.render,

		new FormatsEditWidget(init.formats, cb(fs => _.copy(formats = fs))).element.render,

		Bootstrap.basicPropValueWidget("Version")(
			new VersionWidget(init.version, cb(v => _.copy(version = v))).element
		).render,

		new RightsEditWidget(init.rightsList.getOrElse(Seq()), cb(rs => _.copy(rightsList = Some(rs)))).element.render,

		new DescriptionsEditWidget(init.descriptions, cb(ds => _.copy(descriptions = ds))).element.render,

		new DoiTargetWidget(init.url, init.doi, cb(t => _.copy(url = t))).element

	)

	private def cb[T](upd: T => DoiMeta => DoiMeta): T => Unit = prop => {
		_meta = upd(prop)(_meta)
		validateMeta()
		resetButton.disabled = false
	}

	private[this] val errorMessages = div(color := Constants.formErrorsTextColor).render

	private def validateMeta(): Unit = {
		val errors = error.toSeq.flatMap(_.split("\n"))

		errorMessages.innerHTML = ""
		errors.foreach(err => errorMessages.appendChild(p(err).render))

		val canUpdate = _meta != init && {
			if(_meta.state == DoiPublicationState.draft) withUrlError(_meta.draftError).isEmpty
			else errors.isEmpty
		}

		updateButton.disabled = !canUpdate
		publishButton.disabled = !errors.isEmpty
		submitButton.disabled = !errors.isEmpty
		updateButton.className = "btn doi-update btn-" + (if(canUpdate) "primary" else "secondary")
	}

	private def resetForms(): Unit = {
		_meta = init
		formElems.innerHTML = ""
		formElements.foreach(formElems.appendChild)
		validateMeta()
		resetButton.disabled = true
	}

	private[this] val updateButton = button(tpe := "button", disabled := true)("Update").render
	updateButton.onclick = (_: Event) => {
		updateButton.disabled = true
		updater(_meta).failed.foreach{_ => updateButton.disabled = false}
	}

	private[this] val submitButton = button(tpe := "button", cls := "btn btn-secondary btn-submit")("Submit for publication").render
	submitButton.onclick = (_: Event) => {
		val originalText = submitButton.textContent
		submitButton.textContent = "Submitting..."
		submitButton.disabled = true
		updater(_meta).map{ _ =>
			Backend.submitForPublication(_meta.doi)
		}.andThen{
			case Failure(exc) =>
				submitButton.textContent = originalText
				submitButton.disabled = false
				errorMessages.appendChild(p(exc.getMessage()).render)
			case Success(_) =>
				submitButton.textContent = "Submitted"
		}
	}

	private[this] val publishButton = button(tpe := "button", cls := "btn btn-secondary btn-changestate")("Publish").render
	publishButton.onclick = (_: Event) => {
		publishButton.disabled = true
		updater(
			_meta.copy(event = Some(DoiPublicationEvent.publish))
		).failed.foreach{
			_ => publishButton.disabled = false
		}
	}

	private[this] val cloneButton = button(tpe := "button", cls := "btn btn-secondary")("Clone").render
	cloneButton.onclick = (_: Event) => cloneCb(_meta)

	private[this] val deleteButton = button(tpe := "button", cls := "btn btn-secondary btn-changestate")("Delete").render
	deleteButton.onclick = (_: Event) => {
		deleteButton.disabled = true
		deleteCb(_meta.doi)
	}

	private[this] val resetButton = button(tpe := "button", cls := "btn btn-secondary", disabled := true)("Reset").render
	resetButton.onclick = (_: Event) => resetForms()

	private[this] val formElems = div.render

	private[this] val buttons = {
		_meta.state match {
			case DoiPublicationState.draft =>
				div(cls := "row")(
					div(cls := "col-auto me-auto btn-group draft-controls")(deleteButton, resetButton),
					div(cls := "col-auto btn-group draft-controls ms-auto")(cloneButton, publishButton, submitButton, updateButton)
				)
			case _ =>
				div(cls := "row")(
					div(cls := "col-auto me-auto btn-group draft-controls")(resetButton),
					div(cls := "col-auto btn-group draft-controls pull-right")(cloneButton, updateButton)
				)
		}
	}

	val element = div(
		formElems,
		errorMessages,
		buttons
	).render

	resetForms()
}

object DoiMetaWidget{

	class CreatorsEditWidget(init: Seq[Creator], cb: Seq[Creator] => Unit) extends
		MultiEntitiesEditWidget[Creator, CreatorWidget](init, cb)("Creators", 1){

		protected def makeWidget(value: Creator, updateCb: Creator => Unit) = new CreatorWidget(value, updateCb)

		protected def defaultValue = Creator(GenericName(""), Nil, Nil)
	}


	class TitlesEditWidget(initTitles: Seq[Title], cb: Seq[Title] => Unit) extends
		MultiEntitiesEditWidget[Title, TitleWidget](initTitles, cb)("Titles", 1){

		protected def makeWidget(value: Title, updateCb: Title => Unit) = new TitleWidget(value, updateCb)

		protected def defaultValue = Title("", None, None)
	}


	class SubjectsEditWidget(init: Seq[Subject], cb: Seq[Subject] => Unit) extends
		MultiEntitiesEditWidget[Subject, SubjectWidget](init, cb)("Subjects"){

		protected def makeWidget(value: Subject, updateCb: Subject => Unit) = new SubjectWidget(value, updateCb)

		protected def defaultValue = Subject("", None, None)
	}

	class ContributorsEditWidget(init: Seq[Contributor], cb: Seq[Contributor] => Unit) extends
		MultiEntitiesEditWidget[Contributor, ContributorWidget](init, cb)("Contributors"){

		protected def makeWidget(value: Contributor, updateCb: Contributor => Unit) = new ContributorWidget(value, updateCb)

		protected def defaultValue = Contributor(GenericName(""), Nil, Nil, null)
	}


	class DatesEditWidget(init: Seq[Date], cb: Seq[Date] => Unit) extends
		MultiEntitiesEditWidget[Date, DateWidget](init, cb)("Dates"){

		protected def makeWidget(value: Date, updateCb: Date => Unit) = new DateWidget(value, updateCb)

		protected def defaultValue = Date("", null)
	}


	class FormatsEditWidget(init: Seq[String], cb: Seq[String] => Unit) extends
		MultiStringsWidget(init, cb, "Format")("Formats")


	class RightsEditWidget(init: Seq[Rights], cb: Seq[Rights] => Unit) extends
		MultiEntitiesEditWidget[Rights, RightsWidget](init, cb)("Rights"){

		protected def makeWidget(value: Rights, updateCb: Rights => Unit) = new RightsWidget(value, updateCb)

		protected def defaultValue = Rights("", None)
	}


	class DescriptionsEditWidget(init: Seq[Description], cb: Seq[Description] => Unit) extends
		MultiEntitiesEditWidget[Description, DescriptionWidget](init, cb)("Descriptions"){

		protected def makeWidget(value: Description, updateCb: Description => Unit) = new DescriptionWidget(value, updateCb)

		protected def defaultValue = Description("", null, None)
	}

}
