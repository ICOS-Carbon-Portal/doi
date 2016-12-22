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
	import NameIdentifierScheme.{Orcid, Isni}

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

			case _ => Some("Only ORCID and ISNI name identifier schemes are supported")
		}
	)
}

case class NameIdentifierScheme(name: String, uri: Option[String])

object NameIdentifierScheme{
	val Orcid = NameIdentifierScheme("ORCID", Some("http://orcid.org"))
	val Isni = NameIdentifierScheme("ISNI", Some("http://www.isni.org"))
}

case class Creator(
	name: Name,
	nameId: Option[NameIdentifier],
	affiliations: Seq[String]
) extends SelfValidating{

	def error = joinErrors(
		name.error,
		nameId.flatMap(_.error),
		joinErrors(affiliations.map(aff => nonEmpty(aff)("Affiliation is not required but must not be empty if provided")): _*)
	)
}

case class Title(value: String, lang: Option[String], titleType: Option[TitleType.Value]) extends SelfValidating{
	def error = joinErrors(
		nonEmpty(value)("Title must not be empty"),
		lang.flatMap(l => nonEmpty(l)("Title languate is not required but must not be empty if provided"))
		//TODO Add lang value validation (must have the form 'en:us')
	)
}

case class Contributor(
	name: GenericName,
	nameId: Option[NameIdentifier],
	affiliation: Seq[String],
	contributorType: ContributorType.Value
)
