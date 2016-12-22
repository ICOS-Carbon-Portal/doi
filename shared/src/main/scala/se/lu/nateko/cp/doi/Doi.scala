package se.lu.nateko.cp.doi

import se.lu.nateko.cp.doi.meta._

case class Doi(prefix: String, suffix: String) extends SelfValidating{
	override def toString = prefix + "/" + suffix

	def error: Option[String] = joinErrors(
		nonEmpty(prefix)("DOI prefix is required"),
		nonEmpty(suffix)("DOI suffix is required"),
		if(prefix.startsWith("10.")) None else Some("Prefix must start with \"10.\"")
	)
}

case class DoiMeta(
	id: Doi,
	creators: Seq[Creator],
	titles: Seq[Title],
	publisher: String,
	publicationYear: Int
) extends SelfValidating{

	def error = joinErrors(
		id.error,
		nonEmpty(creators)("At lease one creator is required"),
		nonEmpty(titles)("At lease one title is required"),
		nonEmpty(publisher)("Publisher is required"),
		if(publicationYear < 1000 || publicationYear > 3000) Some("Invalid publication year") else None
	)
}
