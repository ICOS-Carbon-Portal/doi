package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.widgets.generic._
import se.lu.nateko.cp.doi.meta.Contributor
import se.lu.nateko.cp.doi.meta.ContributorType

import CreatorWidget._

class ContributorWidget(init: Contributor, protected val updateCb: Contributor => Unit) extends EntityWidget[Contributor]{

	private[this] var _contributor = init

	private[this] val contributorTypeSelect = new SelectWidget[ContributorType](
		SelectWidget.selectOptions(Some("Select contributor type"), ContributorType.values),
		init.contributorType,
		typeOpt => {
			_contributor = _contributor.copy(contributorType = typeOpt)
			updateCb(_contributor)
		}
	)

	private[this] val nameInput = new NameWidget(init.name, name => {
		_contributor = _contributor.copy(name = name)
		updateCb(_contributor)
	})

	private[this] val nameIdsInput = new NameIdsInput(init.nameIdentifiers, ids => {
		_contributor = _contributor.copy(nameIdentifiers = ids)
		updateCb(_contributor)
	})

	private[this] val affiliationsInput = new AffiliationsInput(init.affiliation, affs => {
		_contributor = _contributor.copy(affiliation = affs)
		updateCb(_contributor)
	})

	val element = div(
		div(cls := "row")(
			div(cls := "col input-group")(
				label(cls := "input-group-text")("Type"),
				(contributorTypeSelect.element),
			)
		),
		nameInput.element,
		nameIdsInput.element,
		affiliationsInput.element
	).render
}
