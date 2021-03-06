ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "se.lu.nateko.cp"

val commonSettings = Seq(
	scalacOptions ++= Seq(
		"-encoding", "UTF-8",
		"-unchecked",
		"-feature",
		"-deprecation",
		"-Wdead-code",
		"-Wnumeric-widen"
	),
	libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.9" % "test"
)

val jvmOnlySettings = Seq(
	scalacOptions += "-target:jvm-1.11"
)

val publishSettings = Seq(
	publishTo := {
		val nexus = "https://repo.icos-cp.eu/content/repositories/"
		if (isSnapshot.value)
			Some("snapshots" at nexus + "snapshots")
		else
			Some("releases"  at nexus + "releases")
	},
	credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

//DataCite DOI metadata model, needed for back- and front end
val common = crossProject(JSPlatform, JVMPlatform)
	.crossType(CrossType.Pure)
	.in(file("common"))
	.settings(commonSettings)
	.settings(
		name := "doi-common",
		version := "0.1.3",
		cpDeploy := {
			sys.error("Please switch to project appJVM for deployment")
		}
	)
	.jvmSettings(jvmOnlySettings: _*)
	.settings(publishSettings: _*)

//core functionality that may be reused by different apps (backends)
lazy val core = project
	.in(file("core"))
	.dependsOn(common.jvm)
	.settings(commonSettings ++ jvmOnlySettings ++ publishSettings: _*)
	.enablePlugins(SbtTwirl)
	.settings(
		name := "doi-core",
		version := "0.1.3"
	)

//the DOI minting web app itself
lazy val app = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(commonSettings)
	.settings(
		name := "doi",
		version := "0.1.3",
		libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.9.2"
	)
	.jvmSettings(jvmOnlySettings: _*)
	.jsSettings(
		name := "doi-js",
		libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.9.4",
		scalaJSUseMainModuleInitializer := true
	)
	.jvmSettings(
		name := "doi-jvm",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http"   % "10.2.4",
			"com.typesafe.akka" %% "akka-stream" % "2.6.14",
			"se.lu.nateko.cp"   %% "views-core"  % "0.4.5",
			"se.lu.nateko.cp"   %% "cpauth-core" % "0.6.4"
		),
		reStart / baseDirectory  := {
			(reStart / baseDirectory).value.getParentFile
		},
		assembly / assemblyMergeStrategy := {
			case PathList(name) if(name.endsWith("-fastopt.js") || name.endsWith("module-info.class")) =>
				MergeStrategy.discard
			case x =>
				val originalStrategy = (assembly / assemblyMergeStrategy).value
				originalStrategy(x)
		}
	)
	.jsConfigure(_.dependsOn(common.js))
	.jvmConfigure(_.dependsOn(core))

lazy val appJs = app.js
lazy val appJvm = app.jvm
	.enablePlugins(IcosCpSbtDeployPlugin, SbtTwirl)
	.settings(
		cpDeployTarget := "doi",
		cpDeployBuildInfoPackage := "se.lu.nateko.cp.doi",

		Compile / resources += (appJs / Compile / fastOptJS).value.data,
		watchSources ++= (appJs / Compile / watchSources).value,
		assembly / assembledMappings := {
			val finalJsFile = (appJs / Compile / fullOptJS).value.data
			(assembly / assembledMappings).value :+ sbtassembly.MappingSet(None, Vector((finalJsFile, finalJsFile.getName)))
		}
	)
