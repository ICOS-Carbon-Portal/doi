package se.lu.nateko.cp.doi.meta

trait SelfValidating{
	def error: Option[String]

	protected def joinErrors(errors: Option[String]*): Option[String] = {
		val list = errors.flatten
		if(list.isEmpty) None else Some(list.mkString("\n"))
	}

	protected def allGood(items: Seq[SelfValidating]): Option[String] = joinErrors(items.map(_.error): _*)

	protected def nonNull(obj: AnyRef)(msg: String): Option[String] =
		if(obj == null) Some(msg) else None

	protected def nonEmpty(s: String)(msg: String): Option[String] =
		if(s == null || s.length == 0) Some(msg) else None

	protected def nonEmpty(items: Seq[SelfValidating])(msg: String): Option[String] =
		if(items.isEmpty) Some(msg) else allGood(items)

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
		scheme match {
			case Orcid =>
				if(id.matches("""^(\d{4}\-?){3}\d{3}[0-9X]$""")) None
				else Some("Wrong ORCID ID format")

			case Isni =>
				if(id.matches("""^(\d{4} ?){3}\d{3}[0-9X]$""")) None
				else Some("Wrong ISNI ID format")

			case _ if(supported.contains(scheme.name)) => None
			case _ =>
				val supportedNames = supported.mkString(", ")
				Some("Only the following name identifier schemes are supported: " + supportedNames)
		}
	)
}

case class NameIdentifierScheme(name: String, uri: Option[String])

object NameIdentifierScheme{
	val Orcid = NameIdentifierScheme("ORCID", Some("http://orcid.org"))
	val Isni = NameIdentifierScheme("ISNI", Some("http://www.isni.org"))
	val supported = Seq(Orcid, Isni).map(_.name)
}

sealed trait Person extends SelfValidating{
	val name: Name
	val nameIds: Seq[NameIdentifier]
	val affiliations: Seq[String]

	def error = joinErrors(
		name.error,
		allGood(nameIds),
		joinErrors(affiliations.map(aff => nonEmpty(aff)("Affiliation is not required but must not be empty if provided")): _*)
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

case class SubjectScheme(subjectScheme: String, schemeUri: Option[String])
object SubjectScheme{
	val Dewey = SubjectScheme("dewey", Some("http://dewey.info"))
}

case class Subject(
	val subject: String,
	val lang: Option[String] = None,
	val subjectScheme: Option[SubjectScheme] = None,
	val valueUri: Option[String] = None
) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(subject)("Subject must not be empty"),
		lang.flatMap(l => nonEmpty(l)("Subject language is not required but must not be empty if provided"))
		//TODO Add valueUri validation
	)
}

case class Date(date: String, dateType: DateType.Value)
