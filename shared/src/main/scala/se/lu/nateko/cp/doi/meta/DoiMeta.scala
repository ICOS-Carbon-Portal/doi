package se.lu.nateko.cp.doi.meta

trait SelfValidating{
	def error: Option[String]

	protected def joinErrors(errors: Option[String]*): Option[String] = {
		val list = errors.flatten
		if(list.isEmpty) None else Some(list.mkString("\n"))
	}

	protected def nonEmpty(s: String)(msg: String): Option[String] =
		if(s == null || s.length == 0) Some(msg) else None

	protected def allGood(items: Seq[SelfValidating]): Option[String] = joinErrors(items.map(_.error): _*)

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
	def error = ???
}

case class NameIdentifierScheme(name: String, uri: Option[String])

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

case class Title(value: String, lang: String, titleType: Option[TitleType.Value]) extends SelfValidating{
	def error = ???
}

case class Contributor(
	name: GenericName,
	nameId: Option[NameIdentifier],
	affiliation: Seq[String],
	contributorType: ContributorType.Value
)
