package se.lu.nateko.cp.doi.gui

import DoiRedux.Reducer
import se.lu.nateko.cp.doi.Doi

import DoiStateUpgrades._

object DoiReducer {

	val reducer: Reducer = (action, state) => action match{

		case FreshDoiList(dois) =>
			state.copy(dois = dois)

		case SelectDoi(doi) =>
			if(state.isSelected(doi))
				state.copy(selected = None)
			else
				state.withSelected(doi)

		case GotDoiInfo(info) => state.withDoiInfo(info)

		case TargetUrlUpdateRequest(doi, _) => state.startUrlUpdate(doi)

		case TargetUrlUpdated(doi, url) => state.updateUrl(doi, url).stopUrlUpdate(doi)

		case MetaUpdateRequest(meta) => state.startMetaUpdate(meta.id)

		case MetaUpdated(meta) => state.updateMeta(meta).stopMetaUpdate(meta.id)

		case ReportError(msg) => state.copy(error = Some(msg))

	}

}
