package se.lu.nateko.cp.doi.meta

import scala.collection.Seq
import scala.collection.immutable
import scala.util.Success
import scala.util.Try
import scala.util.Failure
import java.net.URI
import scala.util.matching.Regex

enum ValidationSection(val id: String, val label: String) {
	case DoiTarget extends ValidationSection("toc-doi-target", "DOI target")
	case Creators extends ValidationSection("toc-creators", "Creators")
	case Titles extends ValidationSection("toc-titles", "Titles")
	case Publisher extends ValidationSection("toc-publisher", "Publisher")
	case PublicationYear extends ValidationSection("toc-publication-year", "Publication year")
	case ResourceType extends ValidationSection("toc-resource-type", "Resource type")
	case Subjects extends ValidationSection("toc-subjects", "Subjects")
	case Contributors extends ValidationSection("toc-contributors", "Contributors")
	case Dates extends ValidationSection("toc-dates", "Dates")
	case RelatedIdentifiers extends ValidationSection("toc-related-identifiers", "Related identifiers")
	case Rights extends ValidationSection("toc-rights", "Rights")
	case Descriptions extends ValidationSection("toc-descriptions", "Descriptions")
	case Geolocations extends ValidationSection("toc-geolocations", "Geolocations")
	case Formats extends ValidationSection("toc-formats", "Formats")
	case Version extends ValidationSection("toc-version", "Version")
	case Funding extends ValidationSection("toc-funding", "Funding references")
}

case class ValidationError(
	section: ValidationSection,
	message: String,
	path: List[String] = Nil
)

trait SelfValidating{
	def errors: Seq[ValidationError]

	// Convenience methods
	def isValid: Boolean = errors.isEmpty

	def errorMessage: Option[String] =
		if (errors.isEmpty) None
		else Some(errors.map(_.message).mkString("\n"))

	// Helper: Create a single error
	protected def mkError(
		section: ValidationSection,
		message: String,
		path: List[String] = Nil
	): ValidationError =
		ValidationError(section, message, path)

	// Helper: Combine multiple error sequences
	protected def combineErrors(errorSeqs: Seq[ValidationError]*): Seq[ValidationError] =
		errorSeqs.flatten

	// Helper: Collect errors from nested SelfValidating items with path tracking
	protected def collectErrors(
		items: Iterable[SelfValidating],
		section: ValidationSection,
		pathPrefix: List[String] = Nil
	): Seq[ValidationError] =
		items.zipWithIndex.flatMap { case (item, idx) =>
			item.errors.map(e => e.copy(
				section = section,
				path = (pathPrefix :+ idx.toString) ::: e.path
			))
		}.toSeq

