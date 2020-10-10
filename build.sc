import mill._, scalalib._, scalafmt._, publish._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

val scalaVersions = List("2.11.12", "2.12.7", "2.13.2")

object client extends Cross[ClientModule](scalaVersions:_*)

class ClientModule(val crossScalaVersion: String)  extends CrossScalaModule with PublishModule with ScalafmtModule {

  override def ivyDeps = Agg(
  	ivy"javax.websocket:javax.websocket-client-api:1.1",
    ivy"org.glassfish.tyrus.bundles:tyrus-standalone-client:1.9"
  )

  override def publishVersion = "0.0.3"

  override def artifactName = "websocket-scala"

  def pomSettings = PomSettings(
    description = "Scala Websocket Client Library",
    organization = "net.domlom",
    url = "https://github.com/lombardo-chcg/websocket-scala",
    // TODO: review Tyrus license model and select best option
    // - "The governance policy is the same as the one used in the GlassFish project. We also use the same two licenses - CDDL 1.1 and GPL 2 with CPE - so, you can pick which one suites your needs better."  https://tyrus-project.github.io/
    licenses = Seq(License.`GPL-2.0-with-classpath-exception`),
    versionControl = VersionControl.github("lombardo-chcg", "websocket-scala"),
    developers = Seq(
      Developer("lombardo-chcg", "Dominick Lombardo", "https://github.com/lombardo-chcg")
    )
  )  
}

object examples extends Cross[ExamplesModule](scalaVersions:_*)

class ExamplesModule(val crossScalaVersion: String) extends CrossScalaModule with ScalafmtModule {

  override def moduleDeps = Seq(client())

  override def mainClass = Some("runnable.HelloWorld")

  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"org.scalactic::scalactic:3.1.1",
      ivy"org.scalatest::scalatest:3.1.1",
      ivy"javax.websocket:javax.websocket-api:1.1",
      ivy"org.glassfish.tyrus:tyrus-server:1.12",
      ivy"org.glassfish.tyrus:tyrus-container-grizzly-server:1.12"
    )

    override def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}