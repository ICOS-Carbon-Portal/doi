package se.lu.nateko.cp.doi

import play.api.libs.json._
import se.lu.nateko.cp.doi.meta._

/**
TODO When/if spray-json gets published for ScalaJS, retire this code by moving
spray-json based JsonSupport from core project to common project
*/
object JsonSupport{

	private def enumFormat[T <: Enumeration](enum: T) = new Format[enum.Value]{
		def writes(v: enum.Value) = JsString(v.toString)
		def reads(js: JsValue): JsResult[enum.Value] = js match{
			case JsString(s) => try{
					JsSuccess(enum.withName(s.toString))
				} catch{
					case _: NoSuchElementException =>
						JsError(s"No such value: $s")
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


	implicit val dateTypeFormat = enumFormat(DateType)
	implicit val contrTypeFormat = enumFormat(ContributorType)
	implicit val descriptionTypeFormat = enumFormat(DescriptionType)
	implicit val resourceTypeGeneralFormat = enumFormat(ResourceTypeGeneral)
	implicit val titleTypeFormat = enumFormat(TitleType)
	implicit val DoiStateEnumFormat = enumFormat(DoiPublicationState)
	implicit val DoiEventEnumFormat = enumFormat(DoiPublicationEvent)

	private def parseDoi(doiTxt: String): JsResult[Doi] = Doi.parse(doiTxt).fold(
		err => JsError("DOI cannot be parsed: " + err.getMessage),
		doi => JsSuccess(doi)
	)

	implicit val doiFormat = new Format[Doi]{
		def writes(doi: Doi): JsValue = JsString(doi.toString)
		def reads(json: JsValue): JsResult[Doi] = parseDoi(json.as[String])
	}
	implicit val subjectShemeFormat = Json.format[SubjectScheme]
	implicit val subjectFormat = fieldConflatingFormat(Json.format[Subject], "scheme", opt = true)
	implicit val nameIdentifierSchemeFormat = Json.format[NameIdentifierScheme]
	implicit val nameIdentifierFormat = fieldConflatingFormat(Json.format[NameIdentifier], "scheme")
	implicit val genericNameFormat = Json.format[GenericName]
	implicit val personalNameFormat = Json.format[PersonalName]

	implicit val nameFormat = new OFormat[Name]{
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

	implicit val creatorFormat = fieldConflatingFormat(Json.format[Creator], "name")
	implicit val contributorFormat = fieldConflatingFormat(Json.format[Contributor], "name")
	implicit val titleFormat = Json.format[Title]
	implicit val resourceTypeFormat = Json.format[ResourceType]
	implicit val dateFormat = Json.format[Date]

	def parseVersion(versionTxt: String): JsResult[Version] = Version.parse(versionTxt).fold(
		err => JsError("Version cannot be parsed. " + err.getMessage),
		v => JsSuccess(v)
	)
	implicit val versionFormat = new Format[Version]{
		def writes(o: Version): JsValue = {
			JsString(o.toString)
		}
		def reads(json: JsValue): JsResult[Version] = {
			parseVersion(json.as[String])
		}
	}
	implicit val rightsFormat = Json.format[Rights]
	implicit val descriptionFormat = Json.format[Description]

	implicit val doiMetaFormat = Json.format[DoiMeta]
}
