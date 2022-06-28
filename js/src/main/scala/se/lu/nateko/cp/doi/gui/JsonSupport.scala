package se.lu.nateko.cp.doi

import play.api.libs.json._
import se.lu.nateko.cp.doi.meta._
import scala.reflect.Enum

/**
TODO When/if spray-json gets published for ScalaJS, retire this code by moving
spray-json based JsonSupport from core project to common project
*/
object JsonSupport{

	private def enumFormat[T <: Enum](valueOf: String => T) = new Format[T]{
		def writes(v: T) = JsString(v.toString)
		def reads(js: JsValue): JsResult[T] = js match{
			case JsString(s) => try{
					JsSuccess(valueOf(s))
				}catch{
					case _: IllegalArgumentException =>
						JsError(s"No such enum value: $s")
				}
			case _ => JsError("Expected a string")
		}
	}

	private def fieldConflatingFormat[T](vanillaF: OFormat[T], field: String, opt: Boolean = false) = new OFormat[T]{
		def writes(obj: T): JsObject = {
			val vanilla = vanillaF.writes(obj)
			val innerFields = vanilla.value.get(field).map(_.as[JsObject]).getOrElse(JsObject.empty)
			vanilla - field ++ innerFields
		}
		def reads(js: JsValue) = {
			val patchedJson = js.as[JsObject] + (field -> js)
			vanillaF.reads(patchedJson) match{
				case err: JsError if(opt) => vanillaF.reads(js)
				case other => other
			}
		}
	}


	given dateTypeFormat: Format[DateType] = enumFormat(DateType.valueOf)
	given contrTypeFormat: Format[ContributorType] = enumFormat(ContributorType.valueOf)
	given descriptionTypeFormat: Format[DescriptionType] = enumFormat(DescriptionType.valueOf)
	given resourceTypeGeneralFormat: Format[ResourceTypeGeneral] = enumFormat(ResourceTypeGeneral.valueOf)
	given titleTypeFormat: Format[TitleType] = enumFormat(TitleType.valueOf)
	given doiStateEnumFormat: Format[DoiPublicationState] = enumFormat(DoiPublicationState.valueOf)
	given doiEventEnumFormat: Format[DoiPublicationEvent] = enumFormat(DoiPublicationEvent.valueOf)

	private def parseDoi(doiTxt: String): JsResult[Doi] = Doi.parse(doiTxt).fold(
		err => JsError("DOI cannot be parsed: " + err.getMessage),
		doi => JsSuccess(doi)
	)

	given Format[Doi] with{
		def writes(doi: Doi): JsValue = JsString(doi.toString)
		def reads(json: JsValue): JsResult[Doi] = parseDoi(json.as[String])
	}
	given OFormat[SubjectScheme] = Json.format[SubjectScheme]
	given OFormat[Subject] = fieldConflatingFormat(Json.format[Subject], "scheme", opt = true)
	given OFormat[NameIdentifierScheme] = Json.format[NameIdentifierScheme]
	given OFormat[NameIdentifier] = fieldConflatingFormat(Json.format[NameIdentifier], "scheme")
	given genericNameFormat: OFormat[GenericName] = Json.format[GenericName]
	given personalNameFormat: OFormat[PersonalName] = Json.format[PersonalName]

	given OFormat[Name] with{
		def writes(name: Name) = name match{
			case gn: GenericName =>
				genericNameFormat.writes(gn) ++ Json.obj("nameType" -> "Organizational")
			case pn: PersonalName =>
				personalNameFormat.writes(pn) ++ Json.obj("nameType" -> "Personal")
		}
		def reads(js: JsValue) = (js \ "familyName") match {
			case JsDefined(JsString(_)) =>
				personalNameFormat.reads(js)

			case _ =>
				genericNameFormat.reads(js)
		}
	}

	given Format[Affiliation] with{
		def writes(affiliation: Affiliation): JsValue = Json.obj(
			"name" -> JsString(affiliation.name)
		)
		def reads(json: JsValue): JsResult[Affiliation] = (json \ "name") match {
			case JsDefined(JsString(name)) => JsSuccess(Affiliation(name))
			case _ => json.validate[String].map(Affiliation.apply)
		}
	}
	given OFormat[Creator] = fieldConflatingFormat(Json.format[Creator], "name")
	given OFormat[Contributor] = fieldConflatingFormat(Json.format[Contributor], "name")
	given OFormat[Title] = Json.format[Title]
	given OFormat[ResourceType] = Json.format[ResourceType]
	given OFormat[Date] = Json.format[Date]

	def parseVersion(versionTxt: String): JsResult[Version] = Version.parse(versionTxt).fold(
		err => JsError("Version cannot be parsed. " + err.getMessage),
		v => JsSuccess(v)
	)
	given Format[Version] with{
		def writes(o: Version): JsValue = {
			JsString(o.toString)
		}
		def reads(json: JsValue): JsResult[Version] = {
			parseVersion(json.as[String])
		}
	}
	given OFormat[Rights] = Json.format[Rights]
	given OFormat[Description] = Json.format[Description]

	given OFormat[DoiMeta] = Json.format[DoiMeta]
	given OFormat[DoiWrapper] = Json.format[DoiWrapper]
	given OFormat[SingleDoiPayload] = Json.format[SingleDoiPayload]
	given OFormat[DoiListMeta] = Json.format[DoiListMeta]
	given OFormat[DoiListPayload] = Json.format[DoiListPayload]
}
