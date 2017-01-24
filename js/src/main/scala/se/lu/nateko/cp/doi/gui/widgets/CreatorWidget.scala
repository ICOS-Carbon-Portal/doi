package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.NameIdentifier
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic._

import CreatorWidget._

class CreatorWidget(init: Creator, protected val updateCb: Creator => Unit) extends EntityWidget[Creator]{

	private[this] var _creator = init

	private[this] val nameInput = new NameWidget(init.name, name => {
		_creator = _creator.copy(name = name)
		updateCb(_creator)
	})

	private[this] val nameIdsInput = new NameIdsInput(init.nameIds, ids => {
		_creator = _creator.copy(nameIds = ids)
		updateCb(_creator)
	})

	private[this] val affiliationsInput = new AffiliationsInput(init.affiliations, affs => {
		_creator = _creator.copy(affiliations = affs)
		updateCb(_creator)
	})

	val element = div(
		nameInput.element,
		nameIdsInput.element,
		affiliationsInput.element
	).render
}

object CreatorWidget{

	class NameIdsInput(init: Seq[NameIdentifier], updateCb: Seq[NameIdentifier] => Unit) extends {

		protected val title = "IDs"

		protected val minAmount = 0

	} with MultiEntitiesEditWidget[NameIdentifier, NameIdentifierWidget](init, updateCb){

		protected def makeWidget(value: NameIdentifier, updateCb: NameIdentifier => Unit) =
			new NameIdentifierWidget(value, updateCb)

		protected def defaultValue = NameIdentifier.orcid("")
	}


	class AffiliationsInput(init: Seq[String], updateCb: Seq[String] => Unit) extends {

		protected val title = "Affiliations"

		protected val minAmount = 0

	} with MultiStringsWidget(init, updateCb, "Affiliation")

}
