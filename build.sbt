name := "Z3jar"

version := "1.0"

organization := "ch.epfl.lara"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.4", "2.11.7")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test"

libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  // if scala 2.11+ used, add dependency on scala-xml module
  case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.2")
  case _                                         => Seq.empty
})

fork in Test := true
