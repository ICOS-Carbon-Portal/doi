package se.lu.nateko.cp.doi.meta

object ContributorType extends Enumeration{
	type ContributorType = Value

	val ContactPerson = Value("ContactPerson")
	val DataCollector = Value("DataCollector")
	val DataCurator = Value("DataCurator")
	val DataManager = Value("DataManager")
	val Distributor = Value("Distributor")
	val Editor = Value("Editor")
	val Funder = Value("Funder")
	val HostingInstitution = Value("HostingInstitution")
	val Other = Value("Other")
	val Producer = Value("Producer")
	val ProjectLeader = Value("ProjectLeader")
	val ProjectManager = Value("ProjectManager")
	val ProjectMember = Value("ProjectMember")
	val RegistrationAgency = Value("RegistrationAgency")
	val RegistrationAuthority = Value("RegistrationAuthority")
	val RelatedPerson = Value("RelatedPerson")
	val ResearchGroup = Value("ResearchGroup")
	val RightsHolder = Value("RightsHolder")
	val Researcher = Value("Researcher")
	val Sponsor = Value("Sponsor")
	val Supervisor = Value("Supervisor")
	val WorkPackageLeader = Value("WorkPackageLeader")
}
