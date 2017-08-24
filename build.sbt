organization in ThisBuild := "less.stupid"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val `less-stupid-flights` = (project in file("."))
  .aggregate(`less-stupid-flights-api`, `less-stupid-flights-impl`)

lazy val `less-stupid-flights-api` = (project in file("less-stupid-flights-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `less-stupid-flights-impl` = (project in file("less-stupid-flights-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomJavadslTestKit,
      lombok,
      assertJ,
      faker
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`less-stupid-flights-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.10"
val assertJ = "org.assertj" % "assertj-core" % "3.8.0"
val faker = "com.github.javafaker" % "javafaker" % "0.13"

def common = Seq(
  javacOptions in compile += "-parameters"
)

lagomCassandraCleanOnStart in ThisBuild := false

