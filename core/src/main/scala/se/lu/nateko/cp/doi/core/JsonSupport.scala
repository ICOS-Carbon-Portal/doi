package se.lu.nateko.cp.doi.core

import se.lu.nateko.cp.doi._
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

	private def fieldConflatingFormat[T](vanillaF: JsonFormat[T], field: String, opt: Boolean = false) = new JsonFormat[T]{
		def write(obj: T): JsValue = {
			val vanilla = vanillaF.write(obj).asJsObject.fields
			val innerFields = vanilla.get(field).toSeq.flatMap(_.asJsObject.fields.toSeq)
			JsObject(vanilla - field ++ innerFields)
		}
		def read(js: JsValue): T = {
			val patchedJson = mergeFields(js, field -> js)
			try{
				vanillaF.read(patchedJson)
			} catch{
				case _: Throwable if(opt) => vanillaF.read(js)
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

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	implicit val doiFormat = new RootJsonFormat[Doi]{
		def write(doi: Doi): JsValue = JsString(doi.toString)
		def read(json: JsValue): Doi = Doi.parse(json.convertTo[String]).fold(
			err => deserializationError(err.getMessage),
			identity
		)
	}
	implicit val subjectShemeFormat = jsonFormat2(SubjectScheme.apply)
	implicit val subjectFormat = fieldConflatingFormat(jsonFormat4(Subject), "scheme", opt = true)
	implicit val nameIdentifierSchemeFormat = jsonFormat2(NameIdentifierScheme.apply)
	implicit val nameIdentifierFormat = fieldConflatingFormat(jsonFormat2(NameIdentifier.apply), "scheme")

	implicit val genericNameFormat = jsonFormat1(GenericName)
	implicit val personalNameFormat = jsonFormat2(PersonalName)

	implicit val nameFormat = new JsonFormat[Name]{
		def write(name: Name) = name match{
			case gn: GenericName =>
				mergeFields(gn.toJson, "nameType" -> JsString("Organizational"))
			case pn: PersonalName =>
				mergeFields(pn.toJson, "nameType" -> JsString("Personal"))
		}
		def read(js: JsValue) = js.asJsObject.fields.get("familyName") match{
			case Some(JsString(_)) => personalNameFormat.read(js)
			case _ => genericNameFormat.read(js)
		}
	}

	implicit val creatorFormat = fieldConflatingFormat(jsonFormat3(Creator), "name")
	implicit val contributorFormat = fieldConflatingFormat(jsonFormat4(Contributor), "name")
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

	implicit val doiWrapperFormat = jsonFormat1(DoiWrapper)
	implicit val singleDoiPayloadFormat = jsonFormat1(SingleDoiPayload)
	implicit val doiListMeta = jsonFormat3(DoiListMeta)
	implicit val doiListPayloadFormat = jsonFormat2(DoiListPayload)
}