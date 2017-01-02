package se.lu.nateko.cp.doi.meta

object DateType extends Enumeration {
	type DateType = Value

	val Accepted = Value("Accepted")
	val Available = Value("Available")
	val Copyrighted = Value("Copyrighted")
	val Collected = Value("Collected")
	val Created = Value("Created")
	val Issued = Value("Issued")
	val Submitted = Value("Submitted")
	val Updated = Value("Updated")
	val Valid = Value("Valid")
}
