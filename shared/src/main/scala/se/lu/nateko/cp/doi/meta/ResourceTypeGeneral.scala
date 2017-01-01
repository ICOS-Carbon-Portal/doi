package se.lu.nateko.cp.doi.meta

object ResourceTypeGeneral extends Enumeration{
	type ResourceTypeGeneral = Value

	val Audiovisual = Value("Audiovisual")
	val Collection = Value("Collection")
	val Dataset = Value("Dataset")
	val Event = Value("Event")
	val Image = Value("Image")
	val InteractiveResource = Value("InteractiveResource")
	val Model = Value("Model")
	val PhysicalObject = Value("PhysicalObject")
	val Service = Value("Service")
	val Software = Value("Software")
	val Sound = Value("Sound")
	val Text = Value("Text")
	val Workflow = Value("Workflow")
	val Other = Value("Other")
}