package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.DoiAction
import org.scalajs.dom.Event
import org.scalajs.dom.console
import se.lu.nateko.cp.doi.gui.SelectDoi
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.DoiWithTitle
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.DoiCloneRequest
import se.lu.nateko.cp.doi.gui.Backend
import scala.util.Failure
import se.lu.nateko.cp.doi.gui.ReportError
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.Success

class DoiView(metaInit: DoiMeta, d: DoiRedux.Dispatcher) {

	private[this] var meta = metaInit
	private[this] var isSelected = false
	private[this] var hasInitializedBody = false

	private val doiListIcon = span(cls := doiListIconClass).render

	private def doiListIconClass = "glyphicon glyphicon-triangle-" +
		(if(isSelected) "bottom" else "right")

	private val selectDoi: Event => Unit = e => d.dispatch(SelectDoi(meta.doi))
	private val titleSpan = span(cls := "panel-title")().render
	private val panelBody = div(cls := "panel-body").render

	def panelClasses = "panel panel-" + (if(meta.state == DoiPublicationState.draft) "warning draft-doi" else "info published-doi")

	val element = div(cls := panelClasses)(
		div(cls := "panel-heading", onclick := selectDoi, cursor := "pointer")(
			doiListIcon,
			titleSpan
		),
		panelBody
	).render

	def updateContentVisibility(): Unit = {
		val title = meta.titles
			.flatMap(_.headOption)
			.map(" | " + _.title)
			.getOrElse("")

		titleSpan.textContent = s" ${meta.doi} $title"

		val display = if(isSelected) "block" else "none"
		panelBody.style.display = display
		element.className = panelClasses
	}

	def setSelected(selected: Boolean): Unit = {
		isSelected = selected
		if(selected && !hasInitializedBody){
			panelBody.appendChild(metaWidget.element)
			hasInitializedBody = true
		}
		doiListIcon.className = doiListIconClass
		updateContentVisibility()
	}

	private def metaWidget = new DoiMetaWidget(
		meta,
		updateDoiMeta,
		meta => d.dispatch(DoiCloneRequest(meta)),
		doi => {
			d.dispatch(ThunkActions.requestDoiDeletion(doi))
		}
	)

	private def updateDoiMeta(updated: DoiMeta): Future[Unit] = Backend.updateMeta(updated).andThen{
		case Failure(exc) =>
			d.dispatch(ReportError(s"Failed to update DOI ${updated.doi}:\n${exc.getMessage}"))
		case Success(_) =>
			//recreate the DOI metadata widget with the updated metadata
			panelBody.innerHTML = ""
			meta = updated
			panelBody.appendChild(metaWidget.element)
			updateContentVisibility()
	}

}
