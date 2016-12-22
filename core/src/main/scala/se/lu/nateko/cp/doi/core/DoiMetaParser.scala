package se.lu.nateko.cp.doi.core

import scala.xml.Node
import scala.util.Success
import scala.util.Failure
import scala.util.Try

import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta._

object DoiMetaParser {

	def parse(xml: Node): Try[DoiMeta] = {
		val publisher = (xml \ "publisher").text.trim
		val publicationYearTry = Try((xml \ "publicationYear").text.trim.toInt)
		val metaTry = for(
			doi <- parseDoi(xml);
			creators <- parseCreators(xml);
			titles <- parseTitles(xml);
			publicationYear <- publicationYearTry
		) yield DoiMeta(doi, creators, titles, publisher, publicationYear)

		metaTry.flatMap(validate)
	}

	private val doiRegex = """^(10\.\d+)/(.+)$""".r

	def parseDoi(xml: Node): Try[Doi] = {
		val idNodes = xml \ "identifier"
		val doiTxt: String = idNodes.text.trim

		doiTxt match{
			case doiRegex(prefix, suffix) => validate(Doi(prefix, suffix))
			case _ => fail("Wrong DOI ID syntax: " + doiTxt)
		}
	}

	def parseCreators(xml: Node): Try[Seq[Creator]] = Try{
		val creatorTries = (xml \ "creators" \ "creator").map(parseCreator)
		creatorTries.map(_.get)
	}

	def parseTitles(xml: Node): Try[Seq[Title]] = Try{
		val titleTries = (xml \ "titles" \ "title").map(parseTitle)
		titleTries.map(_.get)
	}

	private def parseCreator(xml: Node): Try[Creator] = {
		val nameNodes = xml \ "creatorName"
		val nameTxt = nameNodes.text.trim
		//TODO Parse other attributes of creators (nameId and affiliations)
		validate(Creator(GenericName(nameTxt), None, Nil))
	}

	private def parseTitle(xml: Node): Try[Title] = {
		val titleTxt = xml.text.trim
		//TODO Parse other attributes of titles (lang and titleType)
		validate(Title(titleTxt, None, None))
	}

	private def fail[T](msg: String): Try[T] = Failure(new Exception(msg))

	private def validate[T <: SelfValidating](meta: T): Try[T] = meta.error match{
		case None => Success(meta)
		case Some(msg) => fail(msg)
	}
}