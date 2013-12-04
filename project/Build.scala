import sbt._
import Keys._
// sbt-scalariform
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._
// sbt-assembly
import sbtassembly.Plugin._
import AssemblyKeys._
// sbt-revolver
import spray.revolver.RevolverPlugin._
// sbt-dependecy-graph
import net.virtualvoid.sbt.graph.Plugin._

object RootBuild extends Build {
  lazy val main = Project(
    id = "main",
    base = file("."),
    settings = Defaults.defaultSettings ++ buildSettings ++ compileSettings ++ scalariformSettings ++ Revolver.settings ++ assemblySettings ++ graphSettings) settings (
      resolvers ++= resolverSettings,
      libraryDependencies ++= dependencies,
      ScalariformKeys.preferences := formattingSettings,
      javaOptions in Revolver.reStart ++= forkedJvmOption,
      mainClass in assembly := Option("RxAkka"),
      excludedJars in assembly <<= (fullClasspath in assembly) map ( _ filter ( _.data.getName == "scala-compiler.jar" ) ),
      jarName in assembly <<= (name, version) map ( (n, v) => "%s-%s.jar".format(n, v) )
    )

  lazy val appName = "rx-akka"

  lazy val appVersion = "0.1.0"

  lazy val buildSettings = Seq(
    name := appName,
    organization := "io.github.lvicentesanchez",
    version := appVersion,
    scalaVersion := "2.10.3"
  )

  lazy val compileSettings = Seq(
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")
  )

  lazy val dependencies = Seq(
    "com.chuusai"          %% "shapeless"          % "1.2.4", 
    "com.netflix.rxjava"   %  "rxjava-scala"       % "0.15.1",
    "com.typesafe.akka"    %% "akka-actor"         % "2.2.3",
    "com.typesafe.akka"    %% "akka-slf4j"         % "2.2.3",
    // Test libraries
    "org.scalacheck"       %% "scalacheck"         % "1.10.1"  % "test",
    "org.specs2"           %% "specs2"             % "2.2.3"   % "test",
    // Bump dependencies
    "ch.qos.logback"       %  "logback-classic"    % "1.0.13",
    "ch.qos.logback"       %  "logback-core"       % "1.0.13",
    "org.slf4j"            %  "slf4j-api"          % "1.7.5"
  )


  lazy val forkedJvmOption = Seq(
    "-server",
    "-Dfile.encoding=UTF8",
    "-Xss1m",
    "-Xms1536m",
    "-Xmx1536m",
    "-XX:+CMSClassUnloadingEnabled",
    "-XX:MaxPermSize=384m",
    "-XX:ReservedCodeCacheSize=256m",
    "-XX:+DoEscapeAnalysis",
    "-XX:+UseConcMarkSweepGC",
    "-XX:+UseParNewGC",
    "-XX:+UseCodeCacheFlushing",
    "-XX:+UseCompressedOops"
  )

  lazy val formattingSettings =
    FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, false)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
      .setPreference(CompactControlReadability, false)
      .setPreference(CompactStringConcatenation, false)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(FormatXml, true)
      .setPreference(IndentLocalDefs, false)
      .setPreference(IndentPackageBlocks, true)
      .setPreference(IndentSpaces, 2)
      .setPreference(IndentWithTabs, false)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
      .setPreference(PreserveSpaceBeforeArguments, false)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(SpaceBeforeColon, false)
      .setPreference(SpaceInsideBrackets, false)
      .setPreference(SpaceInsideParentheses, false)
      .setPreference(SpacesWithinPatternBinders, true)

  lazy val resolverSettings = Seq(
    "sonatype oss releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "sonatype oss snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )
}
