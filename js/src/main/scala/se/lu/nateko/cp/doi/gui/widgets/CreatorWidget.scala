package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.NameIdentifier
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic._

import scala.collection.Seq

import CreatorWidget._

class CreatorWidget(init: Creator, protected val updateCb: Creator => Unit) extends EntityWidget[Creator]{

	private[this] var _creator = init

	private[this] val nameInput = new NameWidget(init.name, name => {
		_creator = _creator.copy(name = name)
		updateCb(_creator)
	})

	private[this] val nameIdsInput = new NameIdsInput(init.nameIdentifiers, ids => {
		_creator = _creator.copy(nameIdentifiers = ids)
		updateCb(_creator)
	})

	private[this] val affiliationsInput = new AffiliationsInput(init.affiliation, affs => {
		_creator = _creator.copy(affiliation = affs)
		updateCb(_creator)
	})

	val element = div(
		nameInput.element,
		nameIdsInput.element,
		affiliationsInput.element
	).render
}

object CreatorWidget{

	class NameIdsInput(init: Seq[NameIdentifier], updateCb: Seq[NameIdentifier] => Unit) extends
		MultiEntitiesEditWidget[NameIdentifier, NameIdentifierWidget](init, updateCb)("IDs"){

		protected def makeWidget(value: NameIdentifier, updateCb: NameIdentifier => Unit) =
			new NameIdentifierWidget(value, updateCb)

		protected def defaultValue = NameIdentifier.orcid("")
	}


	class AffiliationsInput(init: Seq[String], updateCb: Seq[String] => Unit) extends
		MultiStringsWidget(init, updateCb, "Affiliation")("Affiliations")

}
