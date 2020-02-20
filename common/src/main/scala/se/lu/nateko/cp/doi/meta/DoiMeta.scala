package se.lu.nateko.cp.doi.meta

import scala.collection.Seq
import scala.collection.immutable

trait SelfValidating{
	def error: Option[String]

	protected def joinErrors(errors: Seq[Option[String]]): Option[String] = {
		val list = errors.flatten
		if(list.isEmpty) None else Some(list.mkString("\n"))
	}
	protected def joinErrors(errors: Option[String]*): Option[String] = joinErrors(errors)

	protected def allGood(items: Seq[SelfValidating]): Option[String] = joinErrors(items.map(_.error))

	protected def nonNull(obj: AnyRef)(msg: String): Option[String] =
		if(obj == null) Some(msg) else None

	protected def nonEmpty(s: String)(msg: String): Option[String] =
		if(s == null || s.length == 0) Some(msg) else None

	protected def eachNonEmpty(ss: Seq[String])(msg: String): Option[String] =
		joinErrors(ss.map(s => nonEmpty(s)(msg)))

	protected def nonEmptyAllGood(items: Seq[SelfValidating])(msg: String): Option[String] =
		if(items.isEmpty) Some(msg) else allGood(items)

	//TODO Improve this naive URI syntax validation
	private val uriRegex = """^https?://.+$""".r
	protected def validUri(uri: String): Option[String] =
		if(uriRegex.findFirstIn(uri).isDefined) None else Some("Invalid URI: " + uri)

}

sealed trait Name extends SelfValidating

case class PersonalName(givenName: String, familyName: String) extends Name{
	def error = joinErrors(
		nonEmpty(givenName)("Given name is required"),
		nonEmpty(familyName)("Family name is required")
	)
	override def toString = familyName + ", " + givenName
}

case class GenericName(name: String) extends Name{
	def error = nonEmpty(name)("Name is required")
	override def toString = name
}

case class NameIdentifier(id: String, scheme: NameIdentifierScheme) extends SelfValidating{
	import NameIdentifierScheme._

	def error = joinErrors(
		nonEmpty(id)("Name identifier must not be empty"),
		nonNull(scheme)("Name Identifier scheme must be provided"),
		scheme.error,
		scheme match {
			case Orcid =>
				if(id.matches("""^(\d{4}\-?){3}\d{3}[0-9X]$""")) None
				else Some("Wrong ORCID ID format")

			case Isni =>
				if(id.matches("""^(\d{4} ?){3}\d{3}[0-9X]$""")) None
				else Some("Wrong ISNI ID format")

			case Fluxnet =>
				if(id.matches("""^[A-Z]{2}\-[A-Z][A-Za-z0-9]{2}$""")) None
				else Some("Wrong FLUXNET site id format")

			case _ if(supported.contains(scheme)) => None
			case _ =>
				val supportedNames = supported.mkString(", ")
				Some("Only the following name identifier schemes are supported: " + supportedNames)
		}
	)
}

object NameIdentifier{
	def orcid(id: String) = NameIdentifier(id, NameIdentifierScheme.Orcid)
	def isni(id: String) = NameIdentifier(id, NameIdentifierScheme.Isni)
}

case class NameIdentifierScheme(name: String, uri: Option[String]) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(name)("Name identifier scheme must have a name"),
		uri.flatMap(validUri)
	)
	override def toString = name
}

object NameIdentifierScheme{
	val Orcid = NameIdentifierScheme("ORCID", Some("http://orcid.org/"))
	val Isni = NameIdentifierScheme("ISNI", Some("http://www.isni.org/"))
	val Fluxnet = NameIdentifierScheme("FLUXNET", None)
	val supported = immutable.Seq(Orcid, Isni, Fluxnet)
}

sealed trait Person extends SelfValidating{
	val name: Name
	val nameIds: Seq[NameIdentifier]
	val affiliations: Seq[String]

	def error = joinErrors(
		name.error,
		allGood(nameIds),
		eachNonEmpty(affiliations)("Affiliation is not required but must not be empty if provided")
	)
}

case class Creator(name: Name, nameIds: Seq[NameIdentifier], affiliations: Seq[String]) extends Person

case class Contributor(
	name: Name,
	nameIds: Seq[NameIdentifier],
	affiliations: Seq[String],
	contributorType: ContributorType.Value
) extends Person{

	override def error = joinErrors(
		super.error,
		nonNull(contributorType)("Contributor type must be specified")
	)
}


case class Title(title: String, lang: Option[String], titleType: Option[TitleType.Value]) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(title)("Title must not be empty"),
		lang.flatMap(l => nonEmpty(l)("Title language is not required but must not be empty if provided"))
	)
}

case class ResourceType(resourceType: String, resourceTypeGeneral: ResourceTypeGeneral.Value) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(resourceType)("Specific resource type must not be empty"),
		nonNull(resourceTypeGeneral)("The general resource type must be specified")
	)
}

case class SubjectScheme(subjectScheme: String, schemeUri: Option[String]) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(subjectScheme)("Subject scheme must have a name"),
		schemeUri.flatMap(validUri)
	)
	override def toString = subjectScheme
}
object SubjectScheme{
	val Dewey = SubjectScheme("Dewey", Some("http://dewey.info/"))
}

case class Subject(
	val subject: String,
	val lang: Option[String] = None,
	val subjectScheme: Option[SubjectScheme] = None,
	val valueUri: Option[String] = None
) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(subject)("Subject must not be empty"),
		lang.flatMap(l => nonEmpty(l)("Subject language is not required but must not be empty if provided")),
		subjectScheme.flatMap(_.error),
		valueUri.flatMap(validUri)
	)
}

case class Date(date: String, dateType: DateType.Value) extends SelfValidating{
	private[this] val dateRegex = """(\d{4})-(\d\d)-(\d\d)""".r

	def error = joinErrors(
		nonEmpty(date)("Date must not be empty if specified"),
		nonNull(dateType)("Date type must be specified for every date"),
		if(date == null || date.isEmpty || !dateIsWrong(date)) None
		else Some(s"Wrong date '$date', use format YYYY-MM-DD")
	)

	private def dateIsWrong(date: String): Boolean = date match {
		case dateRegex(yearStr, monthStr, dayStr) =>
			val year = yearStr.toInt
			val month = monthStr.toInt
			val day = dayStr.toInt
			year < 1900 || year > 3000 || month < 1 || month > 12 || day < 1 || day > 31
		case _ => true
	}
}

case class Version(major: Int, minor: Int) extends SelfValidating{

	private def versionCorrect(v: Int, msg: String): Option[String] =
		if(v >= 0 && v < 100) None else Some(msg + " version must be between 0 and 99")

	def error = joinErrors(
		versionCorrect(major, "Major"),
		versionCorrect(minor, "Minor")
	)

	override def toString = s"$major.$minor"
}

case class Rights(rights: String, rightsUri: Option[String]) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(rights)("License name must be provided"),
		rightsUri.flatMap(validUri)
	)
}

case class Description(description: String, descriptionType: DescriptionType.Value, lang: Option[String]) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(description)("Description must not be empty (if supplied)"),
		lang.flatMap(l => nonEmpty(l)("Description language is not required but must not be empty if provided")),
		nonNull(descriptionType)("Description type must be specified")
	)
}
