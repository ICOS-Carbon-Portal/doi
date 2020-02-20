package se.lu.nateko.cp.doi.core

import scala.xml.Node
import scala.util.Success
import scala.util.Failure
import scala.util.Try

import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta._
import scala.util.control.NoStackTrace
import scala.xml.XML
import scala.collection.mutable.Buffer
import scala.collection.Seq

object DoiMetaParser {

	def parse(xml: Node): Try[DoiMeta] = {

		val creators = (xml \ "creators" \ "creator").map(parseCreator)
		val publisher = (xml \ "publisher").text.trim
		val publicationYearString = (xml \ "publicationYear").text.trim
		val subjects = (xml \ "subjects" \ "subject").map(parseSubject)
		val formats = (xml \ "formats" \ "format").map(_.text.trim)
		val rights = (xml \ "rightsList" \ "rights").map(parseRights)

		val metaTry = for(
			doi <- parseDoi(xml);
			contributors <- tryAll((xml \ "contributors" \ "contributor").map(parseContributor));
			titles <- tryAll((xml \ "titles" \ "title").map(parseTitle));
			publicationYear <- Try(publicationYearString.toInt);
			resourceType <- parseResourceType(xml);
			dates <- tryAll((xml \ "dates" \ "date").map(parseDate));
			version <- parseVersion(xml);
			descriptions <- tryAll((xml \ "descriptions" \ "description").map(parseDescription))
		) yield DoiMeta(
			doi, creators, titles, publisher, publicationYear, resourceType, subjects,
			contributors, dates, formats, version, rights, descriptions
		)

		metaTry.flatMap(validate)
	}

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	def parseDoi(doiTxt: String): Try[Doi] = doiTxt match{
		case doiRegex(prefix, suffix) => Success(Doi(prefix, suffix.toUpperCase))
		case _ => fail("Wrong DOI ID syntax: " + doiTxt)
	}

	private def parseDoi(xml: Node): Try[Doi] = parseDoi((xml \ "identifier").text.trim)

	private def parseResourceType(xml: Node): Try[ResourceType] = {
		val resTyp = xml \ "resourceType"
		val resTypGen = resTyp \@ "resourceTypeGeneral"

		inEnum(ResourceTypeGeneral, resTypGen).map(ResourceType(resTyp.text.trim, _))
	}

	private def parseCreator(xml: Node): Creator = parsePerson(xml, "creator", Creator)
	private def parseContributor(xml: Node): Try[Contributor] = {
		val contrTypeText = xml \@ "contributorType"

		inEnum(ContributorType, contrTypeText).map{contrType =>
			parsePerson(xml, "contributor", Contributor.apply(_, _, _, contrType))
		}
	}

	private type PersonConstructor[T <: Person] = (Name, Seq[NameIdentifier], Seq[String]) => T

	private def parsePerson[T <: Person](xml: Node, kind: String, cons:  PersonConstructor[T]): T = {
		val name = parseName(xml, kind + "Name")
		val nameIds = (xml \ "nameIdentifier").map(parseNameId)
		val affiliations = (xml \ "affiliation").map(_.text.trim)
		cons(name, nameIds, affiliations)
	}

	private def parseName(xml: Node, genericTag: String): Name = {
		val given = (xml \ "givenName").text.trim
		val family = (xml \ "familyName").text.trim

		if(given.isEmpty || family.isEmpty)
			GenericName((xml \ genericTag).text.trim)
		else
			PersonalName(given, family)
	}

	private def parseNameId(xml: Node): NameIdentifier = {
		val id = xml.text.trim
		val scheme = xml \@ "nameIdentifierScheme"
		val schemeUri = xml.attribute("schemeURI").map(_.text)
		NameIdentifier(id, NameIdentifierScheme(scheme, schemeUri))
	}

	private def parseTitle(xml: Node): Try[Title] = {
		val titleTxt = xml.text.trim
		val lang = parseLang(xml)
		val titleTypeOpt = xml.attribute("titleType").map(tType => inEnum(TitleType, tType.text))

		titleTypeOpt match {
			case None => Success(Title(titleTxt, lang, None))
			case Some(Success(titleType)) => Success(Title(titleTxt, lang, Some(titleType)))
			case Some(Failure(exc)) => Failure(exc)
		}
	}

	private def parseSubject(xml: Node): Subject = {
		val lang = parseLang(xml)
		val scheme = parseSubjectScheme(xml)
		val valueUri = xml.attribute("valueURI").map(_.text)
		Subject(xml.text.trim, lang, scheme, valueUri)
	}

	private def parseSubjectScheme(xml: Node): Option[SubjectScheme] = {
		val schemeOpt = xml.attribute("subjectScheme").map(_.text)
		val schemeUri = xml.attribute("schemeURI").map(_.text)
		schemeOpt.orElse(schemeUri).map(scheme =>
			SubjectScheme(scheme, schemeUri)
		)
	}

	private def parseDate(xml: Node): Try[Date] = {
		val dateTypeText = xml \@ "dateType"

		inEnum(DateType, dateTypeText).map(Date(xml.text.trim, _))
	}

	private[this] val versionRegex = """^(\d{1,2})\.(\d{1,2})""".r
	private def parseVersion(xml: Node): Try[Option[Version]] = {
		val versTag = xml \ "version"
		if(versTag.isEmpty) Success(None)
		else {
			val versText = versTag.text.trim
			versText match {
				case versionRegex(major, minor) => Success(Some(Version(major.toInt, minor.toInt)))
				case _ => fail(s"Unsupported version format: '$versText', use 'N[N].N[N]'")
			}
		}
	}

	private def parseRights(xml: Node) =
		Rights(xml.text.trim, xml.attribute("rightsURI").map(_.text))


	private def parseDescription(xml: Node): Try[Description] = {
		val lang = parseLang(xml)
		val descrTypeText = xml \@ "descriptionType"
		inEnum(DescriptionType, descrTypeText).map(descrType =>
			Description(xml.text.trim, descrType, lang)
		)
	}

	private def parseLang(xml: Node): Option[String] =
		xml.attribute(XML.namespace, "lang").map(_.text)

	private def fail[T](msg: String): Try[T] = Failure(new Exception(msg) with NoStackTrace)

	private def validate[T <: SelfValidating](meta: T): Try[T] = meta.error match{
		case None => Success(meta)
		case Some(msg) => fail(msg)
	}

	def tryAll[T](all: Seq[Try[T]]): Try[Seq[T]] = tryAll(all.iterator)

	def tryAll[T](iter: Iterator[Try[T]]): Try[Seq[T]] = {
		var error: Throwable = null
		val buff = Buffer.empty[T]
		while(iter.hasNext && error == null){
			iter.next() match{
				case Success(t) => buff += t
				case Failure(f) => error = f
			}
		}
		if(error == null) Success(buff)
		else Failure(error)
	}

	private def inEnum(enum: Enumeration, name: String): Try[enum.Value] = {
		try{
			Success(enum.withName(name))
		}catch{
			case e: NoSuchElementException => fail(s"Unknown $enum: $name")
		}
	}
}
