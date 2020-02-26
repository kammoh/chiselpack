//lazy val publishSettings = Seq(
//  publishMavenStyle := true,
//  publishArtifact in Test := false,
//  pomIncludeRepository := { x => false },
//  // Don't add 'scm' elements if we have a git.remoteRepo definition,
//  //  but since we don't (with the removal of ghpages), add them in below.
//  pomExtra := <url>http://chisel.eecs.berkeley.edu/</url>
//    <licenses>
//      <license>
//        <name>BSD-style</name>
//        <url>http://www.opensource.org/licenses/bsd-license.php</url>
//        <distribution>repo</distribution>
//      </license>
//    </licenses>
//    <scm>
//      <url>https://github.com/kammoh/chiselpack.git</url>
//      <connection>scm:git:github.com/kammoh/chiselpack.git</connection>
//    </scm>
//    <developers>
//      <developer>
//        <id>kammoh</id>
//        <name>Kamyar Mohajerani</name>
//        <url>kamyar.xyz</url>
//      </developer>
//    </developers>,
//
//  publishTo := {
//    val v = version.value
//    val nexus = "https://oss.sonatype.org/"
//    if (v.trim.endsWith("SNAPSHOT")) {
//      Some("snapshots" at nexus + "content/repositories/snapshots")
//    }
//    else {
//      Some("releases" at nexus + "service/local/staging/deploy/maven2")
//    }
//  }
//)


publishMavenStyle := true

publishArtifact in Test := false

Global / onChangedBuildSource := ReloadOnSourceChanges


enablePlugins(xerial.sbt.Sonatype)
//// [Optional] The local staging folder name:
//sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / target.value.getName / "sonatype-staging" / s"${version.value}"
//
//// [Optional] If you need to manage unique session names by yourself, change this default setting:
//sonatypeSessionName := s"[sbt-sonatype] ${name.value} ${version.value}"

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

//  .settings(skip in publish := true)
//  .settings(unmanagedBase := (unmanagedBase in root).value)

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

