import mill._, scalalib._, scalanativelib._

object httpclient extends ScalaNativeModule {
  def scalaVersion = "2.13.8"
  def scalaNativeVersion = "0.4.4"

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
