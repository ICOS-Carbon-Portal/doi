package se.lu.nateko.cp.doi

import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.meta.DoiPublicationState._
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.collection.Seq

case class Doi(prefix: String, suffix: String) extends SelfValidating{
	override def toString = prefix + "/" + suffix

	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmptyString(prefix, ValidationSection.DoiTarget, "DOI prefix is required", List("prefix")),
		Doi.suffixError(suffix).map(msg => mkError(ValidationSection.DoiTarget, msg, List("suffix"))).toSeq,
		if (!prefix.startsWith("10.")) Seq(mkError(ValidationSection.DoiTarget, "Prefix must start with \"10.\"", List("prefix"))) else Seq.empty
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
			if (doi.isValid) Success(doi)
			else Failure(new Exception(doi.errorMessage.getOrElse("Invalid DOI")))
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

	def draftErrors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		// Collect nested validation errors
		errs ++= doi.errors
		errs ++= collectErrors(creators, ValidationSection.Creators, List("creators"))
		
		titles.foreach { ts =>
			ts.zipWithIndex.foreach { case (title, idx) =>
				errs ++= title.errors.map(e => e.copy(
					section = ValidationSection.Titles,
					path = List("titles", idx.toString) ::: e.path
				))
			}
		}

		// Publication year range check
		publicationYear.foreach { year =>
			if (year < 1000 || year > 3000)
				errs += mkError(ValidationSection.PublicationYear, "Invalid publication year", List("publicationYear"))
		}

		errs ++= collectErrors(subjects, ValidationSection.Subjects, List("subjects"))
		errs ++= collectErrors(contributors, ValidationSection.Contributors, List("contributors"))
		errs ++= collectErrors(dates, ValidationSection.Dates, List("dates"))
		
		// Formats validation
		errs ++= requireEachNonEmpty(formats, ValidationSection.Formats, "Format is not required but must not be empty if specified", List("formats"))

		// Version validation
		version.foreach { v =>
			errs ++= v.errors.map(e => e.copy(section = ValidationSection.Version, path = List("version") ::: e.path))
		}

		// Rights validation
		rightsList.foreach { rs =>
			rs.zipWithIndex.foreach { case (rights, idx) =>
				errs ++= rights.errors.map(e => e.copy(
					section = ValidationSection.Rights,
					path = List("rightsList", idx.toString) ::: e.path
				))
			}
		}

		errs ++= collectErrors(descriptions, ValidationSection.Descriptions, List("descriptions"))

		// Funding validation
		fundingReferences.foreach { frs =>
			frs.zipWithIndex.foreach { case (funding, idx) =>
				errs ++= funding.errors.map(e => e.copy(
					section = ValidationSection.Funding,
					path = List("fundingReferences", idx.toString) ::: e.path
				))
			}
		}

		// Geolocation validation
		geoLocations.foreach { gls =>
			gls.zipWithIndex.foreach { case (geo, idx) =>
				errs ++= geo.errors.map(e => e.copy(
					section = ValidationSection.Geolocations,
					path = List("geoLocations", idx.toString) ::: e.path
				))
			}
		}

		// Related identifiers validation
		relatedIdentifiers.foreach { ris =>
			ris.zipWithIndex.foreach { case (rel, idx) =>
				errs ++= rel.errors.map(e => e.copy(
					section = ValidationSection.RelatedIdentifiers,
					path = List("relatedIdentifiers", idx.toString) ::: e.path
				))
			}
		}

		errs.result()
	}

	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmpty(creators, ValidationSection.Creators, "At least one creator is required", List("creators")),
		if (titles.isEmpty || titles.exists(_.isEmpty)) 
			Seq(mkError(ValidationSection.Titles, "At least one title is required", List("titles")))
		else 
			Seq.empty,
		publisher match {
			case None | Some("") => Seq(mkError(ValidationSection.Publisher, "Publisher is required", List("publisher")))
			case _ => Seq.empty
		},
		requireDefined(publicationYear, ValidationSection.PublicationYear, "Publication year is required", List("publicationYear")),
		requireDefined(types, ValidationSection.ResourceType, "Resource type is required", List("types")),
		types.toSeq.flatMap(t => t.errors.map(e => e.copy(section = ValidationSection.ResourceType, path = List("types") ::: e.path))),
		draftErrors
	)
}

case class DoiWrapper(attributes: DoiMeta)
case class SingleDoiPayload(data: DoiWrapper)
case class DoiListMeta(total: Int, totalPages: Int, page: Int)
case class DoiListPayload(data: Seq[DoiWrapper], meta: DoiListMeta)
