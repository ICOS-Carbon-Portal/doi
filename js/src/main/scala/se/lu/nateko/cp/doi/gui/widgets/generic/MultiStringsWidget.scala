package se.lu.nateko.cp.doi.gui.widgets.generic

import scala.collection.Seq

abstract class MultiStringsWidget(init: Seq[String], cb: Seq[String] => Unit, placeHolder: String = "", required: Boolean)(
	title: String, minAmount: Int = 0, showTitle: Boolean = true
) extends MultiEntitiesEditWidget[String, TextInputWidget](init, cb)(title, minAmount, 0, showTitle){

	protected def makeWidget(value: String, updateCb: String => Unit) = new TextInputWidget(value, updateCb, placeHolder, required)

	protected def defaultValue = ""
}