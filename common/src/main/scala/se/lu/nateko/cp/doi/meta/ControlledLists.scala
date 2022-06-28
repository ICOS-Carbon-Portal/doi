package se.lu.nateko.cp.doi.meta


enum ContributorType:
	case ContactPerson, DataCollector, DataCurator, DataManager, Distributor, Editor,
		HostingInstitution, Other, Producer, ProjectLeader, ProjectManager, ProjectMember,
		RegistrationAgency, RegistrationAuthority, RelatedPerson, ResearchGroup,
		RightsHolder, Researcher, Sponsor, Supervisor, WorkPackageLeader

enum DateType:
	case Accepted, Available, Copyrighted, Collected, Created, Issued, Submitted, Updated, Valid

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
