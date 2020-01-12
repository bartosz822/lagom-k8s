organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test

val dockerRepo = Some("eu.gcr.io/PROJECT_ID")

lazy val `hello` = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(`hello-api`, `hello-impl`, `hello-stream-api`, `hello-stream-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    ),
    publish / skip := true
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslAkkaDiscovery,
      lagomScaladslTestKit,
      "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.5",
      macwire,
      scalaTest
    ),
    dockerRepository := dockerRepo,
    publish / skip := true
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`hello-api`)

lazy val `hello-stream-api` = (project in file("hello-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    ),
    publish / skip := true
  )

lazy val `hello-stream-impl` = (project in file("hello-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      lagomScaladslAkkaDiscovery,
      "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.5",
      macwire,
      scalaTest
    ),
    dockerRepository := dockerRepo,
    publish / skip := true
  )
  .dependsOn(`hello-stream-api`, `hello-api`)
