import sbt._

object AppDependencies {


  private val playVersion = "play-30"
  private val bootstrapVersion = "8.6.0"
  private val hmrcMongoVersion = "2.2.0"
//  private val playFrontendHMRCVersion = "8.5.0"

  // Test dependencies
//  private val scalaTestPlusPlayVersion = "7.0.0"
//  private val scalatestPlusScalacheckVersion = "3.2.17.0"
  private val mockitoScalatestVersion = "1.17.30"



  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion" % "8.5.0",
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping-$playVersion" % "3.1.0",
    "org.typelevel"     %% "cats-core"                     % "2.10.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% s"crypto-json-$playVersion"        % "8.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"             %% "mockito-scala-scalatest"      % "1.17.30",
    "org.scalatestplus"       %% "scalacheck-1-17"              % "3.2.17.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"           % "7.0.0",
    "org.jsoup"               %  "jsoup"                        % "1.15.4",
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-test-$playVersion"   % hmrcMongoVersion,
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"        % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
