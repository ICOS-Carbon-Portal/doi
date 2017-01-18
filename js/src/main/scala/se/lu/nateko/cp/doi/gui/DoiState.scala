package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi

case class DoiInfo(meta: DoiMeta, target: Option[String])
case class SelectedDoi(doi: Doi, info: Option[DoiInfo] = None)

case class IoState(updatingUrl: Option[Doi], updatingMeta: Option[Doi])

case class DoiState(dois: Seq[Doi], selected: Option[SelectedDoi], ioState: IoState, error: Option[String])

object DoiStateUpgrades{

	implicit class SmartDoiState(val state: DoiState) extends AnyVal{

		def withSelected(sd: SelectedDoi): DoiState = state.copy(selected = Some(sd))

		def withUrlUpdate(doi: Doi) = state.copy(ioState = state.ioState.copy(updatingUrl = Some(doi)))
		def withoutUrlUpdate = state.copy(ioState = state.ioState.copy(updatingUrl = None))

		def withMetaUpdate(doi: Doi) = state.copy(ioState = state.ioState.copy(updatingMeta = Some(doi)))
		def withoutMetaUpdate(doi: Doi) = state.copy(ioState = state.ioState.copy(updatingMeta = None))


		def updateUrl(doi: Doi, url: String) = state.selected match{

			case Some(sd @ SelectedDoi(`doi`, Some(info))) => withSelected(sd.withInfo(info.withUrl(url)))
			case _ => state
		}

		def updateMeta(meta: DoiMeta) = state.selected match{

			case Some(sd @ SelectedDoi(doi, Some(info))) if(doi == meta.id) =>
				state.withSelected(sd.withInfo(info.withMeta(meta)))

			case _ => state
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

		def isSelected(doi: Doi): Boolean = state.selected.exists(_.doi == doi)
		def isUpdatingUrl(doi: Doi): Boolean = state.ioState.updatingUrl.contains(doi)
		def isUpdatingMeta(doi: Doi): Boolean = state.ioState.updatingMeta.contains(doi)
	}

	implicit class SmartDoiInfo(val info: DoiInfo) extends AnyVal{

		def withUrl(url: String) = info.copy(target = Some(url))
		def withMeta(meta: DoiMeta) = info.copy(meta = meta)
	}


	implicit class SmartSelectedDoi(val selected: SelectedDoi) extends AnyVal{

		def withInfo(info: DoiInfo) = selected.copy(info = Some(info))
	}

}
