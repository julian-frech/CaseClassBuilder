
import mill._, scalalib._
import mill.scalalib.Assembly._

object caseclassbuilder extends RootModule with SbtModule  {
  def scalaVersion = "2.12.18"

  // Define your project's dependencies
  def ivyDeps = Agg(
    ivy"org.scala-lang:scala-reflect:${scalaVersion()}",
    ivy"org.scalatest::scalatest:3.2.15"
  )

  // Define your main class if applicable
  def mainClass = T{ Some("org.julianfrech.samples.CaseClassInitializer") }

  // Configure shading rules for the assembly
  override def assemblyRules = Seq(
    Rule.Append("reference.conf"),
    Rule.Relocate("scala.reflect.**", "shaded.reflect.@1")
  )

}