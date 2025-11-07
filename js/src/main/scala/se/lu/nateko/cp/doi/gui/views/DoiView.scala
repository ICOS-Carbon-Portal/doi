package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiAction
import org.scalajs.dom.Event
import org.scalajs.dom.console
import se.lu.nateko.cp.doi.gui.SelectDoi
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions
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

	private val selectDoi: Event => Unit = e => d.dispatch(SelectDoi(meta.doi))
	private val titleSpan = span().render

	def cardHeaderClasses = "card-header bg-opacity-50 bg-" + (if(meta.state == DoiPublicationState.draft) "warning" else "primary")
	def cardClasses = "card " + (if(meta.state == DoiPublicationState.draft) "draft-doi" else "published-doi")

	val element = div(cls := cardClasses)(
		div(cls := cardHeaderClasses, onclick := selectDoi, cursor := "pointer")(
			span(cls := "fas fa-arrow-right", style := "width: 1em"),
			titleSpan
		)
	).render

	def updateContentVisibility(): Unit = {
		val title = meta.titles
			.flatMap(_.headOption)
			.map(" | " + _.title)
			.getOrElse("")

		titleSpan.textContent = s" ${meta.doi} $title"
		element.className = cardClasses
	}

	def setSelected(selected: Boolean): Unit = {
		// This method is kept for compatibility but no longer expands cards
		// Cards now navigate to detail view instead
	}
}
