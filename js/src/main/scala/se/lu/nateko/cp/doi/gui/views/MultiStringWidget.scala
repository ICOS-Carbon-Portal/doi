package se.lu.nateko.cp.doi.gui.views

abstract class MultiStringsWidget(init: Seq[String], cb: Seq[String] => Unit) extends
	MultiEntitiesEditWidget[String, TextInputWidget](init, cb){

	protected def makeWidget(value: String, updateCb: String => Unit) = new TextInputWidget(value, updateCb)

	protected def defaultValue = ""
}