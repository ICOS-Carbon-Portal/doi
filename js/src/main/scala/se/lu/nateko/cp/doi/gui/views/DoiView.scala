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
	private val stateDot = span(cls := "flex-shrink-0").render


	private val navigateToDetail: Event => Unit = e => {
		e.preventDefault()
		d.dispatch(NavigateToRoute(DetailRoute(meta.doi)))
	}

	val element = a(
		href := s"/doi/${meta.doi}",
		cls := "list-group-item list-group-item-action d-block text-decoration-none",
		style := "cursor: pointer",
		onclick := navigateToDetail
	)(
		div(cls := "d-flex align-items-center justify-content-between gap-2")(
			div(cls := "d-flex flex-column flex-grow-1")(
				doiSpan,
				titleSpan
			),
			stateDot
		)
	).render

	def updateContentVisibility(): Unit = {
		val title = DoiMetaHelpers.extractTitle(meta)

		doiSpan.textContent = meta.doi.toString
		titleSpan.textContent = title
		stateDot.className = s"flex-shrink-0 ${DoiMetaHelpers.stateDotClass(meta.state)}"
	}

	def setSelected(selected: Boolean): Unit = {
		// This method is kept for compatibility but no longer expands cards
		// Cards now navigate to detail view instead
	}
}
