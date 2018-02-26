package se.lu.nateko.cp.doi

import play.api.libs.json._
import se.lu.nateko.cp.doi.meta._

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

	implicit val dateTypeFormat = enumFormat(DateType)
	implicit val contrTypeFormat = enumFormat(ContributorType)
	implicit val descriptionTypeFormat = enumFormat(DescriptionType)
	implicit val resourceTypeGeneralFormat = enumFormat(ResourceTypeGeneral)
	implicit val titleTypeFormat = enumFormat(TitleType)

	implicit val doiFormat = Json.format[Doi]
	implicit val subjectShemeFormat = Json.format[SubjectScheme]
	implicit val subjectFormat = Json.format[Subject]
	implicit val nameIdentifierSchemeFormat = Json.format[NameIdentifierScheme]
	implicit val nameIdentifierFormat = Json.format[NameIdentifier]
	implicit val genericNameFormat = Json.format[GenericName]
	implicit val personalNameFormat = Json.format[PersonalName]

	implicit val nameFormat = new Format[Name]{
		def writes(name: Name) = name match{
			case gn: GenericName => genericNameFormat.writes(gn)
			case pn: PersonalName => personalNameFormat.writes(pn)
		}
		def reads(js: JsValue): JsResult[Name] = js match {
			case JsObject(fields) if(fields.contains("givenName")) =>
				personalNameFormat.reads(js)
			case _ =>
				genericNameFormat.reads(js)
		}
	}
	implicit val creatorFormat = Json.format[Creator]
	implicit val titleFormat = Json.format[Title]
	implicit val resourceTypeFormat = Json.format[ResourceType]
	implicit val contributorFormat = Json.format[Contributor]
	implicit val dateFormat = Json.format[Date]
	implicit val versionFormat = Json.format[Version]
	implicit val rightsFormat = Json.format[Rights]
	implicit val descriptionFormat = Json.format[Description]

	implicit val doiMetaFormat = Json.format[DoiMeta]

	implicit val prefixInfoFormat = Json.format[PrefixInfo]
}
