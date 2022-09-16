package se.lu.nateko.cp.doi.core

import se.lu.nateko.cp.doi._
import se.lu.nateko.cp.doi.meta._
import spray.json._

object JsonSupport extends DefaultJsonProtocol{

	def enumFormat[T <: Enumeration](libEnum: T) = new RootJsonFormat[libEnum.Value] {
		def write(v: libEnum.Value) = JsString(v.toString)

		def read(value: JsValue): libEnum.Value = value match{
			case JsString(s) =>
				try{
					libEnum.withName(s)
				}catch{
					case _: NoSuchElementException => deserializationError(
						"Expected one of: " + libEnum.values.map(_.toString).mkString("'", "', '", "'")
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

	given dateTypeFormat: RootJsonFormat[DateType.Value] = enumFormat(DateType)
	given contrTypeFormat: RootJsonFormat[ContributorType.Value] = enumFormat(ContributorType)
	given descriptionTypeFormat: RootJsonFormat[DescriptionType.Value] = enumFormat(DescriptionType)
	given resourceTypeGeneralFormat: RootJsonFormat[ResourceTypeGeneral.Value] = enumFormat(ResourceTypeGeneral)
	given titleTypeFormat: RootJsonFormat[TitleType.Value] = enumFormat(TitleType)
	given doiStateEnumFormat: RootJsonFormat[DoiPublicationState.Value] = enumFormat(DoiPublicationState)
	given doiEventEnumFormat: RootJsonFormat[DoiPublicationEvent.Value] = enumFormat(DoiPublicationEvent)

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	given RootJsonFormat[Doi] with{
		def write(doi: Doi): JsValue = JsString(doi.toString)
		def read(json: JsValue): Doi = Doi.parse(json.convertTo[String]).fold(
			err => deserializationError(err.getMessage),
			identity
		)
	}
	given RootJsonFormat[SubjectScheme] = jsonFormat2(SubjectScheme.apply)
	given RootJsonFormat[Subject] = fieldConflatingFormat(jsonFormat4(Subject.apply), "scheme", opt = true)
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
	given RootJsonFormat[Rights] = jsonFormat2(Rights.apply)
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

	given RootJsonFormat[GeoLocationPoint] with {
		def write(p: GeoLocationPoint): JsValue = JsObject(
				"pointLongitude" -> JsString(p.pointLongitude.getOrElse(0).toString),
				"pointLatitude" -> JsString(p.pointLatitude.getOrElse(0).toString)
			)

		def read(json: JsValue) = json.asJsObject.getFields("pointLongitude", "pointLatitude") match {
				case List(JsString(pLong), JsString(pLat)) => new GeoLocationPoint(Some(pLong.toDouble), Some(pLat.toDouble))
				case _ => deserializationError("Expected geolocation point")
			}
	}

	given RootJsonFormat[GeoLocationBox] with {
		def write(g: GeoLocationBox): JsValue = JsObject(
			"westBoundLongitude" -> JsString(g.westBoundLongitude.getOrElse(0).toString),
			"eastBoundLongitude" -> JsString(g.eastBoundLongitude.getOrElse(0).toString),
			"southBoundLatitude" -> JsString(g.southBoundLatitude.getOrElse(0).toString),
			"northBoundLatitude" -> JsString(g.northBoundLatitude.getOrElse(0).toString)
		)

		def read(json: JsValue): GeoLocationBox = json.asJsObject.getFields("westBoundLongitude", "eastBoundLongitude", "southBoundLatitude", "northBoundLatitude") match {
			case List(JsString(westLong), JsString(eastLong), JsString(northLat), JsString(southLat)) => 
				new GeoLocationBox(Some(westLong.toDouble), Some(eastLong.toDouble), Some(northLat.toDouble), Some(southLat.toDouble))
			case _ => deserializationError("Expected geolocation box")
		}
	}

	given RootJsonFormat[GeoLocation] = jsonFormat3(GeoLocation.apply)

	given RootJsonFormat[DoiMeta] = jsonFormat18(DoiMeta.apply)

	given RootJsonFormat[DoiWrapper] = jsonFormat1(DoiWrapper.apply)
	given RootJsonFormat[SingleDoiPayload] = jsonFormat1(SingleDoiPayload.apply)
	given RootJsonFormat[DoiListMeta] = jsonFormat3(DoiListMeta.apply)
	given RootJsonFormat[DoiListPayload] = jsonFormat2(DoiListPayload.apply)
}