	// Validation: require non-empty collection
	protected def requireNonEmpty[T](
		seq: Iterable[T],
		section: ValidationSection,
		message: String,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (seq == null || seq.isEmpty)
			Seq(mkError(section, message, path))
		else
			Seq.empty

	// Validation: require non-null object
	protected def requireNonNull(
		obj: AnyRef,
		section: ValidationSection,
		message: String,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (obj == null)
			Seq(mkError(section, message, path))
		else
			Seq.empty

	// Validation: require non-empty string
	protected def requireNonEmptyString(
		str: String,
		section: ValidationSection,
		message: String,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (str == null || str.isEmpty)
			Seq(mkError(section, message, path))
		else
			Seq.empty

	// Validation: require Option to be defined (Some)
	protected def requireDefined[T](
		opt: Option[T],
		section: ValidationSection,
		message: String,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (opt == null || opt.isEmpty)
			Seq(mkError(section, message, path))
		else
			Seq.empty

	// Validation: require each string in collection to be non-empty
	protected def requireEachNonEmpty(
		strings: Seq[String],
		section: ValidationSection,
		message: String,
		pathPrefix: List[String] = Nil
	): Seq[ValidationError] =
		strings.zipWithIndex.flatMap { case (s, idx) =>
			if (s == null || s.isEmpty)
				Seq(mkError(section, message, pathPrefix :+ idx.toString))
			else
				Seq.empty
		}

	// URI validation
	protected def validateUri(
		uri: String,
		section: ValidationSection,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (SelfValidating.uriRegex.findFirstIn(uri).isDefined)
			Seq.empty
		else
			Seq(mkError(section, s"Invalid URI: $uri", path))

	// DOI validation
	protected def validateDoi(
		doi: String,
		section: ValidationSection,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (SelfValidating.doiRegex.findFirstIn(doi).isDefined)
			Seq.empty
		else
			Seq(mkError(section, s"Invalid DOI: $doi, valid format: 00.00000/ABC0-ABC0", path))

	// PID validation
	protected def validatePid(
		pid: String,
		section: ValidationSection,
		path: List[String] = Nil
	): Seq[ValidationError] =
		if (SelfValidating.pidRegex.findFirstIn(pid).isDefined)
			Seq.empty
		else
			Seq(mkError(section, s"Invalid PID: $pid, only letters, numbers, - and _ are allowed. Must be separated by a '/'", path))
}

object SelfValidating{
	private val uriRegex = """^https?://.+$""".r
	private val doiRegex = """^\d{2}\.[0-9]+/[A-Za-z0-9-_]+$""".r
	private val pidRegex = """^[A-Za-z0-9-_]+/[A-Za-z0-9-_]+$""".r
}

sealed trait Name extends SelfValidating

case class PersonalName(givenName: String, familyName: String) extends Name{
	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmptyString(givenName, ValidationSection.Creators, "Given name is required", List("givenName")),
		requireNonEmptyString(familyName, ValidationSection.Creators, "Family name is required", List("familyName"))
	)
	override def toString = givenName + " " + familyName
}

case class GenericName(name: String) extends Name{
	def errors: Seq[ValidationError] =
		requireNonEmptyString(name, ValidationSection.Creators, "Name is required", List("name"))
	override def toString = name
}

case class NameIdentifier(nameIdentifier: String, scheme: NameIdentifierScheme) extends SelfValidating{
	import NameIdentifierScheme.*

	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		errs ++= requireNonEmptyString(nameIdentifier, ValidationSection.Creators, "Name identifier must not be empty", List("nameIdentifier"))
		errs ++= requireNonNull(scheme, ValidationSection.Creators, "Name Identifier scheme must be provided", List("scheme"))

		if (scheme != null) {
			errs ++= scheme.errors.map(e => e.copy(path = "scheme" :: e.path))

			lookupRegex(scheme) match {
				case Some(rex) =>
					if (!rex.matches(nameIdentifier))
						errs += mkError(ValidationSection.Creators, s"Wrong $scheme ID format", List("nameIdentifier"))
				case None if (!values.contains(scheme)) =>
					val supportedNames = values.mkString(", ")
					errs += mkError(ValidationSection.Creators, "Only the following name identifier schemes are supported: " + supportedNames, List("scheme"))
				case _ => // valid
			}
		}

		errs.result()
	}
}

object NameIdentifier{
	def orcid(id: String) = NameIdentifier(id, NameIdentifierScheme.ORCID)
	def isni(id: String) = NameIdentifier(id, NameIdentifierScheme.ISNI)
}

case class NameIdentifierScheme(nameIdentifierScheme: String, schemeUri: Option[String]) extends SelfValidating{
	def errors: Seq[ValidationError] =
		schemeUri.toSeq.flatMap(uri => validateUri(uri, ValidationSection.Creators, List("schemeUri")))

	override def toString = nameIdentifierScheme
}

object NameIdentifierScheme{
	val ORCID               = NameIdentifierScheme("ORCID", Some("http://orcid.org/"))
	val ISNI                = NameIdentifierScheme("ISNI", Some("http://www.isni.org/"))
	val ROR                 = NameIdentifierScheme("ROR", Some("https://ror.org"))
	val FLUXNET             = NameIdentifierScheme("FLUXNET", None)

	def values = Regexes.keys.toSeq

	def lookup(nameIdentifierScheme: String): Option[NameIdentifierScheme] =
		Regexes.keys.find(_.nameIdentifierScheme == nameIdentifierScheme)

	def lookupRegex(nameIdentifierScheme: NameIdentifierScheme): Option[Regex] =
		Regexes.get(nameIdentifierScheme)

	private val Regexes = Map(
		ORCID -> """^(https://orcid.org/)?(\d{4}\-?){3}\d{3}[0-9X]$""".r,
		ISNI -> """^(\d{4} ?){3}\d{3}[0-9X]$""".r,
		ROR -> "^[a-z0-9]{9}$".r,
		FLUXNET -> """^[A-Z]{2}\-[A-Z][A-Za-z0-9]{2}$""".r
	)
}

case class FunderIdentifier(funderIdentifier: Option[String], scheme: Option[FunderIdentifierScheme]) extends SelfValidating {
	import FunderIdentifierScheme._

	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		funderIdentifier match {
			case Some(fi) if !fi.isEmpty && scheme.isEmpty =>
				errs += mkError(ValidationSection.Funding, "Funder Identifier scheme must be provided", List("scheme"))
			case _ => // OK
		}

		scheme.foreach { sch =>
			val validator = lookupValidator(sch.funderIdentifierType)

			validator match {
				case None =>
					errs += mkError(ValidationSection.Funding, "Only the following funder identifier schemes are supported: " + supported.mkString(", "), List("scheme"))
				case Some(fIdVal) =>
					fIdVal.produceErrorMessage(funderIdentifier).foreach { msg =>
						errs += mkError(ValidationSection.Funding, msg, List("funderIdentifier"))
					}
			}
		}

		errs.result()
	}
}

object FunderIdentifier{
	def default = FunderIdentifier(Some(""), None)
}

case class FunderIdentifierScheme(funderIdentifierType: String, SchemeURI: Option[String]) {
	override def toString = funderIdentifierType
}

object FunderIdentifierScheme{
	val Crossref = FunderIdentifierScheme("Crossref Funder ID", Some("https://www.crossref.org/services/funder-registry/"))
	val Grid = FunderIdentifierScheme("GRID", Some("https://www.grid.ac/"))
	val Isni = FunderIdentifierScheme("ISNI", Some("http://www.isni.org/"))
	val Ror = FunderIdentifierScheme("ROR", Some("https://ror.org/"))
	val Other = FunderIdentifierScheme("Other", None)

	private val Validators = Map(
		Isni -> FunderIdentifierValidator(
			Isni,
			"""^((http|https):\/\/(www.)?isni.org\/(isni\/)?)?(\d{4} ?){3}\d{3}[0-9X]$""".r,
			"http://www.isni.org/isni/000000012146438X or https://isni.org/isni/000000012146438X or https://isni.org/000000012146438X or 000000012146438X"
			),
		Ror -> FunderIdentifierValidator(
			Ror,
			"^(https://ror.org/)?[a-z0-9]{9}$".r,
			"https://ror.org/03yrm5c26 or 03yrm5c26"
			),
		Crossref -> FunderIdentifierValidator(
			Crossref,
			"""^(http://(dx.)?doi.org/)?10.13039/\d{9,12}$""".r,
			"10.13039/100013829 or http://dx.doi.org/10.13039/100013829"
			),
		Grid -> FunderIdentifierValidator(
			Grid,
			"""grid.\d{4,6}.[0-9a-f]{1,2}""".r,
			"grid.238252"
			),
		Other -> FunderIdentifierValidator(
			Other,
			"^(.+)$".r
			)
	)

	def supported = Validators.keys.toSeq

	def lookup(funderIdentifierType: String): Option[FunderIdentifierScheme] =
		Validators.keys.find(_.funderIdentifierType == funderIdentifierType)

	def lookupValidator(funderIdentifierType: String): Option[FunderIdentifierValidator] = {
		lookup(funderIdentifierType).flatMap(t => Validators.get(t))
	}
}


case class FunderIdentifierValidator(scheme: FunderIdentifierScheme, regex: Regex, expectedFormat: String = ""){

	def produceErrorMessage(funderIdentifier: Option[String]): Option[String] = {
		funderIdentifier.fold(Some("Empty funder identifier")){fid =>
			if(regex.matches(fid)) None
			else if(fid.isEmpty) Some("Empty funder identifier")
			else Some(s"Wrong $scheme ID format, examples of accepted IDs: ${expectedFormat}")
		}
	}
}

case class Affiliation(name: String)

// TODO: change to Agent
sealed trait Person extends SelfValidating{
	val name: Name
	val nameIdentifiers: Seq[NameIdentifier]
	val affiliation: Seq[Affiliation]

	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		errs ++= name.errors.map(e => e.copy(path = "name" :: e.path))

		errs ++= collectErrors(nameIdentifiers, ValidationSection.Creators, List("nameIdentifiers"))

		errs ++= requireEachNonEmpty(
			affiliation.map(_.name),
			ValidationSection.Creators,
			"Affiliation is not required but must not be empty if provided",
			List("affiliation")
		)

		errs.result()
	}
}

case class Creator(name: Name, nameIdentifiers: Seq[NameIdentifier], affiliation: Seq[Affiliation]) extends Person

case class Award(awardNumber: Option[String], awardTitle: Option[String], awardUri: Option[String]) extends SelfValidating{
	def errors: Seq[ValidationError] = {
		awardUri match {
			case Some(aUri) =>
				Try(new URI(aUri)) match {
					case Failure(_) =>
						Seq(mkError(ValidationSection.Funding, s"Invalid funder award URI: $aUri", List("awardUri")))
					case Success(_) =>
						Seq.empty
				}
			case None =>
				Seq.empty
		}
	}
}

object Award{
	def default = Award(Some(""), None, None)
}

case class FundingReference(
	funderName: Option[String], funderIdentifier: Option[FunderIdentifier], award: Option[Award]
) extends SelfValidating {

	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		funderName match {
			case None | Some("") =>
				errs += mkError(ValidationSection.Funding, "Funder must have a name", List("funderName"))
			case _ => // OK
		}

		award.foreach { a =>
			errs ++= a.errors.map(e => e.copy(path = "award" :: e.path))
		}

		funderIdentifier.foreach { fi =>
			errs ++= fi.errors.map(e => e.copy(path = "funderIdentifier" :: e.path))
		}

		errs.result()
	}
}
case class Contributor(
	name: Name,
	nameIdentifiers: Seq[NameIdentifier],
	affiliation: Seq[Affiliation],
	contributorType: Option[ContributorType]
) extends Person{

	override def errors: Seq[ValidationError] = combineErrors(
		super.errors,
		requireDefined(contributorType, ValidationSection.Contributors, "Contributor type must be specified", List("contributorType"))
	)
}


case class Title(title: String, lang: Option[String], titleType: Option[TitleType]) extends SelfValidating{
	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmptyString(title, ValidationSection.Titles, "Title must not be empty", List("title")),
		lang.toSeq.flatMap(l => requireNonEmptyString(l, ValidationSection.Titles, "Title language is not required but must not be empty if provided", List("lang")))
	)
}

