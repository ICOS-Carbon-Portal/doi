scalaVersion in ThisBuild := "2.12.8"
organization in ThisBuild := "se.lu.nateko.cp"

watchService in ThisBuild := (() => new sbt.io.PollingWatchService(pollInterval.value)) //SBT bug
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
	.settings(
		name := "doi-common",
		version := "0.1.1-SNAPSHOT",
		libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.5" % "test",
		cpDeploy := {
			sys.error("Please switch to project appJVM for deployment")
		}
	)
	.jvmSettings(commonJvmSettings ++ publishSettings: _*)
	.jsSettings(name := "doi-common-js")
	.jvmSettings(name := "doi-common-jvm")

//core functionality that may be reused by different apps (backends)
lazy val core = project
	.in(file("core"))
	.dependsOn(common.jvm)
	.settings(commonJvmSettings ++ publishSettings: _*)
	.enablePlugins(SbtTwirl)
	.settings(
		name := "doi-core",
		version := "0.1.1-SNAPSHOT",
		libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
	)

//the DOI minting web app itself
lazy val app = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(
		name := "doi",
		libraryDependencies ++= Seq(
			"com.typesafe.play" %%% "play-json" % "2.6.13",
			"org.scalatest"     %%% "scalatest" % "3.0.5" % "test",
		)
	)
	.jvmSettings(commonJvmSettings: _*)
	.jsSettings(
		name := "doi-js",
		libraryDependencies ++= Seq(
			"com.lihaoyi" %%% "scalatags" % "0.6.7"
		),
		scalaJSUseMainModuleInitializer := true
	)
	.jvmSettings(
		name := "doi-jvm",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http"   % "10.1.5",
			"com.typesafe.akka" %% "akka-stream" % "2.5.19",
			"se.lu.nateko.cp"   %% "views-core"  % "0.4.0-SNAPSHOT",
			"se.lu.nateko.cp"   %% "cpauth-core" % "0.6.0-SNAPSHOT"
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
		}
	)
	.jsConfigure(_.dependsOn(common.js))
	.jvmConfigure(_.dependsOn(core).enablePlugins(SbtTwirl))

lazy val appJs = app.js
lazy val appJvm = app.jvm
	.enablePlugins(IcosCpSbtDeployPlugin)
	.settings(
		cpDeployTarget := "doi",
		cpDeployBuildInfoPackage := "se.lu.nateko.cp.doi",

		resources.in(Compile) += fastOptJS.in(appJs, Compile).value.data,
		watchSources ++= watchSources.in(appJs, Compile).value,
		assembledMappings.in(assembly) := {
			val finalJsFile = fullOptJS.in(appJs, Compile).value.data
			assembledMappings.in(assembly).value :+ sbtassembly.MappingSet(None, Vector((finalJsFile, finalJsFile.getName)))
		}
	)

