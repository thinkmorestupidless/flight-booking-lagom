import com.typesafe.sbt.packager.docker._

organization in ThisBuild := "less.stupid"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

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
    dockerRepository := Some("less-stupid-flights"),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dplay.crypto.secret="${APPLICATION_SECRET:-none}" -Dplay.akka.actor-system="${AKKA_ACTOR_SYSTEM_NAME:-flightservice-v1}" -Dhttp.address="$FLIGHTSSERVICE_BIND_IP" -Dhttp.port="$FLIGHTSSERVICE_BIND_PORT" -Dakka.actor.provider=cluster -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$AKKA_REMOTING_BIND_PORT" -Dakka.cluster.seed-nodes.0="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST}:${AKKA_SEED_NODE_PORT}" -Dakka.io.dns.resolver=async-dns -Dakka.io.dns.async-dns.resolve-srv=true -Dakka.io.dns.async-dns.resolv-conf=on""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomJavadslTestKit,
      lombok
    ) ++ BuildTarget.additionalLibraryDependencies
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`less-stupid-flights-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.10"

def common = Seq(
  javacOptions in compile += "-parameters"
)

