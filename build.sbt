scalaVersion in ThisBuild := "2.13.1"
organization in ThisBuild := "se.lu.nateko.cp"

val commonSettings = Seq(
	scalacOptions ++= Seq(
		"-encoding", "UTF-8",
		"-unchecked",
		"-feature",
		"-deprecation",
		"-Wdead-code"
	),
	libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.0" % "test"
)

val jvmOnlySettings = Seq(
	scalacOptions += "-target:jvm-1.8"
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
		version := "0.1.2",
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
		version := "0.1.2"
	)

//the DOI minting web app itself
lazy val app = crossProject(JSPlatform, JVMPlatform)
	.in(file("."))
	.settings(commonSettings)
	.settings(
		name := "doi",
		version := "0.1.2",
		libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.8.1"
	)
	.jvmSettings(jvmOnlySettings: _*)
	.jsSettings(
		name := "doi-js",
		libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.8.6",
		scalaJSUseMainModuleInitializer := true
	)
	.jvmSettings(
		name := "doi-jvm",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http"   % "10.1.11",
			"com.typesafe.akka" %% "akka-stream" % "2.6.3",
			"se.lu.nateko.cp"   %% "views-core"  % "0.4.2",
			"se.lu.nateko.cp"   %% "cpauth-core" % "0.6.1"
		),
		baseDirectory in reStart := {
			baseDirectory.in(reStart).value.getParentFile
		},
		assemblyMergeStrategy.in(assembly) := {
			case PathList(name) if(name.endsWith("-fastopt.js") || name.endsWith("module-info.class")) =>
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
