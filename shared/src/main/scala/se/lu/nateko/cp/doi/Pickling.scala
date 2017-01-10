package se.lu.nateko.cp.doi

import upickle.Js
import upickle.default.{Writer, Reader}
import se.lu.nateko.cp.doi.meta.ContributorType
import se.lu.nateko.cp.doi.meta.DateType
import se.lu.nateko.cp.doi.meta.DescriptionType
import se.lu.nateko.cp.doi.meta.ResourceTypeGeneral
import se.lu.nateko.cp.doi.meta.TitleType

object Pickling{

	private def enumWriter[T <: Enumeration](enum: T) = Writer[enum.Value]{
		case v => Js.Str(v.toString)
	}

	private def enumReader[T <: Enumeration](enum: T) = Reader[enum.Value]{
		case Js.Str(s) => enum.withName(s)
	}

	implicit val contrTypeWriter = enumWriter(ContributorType)
	implicit val contrTypeReader = enumReader(ContributorType)

	implicit val dateTypeWriter = enumWriter(DateType)
	implicit val dateTypeReader = enumReader(DateType)

	implicit val descriptionTypeWriter = enumWriter(DescriptionType)
	implicit val descriptionTypeReader = enumReader(DescriptionType)

	implicit val resourceTypeGeneralWriter = enumWriter(ResourceTypeGeneral)
	implicit val resourceTypeGeneralReader = enumReader(ResourceTypeGeneral)

	implicit val titleTypeWriter = enumWriter(TitleType)
	implicit val titleTypeReader = enumReader(TitleType)
}
