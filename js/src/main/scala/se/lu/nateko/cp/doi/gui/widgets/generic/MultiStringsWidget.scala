package se.lu.nateko.cp.doi.gui.widgets.generic

import scala.collection.Seq

abstract class MultiStringsWidget(init: Seq[String], cb: Seq[String] => Unit, placeHolder: String = "")(
	title: String, minAmount: Int = 0
) extends MultiEntitiesEditWidget[String, TextInputWidget](init, cb)(title, minAmount){

	protected def makeWidget(value: String, updateCb: String => Unit) = new TextInputWidget(value, updateCb, placeHolder)

	protected def defaultValue = ""
}