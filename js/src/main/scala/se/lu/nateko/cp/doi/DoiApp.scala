package se.lu.nateko.cp.doi

import scala.scalajs.js.JSApp
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom
import org.scalajs.dom.Event
import dom.document
import org.scalajs.dom.ext.Ajax
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta.Title
import se.lu.nateko.cp.doi.meta.TitleType
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import FrontendViews._

object DoiApp extends JSApp {

	case class DoiInfo(meta: DoiMeta, target: Option[String])
	case class SelectedDoi(doi: Doi, info: Option[DoiInfo] = None)

	private var selected: Option[SelectedDoi] = None

	def main(): Unit = {
		val mainDiv = document.getElementById("main")
		mainDiv.parentNode.replaceChild(mainLayout(refreshDoiList).render, mainDiv)

		refreshDoiList(null)
	}

	def isSelected(doi: Doi): Boolean = selected.exists(_.doi == doi)

	def getInfo(doi: Doi): Unit = Backend.getMeta(doi)
		.zip(Backend.getTarget(doi))
		.map{
			case (meta, target) => if(isSelected(doi)){
				val info = DoiInfo(meta, target)
				selected = Some(SelectedDoi(doi, Some(info)))
				rerenderDoiElem(doi)
			}
		}.failed.foreach{err =>
			dom.console.log(err.getMessage)
		}

	val refreshDoiList: Event => Unit = e => Backend.getDoiList
		.map(repopulateDoiList)
		.failed.foreach{err =>
			dom.console.log(err.getMessage)
		}

	def repopulateDoiList(doiList: Seq[Doi]): Unit = {
		val listElem = getListElem
		listElem.innerHTML = ""

		for(doi <- doiList) {
			listElem.appendChild(doiElem(doi).render)
		}
	}

	def doiElem(doi: Doi) = {
		val heading = div(cls := "panel-heading", onclick := selectDoi(doi))(
			doiListIcon(isSelected(doi)),
			span(" " + doi.toString)
		)

		val body = selected.filter(_.doi == doi).flatMap(_.info).map(doiInfoPanelBody).toList
		
		div(
			cls := "panel panel-default",
			id := doi.toString
		)(
			heading +: body
		)
	}

	def selectDoi(doi: Doi): Event => Unit = e => {
		val oldSelected = selected
		selected = if(isSelected(doi)) None else {
			getInfo(doi)
			Some(SelectedDoi(doi))
		}
		oldSelected.map(_.doi).filter(_ != doi).foreach(rerenderDoiElem)
		rerenderDoiElem(doi)
	}

	def rerenderDoiElem(doi: Doi): Unit = {
		val oldElem = document.getElementById(doi.toString)
		val newElem = doiElem(doi).render
		getListElem.replaceChild(newElem, oldElem)
	}

	def getListElem = document.getElementById(doiListId)
}

