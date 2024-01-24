enablePlugins(GitVersioning, GitBranchPrompt)
git.baseVersion := "1.0.0"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "CaseClassBuilder",
    idePackagePrefix := Some("org.julianfrech.samples")
  )

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

