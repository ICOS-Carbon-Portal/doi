package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi

import scala.collection.Seq

case class DoiInfo(meta: DoiMeta, target: Option[String], hasBeenSaved: Boolean)

case class IoState(updatingUrl: Option[Doi], updatingMeta: Option[Doi])

case class DoiState(
	prefix: String,
	dois: Seq[DoiMeta],
	info: Map[Doi, DoiInfo],
	selected: Option[Doi],
	ioState: IoState,
	error: Option[String]
)

object DoiStateUpgrades{

	implicit class SmartDoiState(val state: DoiState) extends AnyVal{

		def withSelected(doi: Doi): DoiState = state.copy(selected = Some(doi))

		def withUrlUpdate(doi: Doi) = state.copy(ioState = state.ioState.copy(updatingUrl = Some(doi)))
		def withoutUrlUpdate = state.copy(ioState = state.ioState.copy(updatingUrl = None))

		def withMetaUpdate(doi: Doi) = state.copy(ioState = state.ioState.copy(updatingMeta = Some(doi)))
		def withoutMetaUpdate(doi: Doi) = state.copy(ioState = state.ioState.copy(updatingMeta = None))

		def withDoiInfo(doiInfo: DoiInfo) = state.copy(info = state.info + ((doiInfo.meta.doi, doiInfo)))

		def updateUrl(doi: Doi, url: String) = state.info.get(doi) match{

			case Some(doiInfo) => state.withDoiInfo(doiInfo.withUrl(url))

			case _ => state
		}

		def updateMeta(meta: DoiMeta) = state.info.get(meta.doi) match{

			case Some(doiInfo) => state.withDoiInfo(doiInfo.withSavedMeta(meta))

			case _ => state.withDoiInfo(DoiInfo(meta, None, false))
		}

		def startUrlUpdate(doi: Doi) = state.ioState match{
			case IoState(None, _) => state.withUrlUpdate(doi)
			case _ => state.copy(error = Some("Cannot update URL, previous update operation is pending"))
		}

		def stopUrlUpdate(doi: Doi) = state.ioState match{
			case IoState(Some(`doi`), None) => state.withoutUrlUpdate
			case _ => state
		}

		def startMetaUpdate(doi: Doi) = state.ioState match{
			case IoState(_, None) => state.withMetaUpdate(doi)
			case _ => state.copy(error = Some("Cannot update metadata, previous update operation is pending"))
		}

		def stopMetaUpdate(doi: Doi) = state.ioState match{
			case IoState(_, Some(`doi`)) => state.withoutMetaUpdate(doi)
			case _ => state
		}

		def isSelected(doi: Doi): Boolean = state.selected.contains(doi)
		def isUpdatingUrl(doi: Doi): Boolean = state.ioState.updatingUrl.contains(doi)
		def isUpdatingMeta(doi: Doi): Boolean = state.ioState.updatingMeta.contains(doi)

		def selectedInfo: Option[DoiInfo] = state.selected.flatMap(state.info.get)
	}

	implicit class SmartDoiInfo(val info: DoiInfo) extends AnyVal{

		def withUrl(url: String) = info.copy(target = Some(url))
		def withSavedMeta(meta: DoiMeta) = info.copy(meta = meta, hasBeenSaved = true)
	}


}
