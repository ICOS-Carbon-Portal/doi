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

case class DoiMeta(
	doi: Doi,
	state: DoiPublicationState = draft,
	event: Option[DoiPublicationEvent] = None,
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
	url: Option[String] = None,
	fundingReferences: Option[Seq[FundingReference]] = None,
	geoLocations: Option[Seq[GeoLocation]] = None,
	relatedIdentifiers: Option[Seq[RelatedIdentifier]] = None
) extends SelfValidating{

	def draftError: Option[String] = joinErrors(draftErrors)

	private def draftErrors: Seq[Option[String]] = Seq(
		doi.error,
		allGood(creators),
		titles.flatMap(allGood),
		publicationYear.flatMap(p => if(p < 1000 || p > 3000) Some("Invalid publication year") else None),
		allGood(subjects),
		allGood(contributors),
		allGood(dates),
		eachNonEmpty(formats)("Format is not required but must not be empty if specified"),
		version.flatMap(_.error),
		rightsList.flatMap(r => allGood(r)),
		allGood(descriptions),
		fundingReferences.flatMap(allGood),
		geoLocations.flatMap(allGood),
		relatedIdentifiers.flatMap(allGood)
	)

	def error: Option[String] = joinErrors(
		nonEmpty(creators)("At least one creator is required") +:
		nonEmpty(titles)("At least one title is required") +:
		nonEmpty(publisher.getOrElse(""))("Publisher is required") +:
		nonEmpty(publicationYear)("Publication year is required") +:
		nonEmptyAllGood(types)("Resource type is required") +:
		draftErrors
	)
}

case class DoiWrapper(attributes: DoiMeta)
case class SingleDoiPayload(data: DoiWrapper)
case class DoiListMeta(total: Int, totalPages: Int, page: Int)
case class DoiListPayload(data: Seq[DoiWrapper], meta: DoiListMeta)
