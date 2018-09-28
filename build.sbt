name := "meme-factory"

version := "1.0"
scalaVersion := "2.12.2"

lazy val `memefactory` = (project in file(".")).enablePlugins(PlayScala)
val circeVersion = "0.9.3"

// default
libraryDependencies ++= Seq(
  jdbc
  , ehcache
  , ws
  , specs2 % Test
  , guice
)

libraryDependencies ++= Seq(

  // doobie
  "org.tpolecat" %% "doobie-core" % "0.5.3",
  "org.tpolecat" %% "doobie-h2" % "0.5.3", // H2 driver 1.4.197 + type mappings.
  "org.tpolecat" %% "doobie-hikari" % "0.5.3", // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % "0.5.3", // Postgres driver 42.2.2 + type mappings.
  "org.tpolecat" %% "doobie-specs2" % "0.5.3", // Specs2 support for typechecking statements.
  "org.tpolecat" %% "doobie-scalatest" % "0.5.3", // ScalaTest support for typechecking statements.

  // cats
  "org.typelevel" %% "cats-core" % "1.4.0",

  // auth JWT
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5"
)

// circe
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
scalacOptions += "-Ypartial-unification"
      