case class ResourceType(resourceType: Option[String], resourceTypeGeneral: Option[ResourceTypeGeneral]) extends SelfValidating{
	def errors: Seq[ValidationError] = combineErrors(
		resourceType match {
			case None | Some("") => Seq(mkError(ValidationSection.ResourceType, "Specific resource type must not be empty", List("resourceType")))
			case _ => Seq.empty
		},
		requireDefined(resourceTypeGeneral, ValidationSection.ResourceType, "The general resource type must be specified", List("resourceTypeGeneral"))
	)
}

case class Subject(
	val subject: String,
	val lang: Option[String] = None,
	val valueUri: Option[String] = None
) extends SelfValidating{
	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmptyString(subject, ValidationSection.Subjects, "Subject must not be empty", List("subject")),
		lang.toSeq.flatMap(l => requireNonEmptyString(l, ValidationSection.Subjects, "Subject language is not required but must not be empty if provided", List("lang"))),
		valueUri.toSeq.flatMap(uri => validateUri(uri, ValidationSection.Subjects, List("valueUri")))
	)
}

case class Date(date: String, dateType: Option[DateType]) extends SelfValidating{
	import Date._

	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		errs ++= requireNonEmptyString(date, ValidationSection.Dates, "Date must not be empty if specified", List("date"))

		errs ++= requireNonNull(dateType, ValidationSection.Dates, "Date type must be specified for every date", List("dateType"))

		if (date != null && !date.isEmpty && dateIsWrong(date))
			errs += mkError(ValidationSection.Dates, s"Wrong date '$date', use format YYYY[-MM-DD] or YYYY-MM-DD/YYYY-MM-DD", List("date"))
		else if (date != null && !date.isEmpty && !dateIsWrong(date))
			rangeAllowedCheck.foreach { msg =>
				errs += mkError(ValidationSection.Dates, msg, List("date"))
			}

		errs.result()
	}

	private def rangeAllowedCheck: Option[String] = date match
		case dateRangeRegex(_, _) => dateType.collect{
			case dt if(!dt.couldBeRange) =>  s"Date of type $dt cannot be a date range"
		}
		case _ => None

	private def dateIsWrong(date: String): Boolean = date match {
		case dateRegex(yearStr, monthStr, dayStr) =>
			val month = monthStr.toInt
			val day = dayStr.toInt
			yearIsWrong(yearStr) || month < 1 || month > 12 || day < 1 || day > 31
		case dateRangeRegex(startDate, endDate) => dateIsWrong(startDate) || dateIsWrong(endDate)
		case yearRegex(yearStr) => yearIsWrong(yearStr)
		case _ => true
	}

	private def yearIsWrong(yearStr: String): Boolean =
		try{
			val year = yearStr.toInt
			year < 1900 || year > 3000
		}catch{
			case _: Throwable => true
		}

	override def toString = dateType.fold(s"${date}")(dt => s"$dt: ${date}")
}

