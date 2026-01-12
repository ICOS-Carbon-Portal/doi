package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiAction
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.NavigateToRoute
import se.lu.nateko.cp.doi.gui.DetailRoute
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

	private val doiSpan = span(cls := "text-muted small").render
	private val titleSpan = span(cls := "fw-semibold").render
	private val badgeSpan = span().render
	private val navigateToDetail: Event => Unit = e => {
		e.preventDefault()
		d.dispatch(NavigateToRoute(DetailRoute(meta.doi)))
	}

	def badgeClasses = "badge " + (meta.state match {
		case DoiPublicationState.draft => "bg-warning text-dark"
		case DoiPublicationState.registered => "bg-primary"
		case DoiPublicationState.findable => "bg-success"
	})

	val element = a(
		href := s"/doi/${meta.doi}",
		cls := "list-group-item list-group-item-action d-block text-decoration-none",
		style := "cursor: pointer",
		onclick := navigateToDetail
	)(
		div(cls := "d-flex align-items-center justify-content-between")(
			div(cls := "d-flex flex-column flex-grow-1")(
				doiSpan,
				titleSpan
			),
			badgeSpan
		)
	).render

	def updateContentVisibility(): Unit = {
		val title = DoiMetaHelpers.extractTitle(meta)

		doiSpan.textContent = meta.doi.toString
		titleSpan.textContent = title
		badgeSpan.className = badgeClasses
		badgeSpan.textContent = meta.state.toString.capitalize
	}

	def setSelected(selected: Boolean): Unit = {
		// This method is kept for compatibility but no longer expands cards
		// Cards now navigate to detail view instead
	}
}
