package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.gui.redux.Redux

object DoiRedux extends Redux{
	type State = DoiState
	type Action = DoiAction

}