object Date{
	private val dateRegex = """(\d{4})-(\d\d)-(\d\d)""".r
	private val yearRegex = "(\\d{4})".r
	private val dateRangeRegex = """(\d{4}-\d\d-\d\d)/(\d{4}-\d\d-\d\d)""".r
}

case class Version(major: Int, minor: Int) extends SelfValidating{

	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		if (major < 0 || major >= 100)
			errs += mkError(ValidationSection.Version, "Major version must be between 0 and 99", List("major"))

		if (minor < 0 || minor >= 100)
			errs += mkError(ValidationSection.Version, "Minor version must be between 0 and 99", List("minor"))

		errs.result()
	}

	override def toString = s"$major.$minor"
}

object Version{
	val VersionRegex = """^(\d+).(\d+)$""".r
	def parse(s: String): Try[Version] = s match{
		case VersionRegex(major, minor) => Success(Version(major.toInt, minor.toInt))
		case _ => Failure(new IllegalArgumentException(s"Invalid version string: $s"))
	}
}

case class Rights(
	rights: String,
	rightsUri: Option[String],
	rightsIdentifier: Option[String],
	schemeUri: Option[String] = Some("https://spdx.org/licenses"),
	rightsIdentifierScheme: Option[String] = Some("SPDX"),
	lang: Option[String] = Some("en")
) extends SelfValidating {
	def errors: Seq[ValidationError] = combineErrors(
		rightsIdentifier match {
			case None | Some("") => Seq(mkError(ValidationSection.Rights, "Rights identifier must be provided", List("rightsIdentifier")))
			case _ => Seq.empty
		},
		requireNonEmptyString(rights, ValidationSection.Rights, "License name must be provided", List("rights")),
		rightsUri.toSeq.flatMap(uri => validateUri(uri, ValidationSection.Rights, List("rightsUri")))
	)
}

