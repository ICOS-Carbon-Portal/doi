lazy val commonSettings = Seq(
	organization := "se.lu.nateko.cp",
	scalaVersion := "2.11.8"
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

lazy val core = project.in(file("core"))
	.settings(commonJvmSettings: _*)
	.settings(
		name := "doi-core",
		version := "0.1.0-SNAPSHOT",
		unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala",
		publishTo := {
			val nexus = "https://repo.icos-cp.eu/content/repositories/"
			if (isSnapshot.value)
				Some("snapshots" at nexus + "snapshots")
			else
				Some("releases"  at nexus + "releases")
		},
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
	)

lazy val views = (project in file("views"))
	.settings(commonSettings: _*)
	.enablePlugins(SbtTwirl)
	.settings(
		name := "doi-views",
		version := "0.1.0",
		libraryDependencies += "se.lu.nateko.cp" %% "views-core" % "0.2-SNAPSHOT"
	)

lazy val app = crossProject
	.in(file("."))
	.settings(
		name := "doi",
		unmanagedSourceDirectories in Compile += baseDirectory.value  / "shared" / "src" / "main" / "scala",
		libraryDependencies ++= Seq(
			"com.lihaoyi" %%% "upickle" % "0.4.4"
		)
	)
	.jsSettings(
		name := "doi-js",
		libraryDependencies ++= Seq(
			"com.lihaoyi" %%% "scalatags" % "0.6.2"
		)
	)
	.jsSettings(commonSettings: _*)
	.jvmSettings(
		name := "doi-jvm",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http" % "10.0.0"
		),
		assemblyMergeStrategy.in(assembly) := {
			case PathList(name) if(name.endsWith("-fastopt.js")) =>
				MergeStrategy.discard
			case x =>
				val originalStrategy = assemblyMergeStrategy.in(assembly).value
				originalStrategy(x)
		}
	)
	.jvmSettings(commonJvmSettings: _*)
	.jvmConfigure(_.dependsOn(views, core))

lazy val appJS = app.js
lazy val appJVM = app.jvm
	.settings(
		resources.in(Compile) += fastOptJS.in(appJS, Compile).value.data,
		watchSources ++= watchSources.in(appJS, Compile).value,
		assembledMappings.in(assembly) := {
			val finalJsFile = fullOptJS.in(appJS, Compile).value.data
			assembledMappings.in(assembly).value :+ sbtassembly.MappingSet(None, Vector((finalJsFile, finalJsFile.getName)))
		}
	)

