import mill._, scalalib._, scalanativelib._, publish._

trait Publish extends PublishModule {
  def pomSettings = PomSettings(
    description = "Async Scala Native HTTP Client",
    organization = "com.github.lolgab",
    url = "https://github.com/lolgab/scala-native-http-client-async",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("lolgab", "scala-native-http-client-async"),
    developers = Seq(
        Developer("lolgab", "Lorenzo Gabriele", "https://github.com/lolgab")
      )
  )
  def publishVersion = "0.0.1-SNAPSHOT"
}

object httpclient extends Cross[HttpClientModule]("2.13.8", "3.1.2")
class HttpClientModule(val crossScalaVersion: String) extends CrossScalaModule with ScalaNativeModule with Publish {
  def scalaNativeVersion = "0.4.5"

  def ivyDeps = Agg(
    ivy"com.github.lolgab::native-loop-core::0.2.1"
  )

  def nativeCompileOptions = super.nativeCompileOptions() ++ Seq(
    "-I/usr/local/opt/curl/include"
  )

  object test extends Tests with TestModule.Utest {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest::0.7.11"
    )
  }
}
