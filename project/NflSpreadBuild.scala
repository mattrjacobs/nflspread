import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import sbtassembly.Plugin._
import AssemblyKeys._

object NflSpreadBuild extends Build {
  import Resolvers._

  lazy val buildSettings = Seq(
    organization := "com.mattrjacobs.nfl",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.2"
  )

  lazy val nflspread = Project(
    id = "nflspread",
    base = file("."),
    settings = defaultSettings ++ assemblySettings ++ Seq(libraryDependencies ++= Dependencies.all)
  )

  val repos = Seq(sonatypeReleaseRepo)

  override lazy val settings = super.settings ++ buildSettings ++ Seq(
    resolvers := repos,
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
)
  
  lazy val baseSettings = Defaults.defaultSettings
  
  lazy val defaultSettings = {
    import Dependency._
    baseSettings ++ formatSettings ++ Seq(
      resolvers := repos,
      scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature", "-Yrangepos"),
      libraryDependencies ++= Seq(Compile.jodaTime, Test.specs2, Test.specs2Mock, Test.mockito, Test.scalacheck),
      ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet
    )
  }

  lazy val formatSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test    := formattingPreferences
  )

   def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentClassDeclaration, true)
   }
}

object Dependencies {
  import Dependency._

  val all = Seq(Compile.jodaTime, Compile.jodaConvert, Runtime.logback)
}

object Dependency {
  object V {
    val JodaConvert = "1.2"
    val JodaTime = "2.1"
    val Logback = "1.0.3"
    val Mockito = "1.9.0"
    val ScalaCheck = "1.11.1"
    val Specs2 = "2.4.4"
  }

  object Runtime {
    val logback = "ch.qos.logback" %  "logback-classic" % V.Logback
  }

  object Compile {
    val jodaConvert = "org.joda" % "joda-convert" % V.JodaConvert
    val jodaTime = "joda-time" % "joda-time" % V.JodaTime
  }

  object Test {
    val logbackTest = "ch.qos.logback" % "logback-classic" % V.Logback % "test"
    val mockito = "org.mockito" % "mockito-all" % V.Mockito % "test"
    val scalacheck = "org.scalacheck" %% "scalacheck" % V.ScalaCheck % "test"
    val specs2 = "org.specs2" %% "specs2-core" % V.Specs2 % "test"
    val specs2Mock = "org.specs2" %% "specs2-mock" % V.Specs2 % "test"
  }
}

object Resolvers {
  val sonatypeReleaseRepo = "Sonatype Release Repo"  at "http://oss.sonatype.org/content/repositories/releases"
}

