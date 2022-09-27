package se.lu.nateko.cp.doi

import play.api.libs.json._
import se.lu.nateko.cp.doi.meta._
import scala.reflect.ClassTag

object JsonSupport{

	def enumFormat[T <: reflect.Enum](valueOf: String => T)(using ctg: ClassTag[T]) = new Format[T] {
		def writes(v: T) = JsString(v.toString)

		def reads(value: JsValue): JsResult[T] = value match{
			case JsString(s) =>
				try{
					JsSuccess(valueOf(s))
				}catch{
					case _: IllegalArgumentException => JsError(
						s"No such $ctg enum value: $s"
					)
				}
			case _ => JsError("Expected a string")
		}
	}
	
	given dateTypeFormat: Format[DateType] = enumFormat(DateType.valueOf)
	given contrTypeFormat: Format[ContributorType] = enumFormat(ContributorType.valueOf)
	given descriptionTypeFormat: Format[DescriptionType] = enumFormat(DescriptionType.valueOf)
	given resourceTypeGeneralFormat: Format[ResourceTypeGeneral] = enumFormat(ResourceTypeGeneral.valueOf)
	given titleTypeFormat: Format[TitleType] = enumFormat(TitleType.valueOf)
	given doiStateEnumFormat: Format[DoiPublicationState] = enumFormat(DoiPublicationState.valueOf)
	given doiEventEnumFormat: Format[DoiPublicationEvent] = enumFormat(DoiPublicationEvent.valueOf)

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

	private val doubleFormat = summon[Format[Double]]
	given Reads[Latitude] = doubleFormat.map(Latitude.apply)
	given Reads[Longitude] = doubleFormat.map(Longitude.apply)
	given [T <: Latitude | Longitude]: Writes[T] with{
		def writes(d: T) = JsNumber(d)
	}

	given OFormat[GeoLocationPoint] = Json.format[GeoLocationPoint]
	given OFormat[GeoLocationBox] = Json.format[GeoLocationBox]
	given OFormat[GeoLocation] = Json.format[GeoLocation]
}
