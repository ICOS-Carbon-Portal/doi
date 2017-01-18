package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.console
import org.scalajs.dom.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.gui.DoiListRefreshRequest
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.DoiMeta

class MainView(dispatch: DoiAction => Unit) {

	val doiViews = scala.collection.mutable.Map.empty[Doi, DoiView]

	private val listElem = ul(cls := "list-unstyled").render
	private val refreshDoiList = (_: Event) => dispatch(DoiListRefreshRequest)

	val element = div(id := "main")(
		div(cls := "page-header")(
			h1("Carbon Portal DOI minting service")
		),
		Bootstrap.basicPanel(
			button(cls := "btn btn-default", onclick := refreshDoiList)("Refresh DOI list")
		),
		listElem
	)

	def supplyDoiList(dois: Seq[Doi]): Unit = {
		listElem.innerHTML = ""
		doiViews.clear()

		for(doi <- dois) {
			val doiView = new DoiView(doi, dispatch)
			doiViews += ((doi, doiView))
			listElem.appendChild(doiView.element)
		}
	}

	def setSelected(doi: Doi, isSelected: Boolean): Unit = {
		doiViews.get(doi).foreach(_.setSelected(isSelected))
	}

	def supplyInfo(info: DoiInfo): Unit = {
		doiViews.get(info.meta.id).foreach(_.supplyInfo(info))
	}

	def onUrlUpdateBegin(doi: Doi): Unit = {
		//TODO Implement
	}

	def onUrlUpdated(doi: Doi, url: String): Unit = {
		//TODO Implement
	}

	def onMetadataUpdateBegin(doi: Doi): Unit = {
		//TODO Implement
	}

	def onMetadataUpdated(meta: DoiMeta): Unit = {
		//TODO Implement
	}

	def fail(msg: String): Unit = {
		console.log(msg)
	}
}
