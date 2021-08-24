package se.lu.nateko.cp.doi.core

import se.lu.nateko.cp.doi.{Doi, DoiMeta}
import se.lu.nateko.cp.doi.meta._
import spray.json._

object JsonSupport extends DefaultJsonProtocol{

	def enumFormat[T <: Enumeration](enum: T) = new RootJsonFormat[enum.Value] {
		def write(v: enum.Value) = JsString(v.toString)

		def read(value: JsValue): enum.Value = value match{
			case JsString(s) =>
				try{
					enum.withName(s)
				}catch{
					case _: NoSuchElementException => deserializationError(
						"Expected one of: " + enum.values.map(_.toString).mkString("'", "', '", "'")
					)
				}
			case _ => deserializationError("Expected a string")
		}
	}

	private def mergeFields(targetObj: JsValue, fields: (String, JsValue)*) = JsObject(
		targetObj.asJsObject.fields ++ fields
	)

	private def dropNameMerge(o1: JsValue, o2: JsValue) = JsObject(
		o1.asJsObject.fields - "name" ++ o2.asJsObject.fields
	)

	implicit val dateTypeFormat = enumFormat(DateType)
	implicit val contrTypeFormat = enumFormat(ContributorType)
	implicit val descriptionTypeFormat = enumFormat(DescriptionType)
	implicit val resourceTypeGeneralFormat = enumFormat(ResourceTypeGeneral)
	implicit val titleTypeFormat = enumFormat(TitleType)
	implicit val DoiStateEnumFormat = enumFormat(DoiPublicationState)
	implicit val DoiEventEnumFormat = enumFormat(DoiPublicationEvent)

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	implicit val doiFormat = new RootJsonFormat[Doi]{
		def write(doi: Doi): JsValue = JsString(doi.toString)
		def read(json: JsValue): Doi = Doi.parse(json.convertTo[String]).fold(
			err => deserializationError(err.getMessage),
			identity
		)
	}
	implicit val subjectShemeFormat = jsonFormat2(SubjectScheme.apply)
	implicit val subjectFormat = jsonFormat4(Subject)
	implicit val nameIdentifierSchemeFormat = jsonFormat2(NameIdentifierScheme.apply)
	implicit val nameIdentifierFormat = jsonFormat2(NameIdentifier.apply)
	implicit val genericNameFormat = jsonFormat1(GenericName)
	implicit val personalNameFormat = jsonFormat2(PersonalName)

	implicit val nameFormat = new JsonFormat[Name]{
		def write(name: Name) = name match{
			case gn: GenericName =>
				mergeFields(gn.toJson, "nameType" -> JsString("Organizational"))
			case pn: PersonalName =>
				mergeFields(pn.toJson, "nameType" -> JsString("Personal"))
		}
		def read(js: JsValue) =
			if(js.asJsObject.fields.contains("givenName"))
				personalNameFormat.read(js)
			else
				genericNameFormat.read(js)
	}

	private val vanillaCreatorFormat = jsonFormat3(Creator)

	implicit val creatorFormat = new JsonFormat[Creator]{
		def write(c: Creator): JsValue = {
			val nameJs = nameFormat.write(c.name)
			val vanilla = vanillaCreatorFormat.write(c)
			dropNameMerge(vanilla, nameJs)
		}
		def read(json: JsValue): Creator = {
			val patchedJson = mergeFields(json, "name" -> json)
			vanillaCreatorFormat.read(patchedJson)
		}
	}
	private val vanillaContributorFormat = jsonFormat4(Contributor)
	implicit val contributorFormat = new JsonFormat[Contributor]{
		def write(c: Contributor): JsValue = {
			val nameJs = nameFormat.write(c.name)
			val vanilla = vanillaContributorFormat.write(c)
			dropNameMerge(vanilla, nameJs)
		}
		def read(json: JsValue): Contributor = {
			val patchedJson = mergeFields(json, "name" -> json)
			vanillaContributorFormat.read(patchedJson)
		}
	}
	implicit val titleFormat = jsonFormat3(Title)
	implicit val resourceTypeFormat = jsonFormat2(ResourceType)
	implicit val dateFormat = jsonFormat2(Date.apply)

	private val versionRegex = """^(\d+).(\d+)$""".r

	implicit val versionFormat = new JsonFormat[Version]{
		def write(v: Version): JsValue = JsString(v.toString)
		def read(json: JsValue): Version = Version.parse(json.convertTo[String]).fold(
			err => deserializationError(err.getMessage),
			identity
		)
	}
	implicit val rightsFormat = jsonFormat2(Rights)
	implicit val descriptionFormat = jsonFormat3(Description)

	implicit val doiMetaFormat = jsonFormat16(DoiMeta)

}