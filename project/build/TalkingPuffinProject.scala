import sbt._

class TalkingPuffinProject(info: ProjectInfo) extends DefaultProject(info) {

  //Repositories
  val twittr = "twittr" at "http://maven.twttr.com/"

  //Modules
  lazy val common = project("common", "common")
  lazy val desktop = project("desktop", "desktop")
  lazy val webmvn = project("web-mvn", "web-mvn")

  //Scala compile settings
  val scalaCompileSettings =
    Seq("-Xmigration",
      "-Xcheckinit",
      "-Xwarninit",
      "-encoding", "utf8")

  override def compileOptions = super.compileOptions ++ scalaCompileSettings.map(CompileOption)

  //Dependencies
  //Common
  val twitter4j = "org.twitter4j" % "twitter4j-core" % "2.1.11"
  val configgy = "net.lag" % "configgy" % "2.0.0"
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.7.2" % "test"
  val commons_lang = "commons-lang" % "commons-lang" % "2.5"
  val google_collections = "com.google.collections" % "google-collections" % "1.0-rc1"
  val log4j = "log4j" % "log4j" % "1.2.14"
  val junit = "junit" % "junit" % "4.5" % "test"
  val joda_time = "joda-time" % "joda-time" % "1.6"
  //Desktop
  val swingx = "org.swinglabs" % "swingx" % "1.0"
  //web-mvn
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val lift_widgets = "net.liftweb" %% "lift-widgets" % "2.3-RC4"
  val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25" % "test"

}
