package se.lu.nateko.cp.doi.meta

object TitleType extends Enumeration {
	type TitleType = Value

	val AlternativeTitle = Value("AlternativeTitle")
	val Subtitle = Value("Subtitle")
	val TranslatedTitle = Value("TranslatedTitle")
	val Other = Value("Other")
}