enum RelationType {
	case HasMetadata, IsMetadataFor, IsCitedBy, Cites, IsSupplementTo, IsSupplementedBy, IsContinuedBy, Continues,
	IsDescribedBy, Describes, HasVersion, IsVersionOf, IsNewVersionOf, IsPreviousVersionOf, IsPartOf, HasPart,
	IsPublishedIn, IsReferencedBy, References, IsDocumentedBy, Documents, IsCompiledBy, Compiles, IsVariantFormOf,
	IsOriginalFormOf, IsIdenticalTo, IsReviewedBy, Reviews, IsDerivedFrom, IsSourceOf, IsRequiredBy, Requires,
	IsObsoletedBy, Obsoletes, IsCollectedBy, Collects, IsTranslationOf, HasTranslation
}

enum RelatedIdentifierType {
	case DOI, Handle, URL
}

final case class RelatedIdentifier (
	relationType: Option[RelationType],
	relatedIdentifierType: Option[RelatedIdentifierType],
	relatedIdentifier: String,
	resourceTypeGeneral: Option[ResourceTypeGeneral],

	// Only valid for HasMetadata and IsMetadataFor
	relatedMetadataScheme: Option[String],
	schemeUri: Option[String],
	schemeType: Option[String]
) extends SelfValidating {
	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmptyString(relatedIdentifier, ValidationSection.RelatedIdentifiers, "Related identifier must not be empty", List("relatedIdentifier")),
		requireNonNull(relatedIdentifierType, ValidationSection.RelatedIdentifiers, "Please select a related identifier type", List("relatedIdentifierType")),
		relatedIdentifierType.toSeq.flatMap(idType => validateIdentifierWithType(relatedIdentifier, idType)),
		requireNonNull(relationType, ValidationSection.RelatedIdentifiers, "Please provide a relation type", List("relationType"))
	)

	private def validateIdentifierWithType(id: String, idType: RelatedIdentifierType): Seq[ValidationError] = idType match {
		case RelatedIdentifierType.DOI => validateDoi(id, ValidationSection.RelatedIdentifiers, List("relatedIdentifier"))
		case RelatedIdentifierType.Handle => validatePid(id, ValidationSection.RelatedIdentifiers, List("relatedIdentifier"))
		case RelatedIdentifierType.URL => validateUri(id, ValidationSection.RelatedIdentifiers, List("relatedIdentifier"))
		case null => Seq.empty
	}
}

