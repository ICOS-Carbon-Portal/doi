package se.lu.nateko.cp.doi.meta

object DescriptionType extends Enumeration{
	type DescriptionType = Value

	val Abstract = Value("Abstract")
	val Methods = Value("Methods")
	val SeriesInformation = Value("SeriesInformation")
	val TableOfContents = Value("TableOfContents")
	val TechnicalInfo = Value("TechnicalInfo")
	val Other = Value("Other")
}
