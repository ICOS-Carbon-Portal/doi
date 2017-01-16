package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Title

class TitlesEditWidget(initTitles: Seq[Title], cb: Seq[Title] => Unit) extends {
	protected val title = "Titles"
	protected val minAmount = 1
} with MultiEntitiesEditWidget[Title, TitleWidget](initTitles, cb){

	protected def makeWidget(value: Title, updateCb: Title => Unit) = new TitleWidget(value, updateCb)

	protected def defaultValue = Title("", None, None)
}
