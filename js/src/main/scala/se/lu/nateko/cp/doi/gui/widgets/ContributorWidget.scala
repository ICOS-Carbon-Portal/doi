package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.widgets.generic._
import se.lu.nateko.cp.doi.meta.Contributor
import se.lu.nateko.cp.doi.meta.ContributorType
import se.lu.nateko.cp.doi.meta.ContributorType.ContributorType

import CreatorWidget._

class ContributorWidget(init: Contributor, protected val updateCb: Contributor => Unit) extends EntityWidget[Contributor]{

	private[this] var _contributor = init

	private[this] val contributorTypeSelect = new SelectWidget[ContributorType](
		SelectWidget.selectOptions(ContributorType, Some("Contributor type")),
		Option(init.contributorType),
		typeOpt => {
			val contrType = typeOpt.getOrElse(null)
			_contributor = _contributor.copy(contributorType = contrType)
			updateCb(_contributor)
		}
	)

	private[this] val nameInput = new NameWidget(init.name, name => {
		_contributor = _contributor.copy(name = name)
		updateCb(_contributor)
	})

	private[this] val nameIdsInput = new NameIdsInput(init.nameIds, ids => {
		_contributor = _contributor.copy(nameIds = ids)
		updateCb(_contributor)
	})

	private[this] val affiliationsInput = new AffiliationsInput(init.affiliations, affs => {
		_contributor = _contributor.copy(affiliations = affs)
		updateCb(_contributor)
	})

	val element = div(
		Bootstrap.basicPropValueWidget("Contributor type")(contributorTypeSelect.element),
		nameInput.element,
		nameIdsInput.element,
		affiliationsInput.element
	).render
}
