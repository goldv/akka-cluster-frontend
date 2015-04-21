name := "akka-cluster-example"
 
version := "1.0"
 
scalaVersion := "2.10.4"

resolvers ++= Seq("krasserm at bintray" at "http://dl.bintray.com/krasserm/maven",  "spray repo" at "http://repo.spray.io")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.9",
  "com.typesafe.akka" %% "akka-contrib" % "2.3.9",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.9",
  "com.github.krasserm" %% "akka-persistence-cassandra" % "0.3.7",
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-json" % "1.2.6"
  )