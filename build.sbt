import com.lightbend.lagom.sbt.LagomImport.lagomScaladslApi
import org.ensime.EnsimeKeys._
import org.ensime.EnsimePlugin

//resolvers += "Nexus maven" at "http://yum:8081/repository/maven/"
//resolvers += "Nexus ivy" at "http://yum:8081/repository/ivy/"

lazy val root = (project in file("."))
  .settings(name := "hello-world")
  .aggregate(
    `common`,
    helloWorldApi,
    helloWorldImpl,
    `identity-api`,
    `identity-impl`,
    `base64`
  )
  .settings(commonSettings: _*)

organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "4.0.0"
val jbcrypt = "org.mindrot" % "jbcrypt" % "0.3m"
val cuid = "cool.graph" % "cuid-java" % "0.1.1"
val jwt = "com.pauldijou" %% "jwt-play-json" % "0.12.1"
val accord = "com.wix" %% "accord-core" % "0.7.2"
val accord061 = "com.wix" %% "accord-core" % "0.6.1"
val cassandraDriverExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0" // Adds extra codecs

libraryDependencies += lagomScaladslPersistenceCassandra

lazy val `base64` = (project in file("base64"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.0" % "test",
      "commons-codec" % "commons-codec" % "1.9" % "test",
      "io.netty" % "netty-codec" % "4.0.23.Final" % "test"
    )
  )

lazy val `common` = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer,
      jwt,
      accord061,
      playJsonDerivedCodecs,
      scalaTest
    )
  )

lazy val helloWorldApi = (project in file("hello-world/hello-world-api"))
  .dependsOn(`common`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs,
      "ai.x" %% "play-json-extensions" % "0.10.0"
    )
  )

lazy val helloWorldImpl = (project in file("hello-world/hello-world-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      lagomScaladslPubSub,
      cassandraDriverExtras,
      macwire,
      cuid,
      jbcrypt,
      scalaTest,
      "ai.x" %% "play-json-extensions" % "0.10.0"
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(helloWorldApi, `common`, `base64`)

lazy val `identity-api` = (project in file("identity/identity-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      accord
    )
  )
  .dependsOn(`common`)

lazy val `identity-impl` = (project in file("identity/identity-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
////      base64,
      jwt
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`common`, `identity-api`, `base64`)

def commonSettings: Seq[Setting[_]] = Seq(
)

lagomCassandraCleanOnStart in ThisBuild := true
////lagomCassandraEnabled in ThisBuild := false
////lagomUnmanagedServices in ThisBuild := Map("cas_native" -> "http://localhost:9042")

////lagomKafkaEnabled in ThisBuild := false
////lagomKafkaAddress in ThisBuild := "localhost:10000"

ensimeScalaVersion in ThisBuild := "2.12.7"
ensimeIgnoreScalaMismatch in ThisBuild := true
ensimeIgnoreMissingDirectories := true

lazy val serviceLocatorProject = ProjectRef(uri("."), "lagom-internal-meta-project-service-locator")
ensimeUnmanagedSourceArchives in serviceLocatorProject := Nil
ensimeUnmanagedJavadocArchives in serviceLocatorProject := Nil
ensimeScalacTransformer in serviceLocatorProject := identity
ensimeScalacOptions in serviceLocatorProject := EnsimePlugin.ensimeSuggestedScalacOptions(scalaVersion.value)
ensimeJavacOptions in serviceLocatorProject := Nil

lazy val cassandraProject = ProjectRef(uri("."), "lagom-internal-meta-project-cassandra")
ensimeUnmanagedSourceArchives in cassandraProject := Nil
ensimeUnmanagedJavadocArchives in cassandraProject := Nil
ensimeScalacTransformer in cassandraProject := identity
ensimeScalacOptions in cassandraProject := EnsimePlugin.ensimeSuggestedScalacOptions(scalaVersion.value)
ensimeJavacOptions in cassandraProject := Nil

lazy val kafkaProject = ProjectRef(uri("."), "lagom-internal-meta-project-kafka")
ensimeUnmanagedSourceArchives in kafkaProject := Nil
ensimeUnmanagedJavadocArchives in kafkaProject := Nil
ensimeScalacTransformer in kafkaProject := identity
ensimeScalacOptions in kafkaProject := EnsimePlugin.ensimeSuggestedScalacOptions(scalaVersion.value)
ensimeJavacOptions in kafkaProject := Nil

libraryDependencies += "org.scalameta" %% "scalameta" % "4.1.0"