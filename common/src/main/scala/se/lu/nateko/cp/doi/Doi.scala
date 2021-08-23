package se.lu.nateko.cp.doi

import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.meta.DoiPublicationState._
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
			val doi = Doi(prefix, suffix.toUpperCase)
			doi.error.fold[Try[Doi]](Success(doi)){err =>
				Failure(new Exception(err))
			}
		case _ => Failure(new Exception("Error parsing a DOI, expected string of the form 10.nnnn/xxxxx, got " + doiStr))
	}
}

//TODO Move Doi out of DoiMeta
case class DoiMeta(
	doi: Doi,
	state: DoiPublicationState.Value = draft,
	event: Option[DoiPublicationEvent.Value] = None,
	creators: Seq[Creator] = Seq(),
	titles: Option[Seq[Title]] = None,
	publisher: Option[String] = None,
	publicationYear: Option[Int] = None,
	types: Option[ResourceType] = None,
	subjects: Seq[Subject] = Nil,
	contributors: Seq[Contributor] = Nil,
	dates: Seq[Date] = Nil,
	formats: Seq[String] = Nil,
	version: Option[Version] = None,
	rightsList: Option[Seq[Rights]] = None,
	descriptions: Seq[Description] = Nil,
	url: Option[String] = None
) extends SelfValidating{

	def draftError: Option[String] = joinErrors(
		doi.error,
		allGood(creators),
		titles.flatMap(t => allGood(t)),
		publicationYear.map(p => if(p < 1000 || p > 3000) Some("Invalid publication year") else None).getOrElse(None),
		allGood(subjects),
		allGood(contributors),
		allGood(dates),
		eachNonEmpty(formats)("Format is not required but must not be empty if specified"),
		version.flatMap(_.error),
		rightsList.flatMap(r => allGood(r)),
		allGood(descriptions)
	)

	def error: Option[String] = joinErrors(
		doi.error,
		nonEmptyAllGood(creators)("At least one creator is required"),
		titles.fold[Option[String]](Some("At least one title is required"))(t => allGood(t)),
		nonEmpty(publisher.fold("")(p => p))("Publisher is required"),
		publicationYear.fold[Option[String]](Some("Publication year is required"))(y => if(y < 1000 || y > 3000) Some("Invalid publication year") else None),
		types.fold[Option[String]](Some("Resource type is required"))(r => r.error),
		allGood(subjects),
		allGood(contributors),
		allGood(dates),
		eachNonEmpty(formats)("Format is not required but must not be empty if specified"),
		version.flatMap(_.error),
		rightsList.flatMap(r => allGood(r)),
		allGood(descriptions)
	)
}
