/*enablePlugins(GitVersioning, GitBranchPrompt)
git.baseVersion := "1.0.0"*/

ThisBuild / scalaVersion := "2.12.18"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization  := "org.julianfrech"


libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.2.16" % Test
)
