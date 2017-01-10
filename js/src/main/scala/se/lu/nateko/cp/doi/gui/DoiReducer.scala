package se.lu.nateko.cp.doi.gui

import DoiRedux.Reducer

object DoiReducer {

	val reducer: Reducer = (action, state) => action match{

		case FreshDoiList(dois) =>
			state.copy(dois = dois)

		case SelectDoi(doi) => state.selected match{

			case Some(SelectedDoi(`doi`, _)) =>
				state.copy(selected = None)

			case _ =>
				state.copy(selected = Some(SelectedDoi(doi, None)))
		}

		case GotDoiInfo(info) => state.selected match{

			case Some(SelectedDoi(doi, _)) if(doi == info.meta.id)=>
				state.copy(selected = Some(SelectedDoi(doi, Some(info))))

			case _ =>
				state
		}

		case DoiListRefreshRequest | ReportError(_) => state
	}
}