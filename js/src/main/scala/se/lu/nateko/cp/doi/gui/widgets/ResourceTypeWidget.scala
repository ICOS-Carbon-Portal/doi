package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._

import se.lu.nateko.cp.doi.meta.ResourceType
import se.lu.nateko.cp.doi.meta.ResourceTypeGeneral
import se.lu.nateko.cp.doi.gui.widgets.generic._

class ResourceTypeWidget(
	init: ResourceType,
	protected val updateCb: ResourceType => Unit
) extends EntityWidget[ResourceType]{

	private[this] var _resType = init

	private[this] val rtInput = new TextInputWidget(init.resourceType.getOrElse(""), rt => {
		_resType = _resType.copy(resourceType = Some(rt))
		updateCb(_resType)
	}, "Specific resource type", required = true)

	private[this] val rtGenInput = new SelectWidget[ResourceTypeGeneral](
		SelectWidget.selectOptions(Some("General resource type"), ResourceTypeGeneral.values),
		init.resourceTypeGeneral,
		rtGenOpt => {
			val rtGen = rtGenOpt.getOrElse(null)
			_resType = _resType.copy(resourceTypeGeneral = Some(rtGen))
			updateCb(_resType)
		}
	)

	val element = div(cls := "row")(
		div(cls := "col-xl-10 col-lg-9")(
			div(cls := "input-group")(
				rtGenInput.element,
				span(cls := "input-group-text")("/"),
				rtInput.element
			)
		)
	).render

}
