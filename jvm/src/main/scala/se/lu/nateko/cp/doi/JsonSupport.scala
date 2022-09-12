package se.lu.nateko.cp.doi

import play.api.libs.json._
import se.lu.nateko.cp.doi.meta._


object JsonSupport{
	
	given dateTypeFormat: Format[DateType.Value] = Json.formatEnum(DateType)
	given contrTypeFormat: Format[ContributorType.Value] = Json.formatEnum(ContributorType)
	given descriptionTypeFormat: Format[DescriptionType.Value] = Json.formatEnum(DescriptionType)
	given resourceTypeGeneralFormat: Format[ResourceTypeGeneral.Value] = Json.formatEnum(ResourceTypeGeneral)
	given titleTypeFormat: Format[TitleType.Value] = Json.formatEnum(TitleType)
	given doiStateEnumFormat: Format[DoiPublicationState.Value] = Json.formatEnum(DoiPublicationState)
	given doiEventEnumFormat: Format[DoiPublicationEvent.Value] = Json.formatEnum(DoiPublicationEvent)

	given Format[Doi] = Json.format[Doi]

	given OFormat[SubjectScheme] = Json.format[SubjectScheme]
	given OFormat[Subject] = Json.format[Subject]
	given OFormat[NameIdentifierScheme] = Json.format[NameIdentifierScheme]
	given OFormat[NameIdentifier] = Json.format[NameIdentifier]
	given genericNameFormat: OFormat[GenericName] = Json.format[GenericName]
	given personalNameFormat: OFormat[PersonalName] = Json.format[PersonalName]

	given OFormat[Name] with{
		def writes(name: Name) = name match
			case gn: GenericName =>
				genericNameFormat.writes(gn) ++ Json.obj("nameType" -> "Organizational")
			case pn: PersonalName =>
				personalNameFormat.writes(pn) ++ Json.obj("nameType" -> "Personal")

		def reads(js: JsValue) = (js \ "familyName") match
			case JsDefined(JsString(_)) =>
				personalNameFormat.reads(js)

			case _ =>
				genericNameFormat.reads(js)

	}

	given OFormat[Affiliation] = Json.format[Affiliation]

	given OFormat[Creator] = Json.format[Creator]
	given OFormat[Contributor] = Json.format[Contributor]
	given OFormat[Title] = Json.format[Title]
	given OFormat[ResourceType] = Json.format[ResourceType]
	given OFormat[Date] = Json.format[Date]

	given Format[Version] = Json.format[Version]

	given OFormat[Rights] = Json.format[Rights]
	given OFormat[Description] = Json.format[Description]

	given OFormat[DoiMeta] = Json.format[DoiMeta]
	given OFormat[DoiWrapper] = Json.format[DoiWrapper]
	given OFormat[SingleDoiPayload] = Json.format[SingleDoiPayload]
	given OFormat[DoiListMeta] = Json.format[DoiListMeta]
	given OFormat[DoiListPayload] = Json.format[DoiListPayload]

	given OFormat[Award] = Json.format[Award]
	given OFormat[FunderIdentifierScheme] = Json.format[FunderIdentifierScheme]
	given OFormat[FunderIdentifier] =  Json.format[FunderIdentifier]
	given OFormat[FundingReference] = Json.format[FundingReference]
}
