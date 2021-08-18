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
	implicit val DoiStateEnumFormat = enumFormat(DoiPublicationState)
	implicit val DoiEventEnumFormat = enumFormat(DoiPublicationEvent)

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	def parseDoi(doiTxt: String): JsResult[Doi] = doiTxt match {
		case doiRegex(prefix, suffix) => JsSuccess(Doi(prefix, suffix.toUpperCase()))
		case _ => JsError("DOI cannot be parsed")
	}

	implicit val doiFormat = new Format[Doi]{
		def writes(doi: Doi): JsValue = JsString(s"${doi.prefix}/${doi.suffix}")
		def reads(json: JsValue): JsResult[Doi] = parseDoi(json.as[String])
	}
	implicit val subjectShemeFormat = Json.format[SubjectScheme]
	implicit val subjectFormat = Json.format[Subject]
	implicit val nameIdentifierSchemeFormat = Json.format[NameIdentifierScheme]
	implicit val nameIdentifierFormat = Json.format[NameIdentifier]
	implicit val genericNameFormat = Json.format[GenericName]
	implicit val personalNameFormat = Json.format[PersonalName]

	implicit val nameFormat = new OFormat[Name]{
		def writes(name: Name) = name match{
			case gn: GenericName =>
				genericNameFormat.writes(gn) ++ Json.obj("nameType" -> "Organizational")
			case pn: PersonalName =>
				personalNameFormat.writes(pn) ++ Json.obj("nameType" -> "Personal")
		}
		def reads(js: JsValue) = (js \ "givenName") match {
			case JsDefined(JsString(_)) =>
				personalNameFormat.reads(js)

			case _ =>
				genericNameFormat.reads(js)
		}
	}

	private val vanillaCreatorFormat = Json.format[Creator]

	implicit val creatorFormat = new Format[Creator]{
		def writes(c: Creator): JsValue = {
			val nameJs = nameFormat.writes(c.name)
			val vanilla = vanillaCreatorFormat.writes(c)

			vanilla - "name" ++ nameJs
		}
		def reads(json: JsValue): JsResult[Creator] = {
			val patchedJson = json.as[JsObject] + ("name" -> json)
			vanillaCreatorFormat.reads(patchedJson)
		}
	}
	private val vanillaContributorFormat = Json.format[Contributor]
	implicit val contributorFormat = new Format[Contributor]{
		def writes(c: Contributor): JsValue = {
			val nameJs = nameFormat.writes(c.name)
			val vanilla = vanillaContributorFormat.writes(c)

			vanilla - "name" ++ nameJs
		}
		def reads(json: JsValue): JsResult[Contributor] = {
			val patchedJson = json.as[JsObject] + ("name" -> json)
			vanillaContributorFormat.reads(patchedJson)
		}
	}
	implicit val titleFormat = Json.format[Title]
	implicit val resourceTypeFormat = Json.format[ResourceType]
	implicit val dateFormat = Json.format[Date]

	private val versionRegex = """^(\d+).(\d+)$""".r

	def parseVersion(versionTxt: String): JsResult[Version] = versionTxt match {
		case versionRegex(major, minor) => JsSuccess(Version(major.toInt, minor.toInt))
		case _ => JsError("Version cannot be parsed")
	}
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
