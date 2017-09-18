lazy val commonSettings = Seq(
	organization := "se.lu.nateko.cp",
	scalaVersion := "2.12.3"
)

lazy val commonJvmSettings = Seq(
	scalacOptions ++= Seq(
		"-unchecked",
		"-deprecation",
		"-Xlint",
		"-Ywarn-dead-code",
		"-language:_",
		"-target:jvm-1.8",
		"-encoding", "UTF-8"
	)
) ++ commonSettings

val common = crossProject
	.crossType(CrossType.Pure)
	.in(file("common"))
	.settings(
		name := "doi-common",
		version := "0.1.1-SNAPSHOT",
		libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
	)
	.jsSettings(commonSettings: _*)
	.jvmSettings(commonJvmSettings: _*)
	.jsSettings(name := "doi-common-js")
	.jvmSettings(name := "doi-common-jvm")

lazy val commonJs = common.js
lazy val commonJvm = common.jvm

lazy val core = project
	.in(file("core"))
	.dependsOn(commonJvm)
	.settings(commonJvmSettings: _*)
	.enablePlugins(SbtTwirl)
	.settings(
		name := "doi-core",
		version := "0.1.1-SNAPSHOT",
		libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
		publishTo := {
			val nexus = "https://repo.icos-cp.eu/content/repositories/"
			if (isSnapshot.value)
				Some("snapshots" at nexus + "snapshots")
			else
				Some("releases"  at nexus + "releases")
		},
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
	)

lazy val deploy = inputKey[Unit]("Deploys to production using Ansible (depends on 'infrastructure' project)")

lazy val app = crossProject
	.in(file("."))
	.settings(
		name := "doi",
		libraryDependencies ++= Seq(
			"com.lihaoyi" %%% "upickle" % "0.4.4"
		)
	)
	.jsSettings(commonSettings: _*)
	.jvmSettings(commonJvmSettings: _*)
	.jsSettings(
		name := "doi-js",
		libraryDependencies ++= Seq(
			"com.lihaoyi" %%% "scalatags" % "0.6.2"
		)
	)
	.jvmSettings(
		name := "doi-jvm",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http"   % "10.0.4",
			"se.lu.nateko.cp"   %% "views-core"  % "0.3.1-SNAPSHOT",
			"se.lu.nateko.cp"   %% "cpauth-core" % "0.5-SNAPSHOT"
		),
		baseDirectory in reStart := {
			baseDirectory.in(reStart).value.getParentFile
		},
		assemblyMergeStrategy.in(assembly) := {
			case PathList(name) if(name.endsWith("-fastopt.js")) =>
				MergeStrategy.discard
			case x =>
				val originalStrategy = assemblyMergeStrategy.in(assembly).value
				originalStrategy(x)
		},
		deploy := {
			val gitStatus = sbt.Process("git status -s").lines.mkString("").trim
			if(!gitStatus.isEmpty) sys.error("Please commit before deploying!")

			val log = streams.value.log
			val args: Seq[String] = sbt.Def.spaceDelimited().parsed

			val check = args.toList match{
				case "to" :: "production" :: Nil =>
					log.info("Performing a REAL deployment to production")
					""
				case _ =>
					log.warn("Performing a TEST deployment, use 'deploy to production' for a real one")
					"--check"
			}
			val jarPath = assembly.value.getCanonicalPath
			val confPath = new java.io.File("./application.conf").getCanonicalPath
			val ymlPath = new java.io.File("../infrastructure/devops/doi/setup_doi.yml").getCanonicalPath
			sbt.Process(s"""ansible-playbook $check -i fsicos.lunarc.lu.se, $ymlPath """ +
				s"""--ask-sudo -e doi_app_conf=$confPath -e doi_jar_file=$jarPath""").run(true).exitValue()
		}
	)
	.jsConfigure(_.dependsOn(commonJs))
	.jvmConfigure(_.dependsOn(core).enablePlugins(SbtTwirl))

lazy val appJs = app.js
lazy val appJvm = app.jvm
	.enablePlugins(BuildInfoPlugin)
	.settings(
		buildInfoKeys := Seq[BuildInfoKey](name, version),
		buildInfoPackage := "se.lu.nateko.cp.doi",
		buildInfoKeys ++= Seq(
			BuildInfoKey.action("buildTime") {java.time.Instant.now()},
			BuildInfoKey.action("gitOriginRemote") {
				sbt.Process("git config --get remote.origin.url").lines.mkString("")
			},
			BuildInfoKey.action("gitHash") {
				sbt.Process("git rev-parse HEAD").lines.mkString("")
			}
		),
		resources.in(Compile) += fastOptJS.in(appJs, Compile).value.data,
		watchSources ++= watchSources.in(appJs, Compile).value,
		assembledMappings.in(assembly) := {
			val finalJsFile = fullOptJS.in(appJs, Compile).value.data
			assembledMappings.in(assembly).value :+ sbtassembly.MappingSet(None, Vector((finalJsFile, finalJsFile.getName)))
		}
	)
