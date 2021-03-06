package se.lu.nateko.cp.doi

import se.lu.nateko.cp.doi.meta._
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.collection.Seq

case class Doi(prefix: String, suffix: String) extends SelfValidating{
	override def toString = prefix + "/" + suffix

	def error: Option[String] = joinErrors(
		nonEmpty(prefix)("DOI prefix is required"),
		Doi.suffixError(suffix),
		if(prefix.startsWith("10.")) None else Some("Prefix must start with \"10.\"")
	)
}

object Doi{
	private val suffixRegex = """^[\d\w\.\-]+$""".r
	private val DoiRegex = """(10\.\d+)/(.+)""".r

	def suffixError(suffix: String) =
		if(suffixRegex.findFirstIn(suffix).isDefined) None else Some("Invalid DOI suffix")

	def parse(doiStr: String): Try[Doi] = doiStr match {
		case DoiRegex(prefix, suffix) =>
			val doi = Doi(prefix, suffix)
			doi.error.fold[Try[Doi]](Success(doi)){err =>
				Failure(new Exception(err))
			}
		case _ => Failure(new Exception("Error parsing a DOI, expected string of the form 10.nnnn/xxxxx, got " + doiStr))
	}
}

//TODO Move Doi out of DoiMeta
case class DoiMeta(
	id: Doi,
	creators: Seq[Creator],
	titles: Seq[Title],
	publisher: String,
	publicationYear: Int,
	resourceType: ResourceType,
	subjects: Seq[Subject] = Nil,
	contributors: Seq[Contributor] = Nil,
	dates: Seq[Date] = Nil,
	formats: Seq[String] = Nil,
	version: Option[Version] = None,
	rights: Seq[Rights] = Nil,
	descriptions: Seq[Description] = Nil
) extends SelfValidating{

	def error: Option[String] = joinErrors(
		id.error,
		nonEmptyAllGood(creators)("At least one creator is required"),
		nonEmptyAllGood(titles)("At least one title is required"),
		nonEmpty(publisher)("Publisher is required"),
		if(publicationYear < 1000 || publicationYear > 3000) Some("Invalid publication year") else None,
		resourceType.error,
		allGood(subjects),
		allGood(contributors),
		allGood(dates),
		eachNonEmpty(formats)("Format is not required but must not be empty if specified"),
		version.flatMap(_.error),
		allGood(rights),
		allGood(descriptions)
	)
}
