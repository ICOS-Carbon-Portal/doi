package se.lu.nateko.cp.doi.meta

object DoiPublicationState extends Enumeration {
	type DoiPublicationState = Value
	val draft = Value("draft")
	val registered = Value("registered")
	val findable = Value("findable")
}

object DoiPublicationEvent extends Enumeration {
	type DoiPublicationEvent = Value
	val publish = Value("publish")
	val register = Value("register")
	val hide = Value("hide")
}
