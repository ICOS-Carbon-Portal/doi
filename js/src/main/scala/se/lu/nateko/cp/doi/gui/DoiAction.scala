package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.Doi

sealed trait DoiAction

case object DoiListRefreshRequest extends DoiAction

case class FreshDoiList(dois: Seq[Doi]) extends DoiAction

case class SelectDoi(doi: Doi) extends DoiAction

case class GotDoiInfo(info: DoiInfo) extends DoiAction

case class ReportError(msg: String) extends DoiAction
