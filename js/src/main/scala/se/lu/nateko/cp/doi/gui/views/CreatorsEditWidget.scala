package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.GenericName

class CreatorsEditWidget(init: Seq[Creator], cb: Seq[Creator] => Unit) extends {
	protected val title = "Creators"
	protected val minAmount = 1
} with MultiEntitiesEditWidget[Creator, CreatorWidget](init, cb){

	protected def makeWidget(value: Creator, updateCb: Creator => Unit) = new CreatorWidget(value, updateCb)

	protected def defaultValue = Creator(GenericName(""), Nil, Nil)
}
