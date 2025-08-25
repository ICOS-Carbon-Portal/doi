package se.lu.nateko.cp.doi.core

import se.lu.nateko.cp.doi._
import se.lu.nateko.cp.doi.meta._
import spray.json._
import scala.reflect.ClassTag

object JsonSupport extends DefaultJsonProtocol{

	def enumFormat[T <: reflect.Enum](valueOf: String => T)(using ctg: ClassTag[T]) = new RootJsonFormat[T] {
		def write(v: T) = JsString(v.toString)

		def read(value: JsValue): T = value match{
			case JsString(s) =>
				try{
					valueOf(s)
				}catch{
					case _: IllegalArgumentException => deserializationError(
						s"No such $ctg enum value: $s"
					)
				}
			case _ => deserializationError("Expected a string")
		}
	}

	private def mergeFields(targetObj: JsValue, fields: (String, JsValue)*) = JsObject(
		targetObj.asJsObject.fields ++ fields
	)

	private def fieldConflatingFormat[T](vanillaF: JsonFormat[T], field: String, opt: Boolean = false) = new RootJsonFormat[T]{
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

	given dateTypeFormat: RootJsonFormat[DateType] = enumFormat(DateType.valueOf)
	given contrTypeFormat: RootJsonFormat[ContributorType] = enumFormat(ContributorType.valueOf)
	given descriptionTypeFormat: RootJsonFormat[DescriptionType] = enumFormat(DescriptionType.valueOf)
	given resourceTypeGeneralFormat: RootJsonFormat[ResourceTypeGeneral] = enumFormat(ResourceTypeGeneral.valueOf)
	given titleTypeFormat: RootJsonFormat[TitleType] = enumFormat(TitleType.valueOf)
	given doiStateEnumFormat: RootJsonFormat[DoiPublicationState] = enumFormat(DoiPublicationState.valueOf)
	given doiEventEnumFormat: RootJsonFormat[DoiPublicationEvent] = enumFormat(DoiPublicationEvent.valueOf)

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	given RootJsonFormat[Doi] with{
		def write(doi: Doi): JsValue = JsString(doi.toString)
		def read(json: JsValue): Doi = Doi.parse(json.convertTo[String])
			.fold(err => deserializationError(err.getMessage), identity)
	}
	given RootJsonFormat[Subject] = jsonFormat3(Subject.apply)
	private val nameIdentifierSchemeFormat = jsonFormat2(NameIdentifierScheme.apply)

	given RootJsonFormat[NameIdentifierScheme] with {

		def write(ns: NameIdentifierScheme): JsValue = nameIdentifierSchemeFormat.write(ns)

		def read(json: JsValue) = {
			val dataCiteVersion = nameIdentifierSchemeFormat.read(json)

			NameIdentifierScheme.lookup(dataCiteVersion.nameIdentifierScheme).getOrElse(dataCiteVersion)
		}
	}

	given RootJsonFormat[NameIdentifier] = fieldConflatingFormat(jsonFormat2(NameIdentifier.apply), "scheme")

	given genericNameFormat: RootJsonFormat[GenericName] = jsonFormat1(GenericName.apply)
	given personalNameFormat: RootJsonFormat[PersonalName] = jsonFormat2(PersonalName.apply)

	given JsonFormat[Name] with{
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

	given JsonFormat[Affiliation] with{
		def write(affiliation: Affiliation) = JsObject(
			"name" -> JsString(affiliation.name)
		)

		def read(json: JsValue): Affiliation = json match {
			case JsObject(fields) => fields.get("name") match {
				case Some(JsString(name)) => Affiliation(name)
				case _ => deserializationError("Expected affiliation name")
			}
			case JsString(name) => Affiliation(name)
			case _ => deserializationError("Expected affiliation")
		}
	}
	given RootJsonFormat[Creator] = fieldConflatingFormat(jsonFormat3(Creator.apply), "name")
	given RootJsonFormat[Contributor] = fieldConflatingFormat(jsonFormat4(Contributor.apply), "name")
	given RootJsonFormat[Title] = jsonFormat3(Title.apply)
	given RootJsonFormat[ResourceType] = jsonFormat2(ResourceType.apply)
	given RootJsonFormat[Date] = jsonFormat2(Date.apply)

	private val versionRegex = """^(\d+).(\d+)$""".r

	given JsonFormat[Version] with{
		def write(v: Version): JsValue = JsString(v.toString)
		def read(json: JsValue): Version = Version.parse(json.convertTo[String]).fold(
			err => deserializationError(err.getMessage),
			identity
		)
	}
	given RootJsonFormat[Rights] = jsonFormat6(Rights.apply)

	given RootJsonFormat[Description] = jsonFormat3(Description.apply)

	private val funderIdentifierSchemeFormat = jsonFormat2(FunderIdentifierScheme.apply)
	given RootJsonFormat[FunderIdentifierScheme] with {

		def write(fs: FunderIdentifierScheme): JsValue = funderIdentifierSchemeFormat.write(fs)

		def read(json: JsValue) = {
			val dataCiteVersion = funderIdentifierSchemeFormat.read(json)

			FunderIdentifierScheme.lookup(dataCiteVersion.funderIdentifierType).getOrElse(dataCiteVersion)
		}
	}


	given RootJsonFormat[FunderIdentifier] = fieldConflatingFormat(jsonFormat2(FunderIdentifier.apply), "scheme", true)
	given RootJsonFormat[Award] = jsonFormat3(Award.apply)

	given RootJsonFormat[FundingReference] = fieldConflatingFormat(fieldConflatingFormat(jsonFormat3(FundingReference.apply), "funderIdentifier", true), "award", true)

	def latLonFormat[T <: Latitude | Longitude](factory: Double => T) = new RootJsonFormat[Option[T]]{
		def write(obj: Option[T]): JsValue = obj.fold(JsNull)(JsNumber.apply)
		def read(json: JsValue): Option[T] = json match
			case JsNull => None
			case JsString(s) => s.toDoubleOption.map(factory)
			case JsNumber(n) => Some(factory(n.toDouble))
			case _ => deserializationError("expected a lat/lon number")
	}
	given latFormat: RootJsonFormat[Option[Latitude]] = latLonFormat(Latitude.apply)
	given lonFormat: RootJsonFormat[Option[Longitude]] = latLonFormat(Longitude.apply)

	given RootJsonFormat[GeoLocationPoint] = jsonFormat2(GeoLocationPoint.apply)
	given RootJsonFormat[GeoLocationBox] = jsonFormat4(GeoLocationBox.apply)
	given RootJsonFormat[GeoLocation] = jsonFormat3(GeoLocation.apply)

	given RootJsonFormat[DoiMeta] = jsonFormat18(DoiMeta.apply)

	given RootJsonFormat[DoiWrapper] = jsonFormat1(DoiWrapper.apply)
	given RootJsonFormat[SingleDoiPayload] = jsonFormat1(SingleDoiPayload.apply)
	given RootJsonFormat[DoiListMeta] = jsonFormat3(DoiListMeta.apply)
	given RootJsonFormat[DoiListPayload] = jsonFormat2(DoiListPayload.apply)
}
