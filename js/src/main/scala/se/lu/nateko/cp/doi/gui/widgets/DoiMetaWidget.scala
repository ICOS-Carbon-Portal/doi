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
	toolbar: UnifiedToolbar
) extends EntityWidget[DoiMeta] with SelfValidating{

	private[this] var _meta = init
	def error = withUrlError(_meta.error)

	protected val updateCb: DoiMeta => Unit = _ => ???//dummy, not used here

	private def withUrlError(err: Option[String]): Option[String] =
		joinErrors(err, _meta.url.flatMap(DoiTargetWidget.targetUrlError))

	private def formElements: Seq[Div] = Seq(
		
		div(cls := "row mt-5")(h4("Required properties")).render,

		new DoiTargetWidget(init.url, init.doi, cb(t => _.copy(url = t))).element,

		new CreatorsEditWidget(init.creators, cb(cs => _.copy(creators = cs))).element.render,

		new TitlesEditWidget(init.titles.getOrElse(Seq()), cb(ts => _.copy(titles = Some(ts)))).element.render,

		Bootstrap.singlePropValueWidget("Publisher")(
			new TextInputWidget(init.publisher.getOrElse(""), cb(pub => _.copy(publisher = Some(pub))), required = true).element
		).render,

		Bootstrap.singlePropValueWidget("Publication year")(
			new IntInputWidget(init.publicationYear.getOrElse(0), cb(pub => _.copy(publicationYear = Some(pub)))).element
		).render,

		Bootstrap.basicPropValueWidget("Resource type")(
			new ResourceTypeWidget(init.types.getOrElse(ResourceType(None, None)), cb(rt => _.copy(types = Some(rt)))).element
		).render,

		div(cls := "row mt-5")(h4("Recommended properties")).render,

		new SubjectsEditWidget(init.subjects, cb(ss => _.copy(subjects = ss))).element.render,

		new ContributorsEditWidget(init.contributors, cb(cs => _.copy(contributors = cs))).element.render,

		new DatesEditWidget(init.dates, cb(ds => _.copy(dates = ds))).element.render,

		new RelatedIdentifierEditWidget(init.relatedIdentifiers.getOrElse(Seq()), cb(ri => _.copy(relatedIdentifiers = Some(ri)))).element.render,

		new RightsEditWidget(init.rightsList.getOrElse(Seq()), cb(rs => _.copy(rightsList = Some(rs)))).element.render,

		new DescriptionsEditWidget(init.descriptions, cb(ds => _.copy(descriptions = ds))).element.render,

		new GeoLocationEditWidget(init.geoLocations.getOrElse(Nil), cb(gl => _.copy(geoLocations = Some(gl)))).element.render,

		div(cls := "row mt-5")(h4("Optional properties")).render,

		new FormatsEditWidget(init.formats, cb(fs => _.copy(formats = fs))).element.render,

		Bootstrap.singlePropValueWidget("Version")(
			new VersionWidget(init.version, cb(v => _.copy(version = v))).element
		).render,

		new FundingEditWidget(init.fundingReferences.getOrElse(Nil), cb(fr => _.copy(fundingReferences = Some(fr)))).element.render,

	)

	private def cb[T](upd: T => DoiMeta => DoiMeta): T => Unit = prop => {
		_meta = upd(prop)(_meta)
		validateMeta()
	}

	private var toolbarInitialized = false

	private[this] val errorMessages = div(color := Constants.formErrorsTextColor).render

	private def validateMeta(): Unit = {
		val errors = error.toSeq.flatMap(_.split("\n"))

		errorMessages.innerHTML = ""
		errors.foreach(err => errorMessages.appendChild(p(err).render))

		val canUpdate = _meta != init && {
			if(_meta.state == DoiPublicationState.draft) withUrlError(_meta.draftError).isEmpty
			else errors.isEmpty
		}

		if (toolbarInitialized) {
			toolbar.setUpdateButtonEnabled(canUpdate)
			toolbar.setSubmitButtonEnabled(errors.isEmpty)
		}
	}

	private def resetForms(): Unit = {
		_meta = init
		formElems.innerHTML = ""
		formElements.foreach(formElems.appendChild)
		validateMeta()
	}

	// Expose methods to wire toolbar callbacks
	def wireToolbarCallbacks(): Unit = {
		toolbarInitialized = true
		// Run validation now that toolbar is initialized
		validateMeta()
		toolbar.setUpdateButtonCallback { (_: Event) =>
			toolbar.setUpdateButtonEnabled(false)
			updater(_meta).failed.foreach{_ => toolbar.setUpdateButtonEnabled(true)}
		}

		toolbar.setSubmitButtonCallback { (_: Event) =>
			toolbar.setSubmitButtonEnabled(false)
			updater(_meta).map{ _ =>
				Backend.submitForPublication(_meta.doi)
			}.andThen{
				case Failure(exc) =>
					toolbar.setSubmitButtonEnabled(true)
					errorMessages.appendChild(p(exc.getMessage()).render)
				case Success(_) =>
					// Keep button disabled after successful submission
			}
		}

		toolbar.setStateChangeCallback { (newState: DoiPublicationState) =>
			val event = (init.state, newState) match {
				case (_, DoiPublicationState.findable) => Some(DoiPublicationEvent.publish)
				case (DoiPublicationState.draft, DoiPublicationState.registered) => Some(DoiPublicationEvent.register)
				case (DoiPublicationState.findable, DoiPublicationState.registered) => Some(DoiPublicationEvent.hide)
				case _ => None
			}

			val metaWithEvent = _meta.copy(state = newState, event = event)
			updater(metaWithEvent).foreach { _ =>
				_meta = metaWithEvent.copy(event = None)
				toolbar.updateBadge(newState)
			}
		}
	}

	private[this] val formElems = div.render

	val element = div(
		formElems,
		errorMessages
	).render

	// Initialize forms (deferred to avoid calling toolbar during construction)
	private def initialize(): Unit = {
		resetForms()
	}

	// Call initialization when the widget is first accessed
	initialize()
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
		MultiEntitiesEditWidget[Date, CompositeDateWidget](init, cb)("Dates"){

		protected def makeWidget(value: Date, updateCb: Date => Unit) = new CompositeDateWidget(value, updateCb)

		protected def defaultValue = Date("", None)
	}


	class FormatsEditWidget(init: Seq[String], cb: Seq[String] => Unit) extends
		MultiStringsWidget(init, cb, "Format", required = true)("Formats")


	class RightsEditWidget(init: Seq[Rights], cb: Seq[Rights] => Unit) extends
		MultiEntitiesEditWidget[Rights, RightsWidget](init, cb)("Rights"){

		protected def makeWidget(value: Rights, updateCb: Rights => Unit) = new RightsWidget(value, updateCb)

		protected def defaultValue = Rights("", None, None, Some("https://spdx.org/licenses"), Some("SPDX"), Some("eng"))
	}


	class DescriptionsEditWidget(init: Seq[Description], cb: Seq[Description] => Unit) extends
		MultiEntitiesEditWidget[Description, DescriptionWidget](init, cb)("Descriptions"){

		protected def makeWidget(value: Description, updateCb: Description => Unit) = new DescriptionWidget(value, updateCb)

		protected def defaultValue = Description("", null, None)
	}

	class RelatedIdentifierEditWidget(init: Seq[RelatedIdentifier], cb: Seq[RelatedIdentifier] => Unit) extends
		MultiEntitiesEditWidget[RelatedIdentifier, RelatedIdentifierWidget](init, cb)("Related identifiers") {

		protected def makeWidget(value: RelatedIdentifier, updateCb: RelatedIdentifier => Unit) = new RelatedIdentifierWidget(value, updateCb)

		protected  def defaultValue = RelatedIdentifier(None, None, "", None, None, None, None)
	}

	class FundingEditWidget(init: Seq[FundingReference], cb: Seq[FundingReference] => Unit) extends
		MultiEntitiesEditWidget[FundingReference, FundingWidget](init, cb)("Funding references"){

			protected def makeWidget(value: FundingReference, updateCb: FundingReference => Unit) = new FundingWidget(value, updateCb)

			protected def defaultValue = FundingReference(None, None, None )
		}

	class GeoLocationEditWidget(init: Seq[GeoLocation], cb: Seq[GeoLocation] => Unit) extends
		MultiEntitiesEditWidget[GeoLocation, GeoLocationWidget](init, cb)("Geolocations"){

			protected def makeWidget(value: GeoLocation, updateCb: GeoLocation => Unit) = new GeoLocationWidget(value, updateCb)

			protected def defaultValue = GeoLocation(None, None, None)
		}
}
