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

object DoiMetaParser {

	def parse(xml: Node): Try[DoiMeta] = {

		val creators = (xml \ "creators" \ "creator").map(parseCreator)
		val publisher = (xml \ "publisher").text.trim
		val publicationYearString = (xml \ "publicationYear").text.trim
		//TODO Parse contributors
		val contributors = Nil

		val metaTry = for(
			doi <- parseDoi(xml);
			titles <- tryAll((xml \ "titles" \ "title").map(parseTitle));
			publicationYear <- Try(publicationYearString.toInt);
			resourceType <- parseResourceType(xml)
		) yield DoiMeta(doi, creators, contributors, titles, publisher, publicationYear, resourceType)

		metaTry.flatMap(validate)
	}

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	def parseDoi(xml: Node): Try[Doi] = {
		val idNodes = xml \ "identifier"
		val doiTxt: String = idNodes.text.trim

		doiTxt match{
			case doiRegex(prefix, suffix) => Success(Doi(prefix, suffix))
			case _ => fail("Wrong DOI ID syntax: " + doiTxt)
		}
	}

	def parseResourceType(xml: Node): Try[ResourceType] = {
		val resTyp = xml \ "resourceType"
		val resTypGen = resTyp \@ "resourceTypeGeneral"
		
		inEnum(ResourceTypeGeneral, resTypGen).map(ResourceType(resTyp.text.trim, _))
	}

	private def parseCreator(xml: Node): Creator = {
		val name = parseName(xml, "creatorName")
		val affiliations = (xml \ "affiliation").map(_.text.trim)
		val nameIds = (xml \ "nameIdentifier").map(parseNameId)
		Creator(name, nameIds, affiliations)
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
		val lang = xml.attribute(XML.namespace, "lang").map(_.text)
		val titleTypeOpt = xml.attribute("titleType").map(tType => inEnum(TitleType, tType.text))

		titleTypeOpt match {
			case None => Success(Title(titleTxt, lang, None))
			case Some(Success(titleType)) => Success(Title(titleTxt, lang, Some(titleType)))
			case Some(Failure(exc)) => Failure(exc)
		}
	}

	private def fail[T](msg: String): Try[T] = Failure(new Exception(msg) with NoStackTrace)

	private def validate[T <: SelfValidating](meta: T): Try[T] = meta.error match{
		case None => Success(meta)
		case Some(msg) => fail(msg)
	}

	private def tryAll[T](all: Seq[Try[T]]): Try[Seq[T]] = {
		var error: Throwable = null
		val iter = all.iterator
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
