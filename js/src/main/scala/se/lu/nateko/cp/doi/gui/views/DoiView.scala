package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiAction
import org.scalajs.dom.Event
import org.scalajs.dom.console
import se.lu.nateko.cp.doi.gui.SelectDoi
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.DoiWithTitle
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.DoiMetaViewer
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.DoiCloneRequest
import se.lu.nateko.cp.doi.gui.Backend
import scala.util.Failure
import se.lu.nateko.cp.doi.gui.ReportError
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.Success
import se.lu.nateko.cp.doi.gui.DoiJsonEditor
import se.lu.nateko.cp.doi.gui.widgets.EditorTab

class DoiView(metaInit: DoiMeta, d: DoiRedux.Dispatcher) {

	private[this] var meta = metaInit
	private[this] var isSelected = false
	private[this] var hasInitializedBody = false

	private val doiListIcon = span(cls := doiListIconClass, style := "width: 1em").render

	private def doiListIconClass = "fas fa-caret-" +
		(if(isSelected) "down" else "right")

	private val selectDoi: Event => Unit = e => d.dispatch(SelectDoi(meta.doi))
	private val titleSpan = span().render
	private val cardBody = div(cls := "card-body").render

	def cardHeaderClasses = "card-header bg-opacity-50 bg-" + (if(meta.state == DoiPublicationState.draft) "warning" else "primary")
	def cardClasses = "card " + (if(meta.state == DoiPublicationState.draft) "draft-doi" else "published-doi")

	val element = div(cls := cardClasses)(
		div(cls := cardHeaderClasses, onclick := selectDoi, cursor := "pointer")(
			doiListIcon,
			titleSpan
		),
		cardBody
	).render

	def updateContentVisibility(): Unit = {
		val title = meta.titles
			.flatMap(_.headOption)
			.map(" | " + _.title)
			.getOrElse("")

		titleSpan.textContent = s" ${meta.doi} $title"

		val display = if(isSelected) "block" else "none"
		cardBody.style.display = display
		element.className = cardClasses
	}

	def setSelected(selected: Boolean): Unit = {
		isSelected = selected
		if(selected && !hasInitializedBody){
			cardBody.appendChild(metaViewer.element)
			hasInitializedBody = true
		}
		doiListIcon.className = doiListIconClass
		updateContentVisibility()
	}

	private def metaViewer: DoiMetaViewer = new DoiMetaViewer(meta, tabsCb, meta => d.dispatch(DoiCloneRequest(meta)))

	private def metaWidget = new DoiMetaWidget(
		meta,
		updateDoiMeta,
		tabsCb,
		doi => {
			d.dispatch(ThunkActions.requestDoiDeletion(doi))
		}
	)

	private def metaJsonEditor = new DoiJsonEditor(meta, updateDoiMeta, tabsCb)

	private val tabsCb: Map[EditorTab.Value, () => Unit] = Map(
		EditorTab.view -> {() => cardBody.replaceChildren(metaViewer.element)},
		EditorTab.edit -> {() => cardBody.replaceChildren(metaWidget.element)},
		EditorTab.json -> {() => cardBody.replaceChildren(metaJsonEditor.element)},
	)

	private def updateDoiMeta(updated: DoiMeta): Future[Unit] = Backend.updateMeta(updated).andThen{
		case Failure(exc) =>
			d.dispatch(ReportError(s"Failed to update DOI ${updated.doi}:\n${exc.getMessage}"))
		case Success(_) =>
			//recreate the DOI metadata widget with the updated metadata
			cardBody.innerHTML = ""
			meta = updated
			cardBody.appendChild(metaWidget.element)
			updateContentVisibility()
	}

}
