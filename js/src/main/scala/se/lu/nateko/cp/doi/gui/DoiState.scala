package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi

import scala.collection.Seq

case class DoiInfo(meta: DoiMeta, target: Option[String], hasBeenSaved: Boolean)

case class DoiState(
	prefix: String,
	dois: Seq[DoiMeta],
	info: Map[Doi, DoiInfo],
	selected: Option[Doi],
	error: Option[String]
)

object DoiStateUpgrades{

	implicit class SmartDoiState(val state: DoiState) extends AnyVal{

		def withSelected(doi: Doi): DoiState = state.copy(selected = Some(doi))

		def withDoiInfo(doiInfo: DoiInfo) = state.copy(info = state.info + ((doiInfo.meta.doi, doiInfo)))

		def updateUrl(doi: Doi, url: String) = state.info.get(doi) match{

			case Some(doiInfo) => state.withDoiInfo(doiInfo.withUrl(url))

			case _ => state
		}

		def updateMeta(meta: DoiMeta) = state.info.get(meta.doi) match{

			case Some(doiInfo) => state.withDoiInfo(doiInfo.withSavedMeta(meta))

			case _ => state.withDoiInfo(DoiInfo(meta, None, false))
		}

		def isSelected(doi: Doi): Boolean = state.selected.contains(doi)
		def selectedInfo: Option[DoiInfo] = state.selected.flatMap(state.info.get)
	}

	implicit class SmartDoiInfo(val info: DoiInfo) extends AnyVal{

		def withUrl(url: String) = info.copy(target = Some(url))
		def withSavedMeta(meta: DoiMeta) = info.copy(meta = meta, hasBeenSaved = true)
	}


}
