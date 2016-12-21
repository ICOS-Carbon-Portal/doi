package se.lu.nateko.cp.doi

import se.lu.nateko.cp.doi.meta._

case class Doi(prefix: String, suffix: String) extends SelfValidating{
	override def toString = prefix + "/" + suffix

	override def error = ???
}

case class DoiMeta(
	id: Doi,
	creators: Seq[Creator],
	titles: Seq[Title],
	publisher: String
) extends SelfValidating{

	def error = joinErrors(
		id.error,
		nonEmpty(creators)("At lease one creator is required"),
		nonEmpty(titles)("At lease one title is required"),
		nonEmpty(publisher)("Publisher is required")
	)
}
