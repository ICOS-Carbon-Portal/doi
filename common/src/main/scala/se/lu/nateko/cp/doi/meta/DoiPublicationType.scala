package se.lu.nateko.cp.doi.meta

object DoiPublicationState extends Enumeration {
	type DoiPublicationState = Value
	val draft, registered, findable = Value
}

object DoiPublicationEvent extends Enumeration {
	type DoiPublicationEvent = Value
	val publish, register, hide = Value
}