case class Description(description: String, descriptionType: DescriptionType, lang: Option[String]) extends SelfValidating{
	def errors: Seq[ValidationError] = combineErrors(
		requireNonEmptyString(description, ValidationSection.Descriptions, "Description must not be empty (if supplied)", List("description")),
		lang.toSeq.flatMap(l => requireNonEmptyString(l, ValidationSection.Descriptions, "Description language is not required but must not be empty if provided", List("lang"))),
		requireNonNull(descriptionType, ValidationSection.Descriptions, "Description type must be specified", List("descriptionType"))
	)
}

opaque type Latitude <: Double = Double
opaque type Longitude <: Double = Double

object Latitude:
	def apply(value: Double): Latitude = value
object Longitude:
	def apply(value: Double): Longitude = value

extension (l: Longitude)
	def lonError: Option[String] = if(l < -180 || l > 180) Some(s"Longitude must be between -180 and 180") else None

extension (l: Latitude)
	def latError: Option[String] = if(l < -90 || l > 90) Some(s"Latitude must be between -90 and 90") else None

case class GeoLocationPoint(pointLongitude: Option[Longitude], pointLatitude: Option[Latitude]) extends SelfValidating{
	def errors: Seq[ValidationError] = combineErrors(
		requireDefined(pointLongitude, ValidationSection.Geolocations, "Point longitude must be specified for every geolocation point", List("pointLongitude")),
		pointLongitude.toSeq.flatMap(lon => lon.lonError.map(msg => mkError(ValidationSection.Geolocations, msg, List("pointLongitude")))),
		requireDefined(pointLatitude, ValidationSection.Geolocations, "Point latitude must be specified for every geolocation point", List("pointLatitude")),
		pointLatitude.toSeq.flatMap(lat => lat.latError.map(msg => mkError(ValidationSection.Geolocations, msg, List("pointLatitude"))))
	)
}

case class GeoLocationBox(westBoundLongitude: Option[Longitude], eastBoundLongitude: Option[Longitude], southBoundLatitude: Option[Latitude], northBoundLatitude: Option[Latitude]) extends SelfValidating{
	def errors: Seq[ValidationError] = combineErrors(
		requireDefined(westBoundLongitude, ValidationSection.Geolocations, "West bound longitude must be specified for every geolocation box", List("westBoundLongitude")),
		westBoundLongitude.toSeq.flatMap(lon => lon.lonError.map(msg => mkError(ValidationSection.Geolocations, msg, List("westBoundLongitude")))),
		requireDefined(eastBoundLongitude, ValidationSection.Geolocations, "East bound latitude must be specified for every geolocation box", List("eastBoundLongitude")),
		eastBoundLongitude.toSeq.flatMap(lon => lon.lonError.map(msg => mkError(ValidationSection.Geolocations, msg, List("eastBoundLongitude")))),
		requireDefined(southBoundLatitude, ValidationSection.Geolocations, "South bound latitude must be specified for every geolocation box", List("southBoundLatitude")),
		southBoundLatitude.toSeq.flatMap(lat => lat.latError.map(msg => mkError(ValidationSection.Geolocations, msg, List("southBoundLatitude")))),
		requireDefined(northBoundLatitude, ValidationSection.Geolocations, "North bound latitude must be specified for every geolocation box", List("northBoundLatitude")),
		northBoundLatitude.toSeq.flatMap(lat => lat.latError.map(msg => mkError(ValidationSection.Geolocations, msg, List("northBoundLatitude"))))
	)
}

case class GeoLocation(geoLocationPoint: Option[GeoLocationPoint], geoLocationBox: Option[GeoLocationBox], geoLocationPlace: Option[String]) extends SelfValidating{
	def errors: Seq[ValidationError] = {
		val errs = Seq.newBuilder[ValidationError]

		geoLocationPoint.foreach { point =>
			errs ++= point.errors.map(e => e.copy(path = "geoLocationPoint" :: e.path))
		}

		geoLocationBox.foreach { box =>
			errs ++= box.errors.map(e => e.copy(path = "geoLocationBox" :: e.path))
		}

		errs.result()
	}
}
