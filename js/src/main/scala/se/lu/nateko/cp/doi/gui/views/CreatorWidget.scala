package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.NameIdentifier
import scalatags.JsDom.all._

class CreatorWidget(init: Creator, protected val updateCb: Creator => Unit) extends EntityWidget[Creator]{

	private[this] var _creator = init

	private[this] val nameInput = new NameWidget(init.name, name => {
		_creator = _creator.copy(name = name)
		updateCb(_creator)
	})


	private[this] val nameIdsInput = new {

		protected val title = "IDs"

		protected val minAmount = 0

	} with MultiEntitiesEditWidget[NameIdentifier, NameIdentifierWidget](init.nameIds, ids => {

		_creator = _creator.copy(nameIds = ids)
		updateCb(_creator)
	}){

		protected def makeWidget(value: NameIdentifier, updateCb: NameIdentifier => Unit) =
			new NameIdentifierWidget(value, updateCb)

		protected def defaultValue = NameIdentifier.orcid("")
	}


	private[this] val affiliationsInput = new {

		protected val title = "Affiliations"

		protected val minAmount = 0

	} with MultiStringsWidget(init.affiliations, affs => {

		_creator = _creator.copy(affiliations = affs)

		updateCb(_creator)
	})


	val element = div(
		nameInput.element,
		nameIdsInput.element,
		affiliationsInput.element
	).render
}
