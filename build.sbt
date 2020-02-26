publishMavenStyle := true

publishArtifact in Test := false

//Global / onChangedBuildSource := ReloadOnSourceChanges

enablePlugins(xerial.sbt.Sonatype)

publishTo := sonatypePublishToBundle.value

lazy val chisel = (project in file("chisel3"))
  .settings(publish / skip := true)
//  .settings(unmanagedBase := (unmanagedBase in root).value)
  .dependsOn(firrtl)
  .settings(addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
  .dependsOn(firrtl, coreMacros, chiselFrontend)
  .aggregate(firrtl, coreMacros, chiselFrontend)


lazy val chiseltest = (project in file("chiseltest"))
  .dependsOn(firrtl, treadle, chisel)
  //.aggregate(firrtl, treadle, chisel)
  .settings(publish / skip := true)


lazy val firrtl = (project in file("firrtl"))
  .settings(publish / skip := true)

lazy val chiselFrontend = (project in file("chisel3/chiselFrontend"))
  .settings(addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
  .dependsOn(coreMacros)
  .dependsOn(firrtl)
  .aggregate(firrtl)
  .settings(publish / skip := true)

lazy val coreMacros = (project in file("chisel3/coreMacros"))
  .dependsOn(firrtl)
  .settings(publish / skip := true)

lazy val treadle = (project in file("treadle"))
  .dependsOn(firrtl)
  .settings(publish / skip := true)

lazy val diagrammer = (project in file("diagrammer"))
  .dependsOn(firrtl,treadle, chisel)
  .aggregate(firrtl, treadle, chisel)
  .settings(publish / skip := true)

lazy val subProjects = Seq(treadle, firrtl, coreMacros, chiselFrontend, chisel, chiseltest, diagrammer)

lazy val settingsAlreadyOverriden = SettingKey[Boolean]("settingsAlreadyOverriden", "Has overrideSettings command already run?")
settingsAlreadyOverriden := false
commands += Command.command("overrideSettings") { state =>
  if (settingsAlreadyOverriden.value) {
    state
  } else {
    Project.extract(state).appendWithSession(
      subProjects.flatMap { subproject =>
        Seq(
          subproject / organization := organization.value,
          subproject / scalaVersion := scalaVersion.value,
          subproject / libraryDependencies := (subproject / libraryDependencies).value.filter(_.organization != "edu.berkeley.cs"),
          subproject / allDependencies := (subproject / allDependencies).value.filter(_.organization != "edu.berkeley.cs"),
        )
      } :+ (settingsAlreadyOverriden := true)
      ,
      state
    )
  }
}

onLoad in Global := {
  ((s: State) => {
    "overrideSettings" :: s
  }) compose (onLoad in Global).value
}

lazy val root = RootProject(file("."))

lazy val chiselpack = (project in file("."))
  .aggregate(chisel, chiselFrontend, coreMacros, chiseltest, diagrammer)
  .settings(publish / skip := false)
  .settings(
    aggregate := false,
    exportJars := true,
  )
  .settings(subProjects.flatMap { subproject =>
    Seq(mappings in(Compile, packageBin) ++= (mappings in(subproject, Compile, packageBin)).value,
      mappings in(Compile, packageSrc) ++= (mappings in(subproject, Compile, packageSrc)).value,
    )
  })
  .settings(subProjects.map { subproject  =>
    libraryDependencies ++= (subproject / libraryDependencies).value.filter(m => m.configurations.isEmpty && m.organization != "edu.berkeley.cs" &&
      m.organization != organization.value)
  })

