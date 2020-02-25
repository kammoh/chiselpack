//sonatypeProfileName := "xyz.kamyar"
//
publishMavenStyle := true
//
// Open-source license of your choice
licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

//// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("kammoh", "chiselpack", "kammoh@gmail.com"))
//// or
//sonatypeProjectHosting := Some(GitLabHosting("kamyar", "chiselpack", "kammoh@gmail.com"))
//
//// or if you want to set these fields manually
homepage := Some(url("https://github.com/kammoh/chiselpack"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/kammoh/chiselpack"),
    "scm:git@github.com:kammoh/chiselpack.git"
  )
)
developers := List(
  Developer(id="kammoh", name="Kamyar Mohajerani", email="kammoh@gmail.com", url=url("http://kamyar.xyz"))
)