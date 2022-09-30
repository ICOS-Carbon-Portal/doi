package se.lu.nateko.cp.doi.meta


enum ContributorType:
	case ContactPerson, DataCollector, DataCurator, DataManager, Distributor, Editor,
		HostingInstitution, Other, Producer, ProjectLeader, ProjectManager, ProjectMember,
		RegistrationAgency, RegistrationAuthority, RelatedPerson, ResearchGroup,
		RightsHolder, Researcher, Sponsor, Supervisor, WorkPackageLeader

enum DateType(val couldBeRange: Boolean):
	case Accepted extends DateType(false)
	case Available extends DateType(true)
	case Copyrighted extends DateType(false)
	case Collected extends DateType(true)
	case Created extends DateType(true)
	case Issued extends DateType(false)
	case Submitted extends DateType(false)
	case Updated extends DateType(true)
	case Valid extends DateType(true)
	case Other extends DateType(false)

enum DescriptionType:
	case Abstract, Methods, SeriesInformation, TableOfContents, TechnicalInfo, Other

enum DoiPublicationState:
	case draft, registered, findable

enum DoiPublicationEvent:
	case publish, register, hide

enum TitleType:
	case AlternativeTitle, Subtitle, TranslatedTitle, Other

enum ResourceTypeGeneral:
	case Audiovisual, Collection, Dataset, Event, Image, InteractiveResource, Model, PhysicalObject,
		Service, Software, Sound, Text, Workflow, Other
