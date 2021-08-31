package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi

import scala.collection.Seq

case class DoiState(
	prefix: String,
	dois: Seq[DoiMeta],
	selected: Option[Doi],
	error: Option[String]
)

object DoiStateUpgrades{

	implicit class SmartDoiState(val state: DoiState) extends AnyVal{

		def withSelected(doi: Doi): DoiState = state.copy(selected = Some(doi))
		def isSelected(doi: Doi): Boolean = state.selected.contains(doi)
	}

}
