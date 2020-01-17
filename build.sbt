name := "CaesarCipher"
version := "0.2"
scalaVersion := "2.13.1"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.0"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.29"
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)
scalacOptions += "-Ymacro-annotations"
libraryDependencies += "org.scalafx" %% "scalafxml-core-sfx8" % "0.5"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}