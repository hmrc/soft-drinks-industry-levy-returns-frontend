import sbt._

object AppDependencies {


  private val playSuffix = s"-play-30"
  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"
  private val playFrontendHMRCVersion = "8.5.0"

  // Test dependencies
  private val scalaTestPlusPlayVersion = "7.0.0"
  private val scalatestPlusScalacheckVersion = "3.2.17.0"
  private val mockitoScalatestVersion = "1.17.30"



  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc$playSuffix" % playFrontendHMRCVersion,
    "uk.gov.hmrc"       %% s"bootstrap-frontend$playSuffix" % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping$playSuffix" % "2.0.0",
    "org.typelevel"     %% "cats-core"                     % "2.10.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playSuffix"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% s"crypto-json$playSuffix"        % "7.6.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"             %% "mockito-scala-scalatest"      % mockitoScalatestVersion,
    "org.scalatestplus"       %% "scalacheck-1-17"              % scalatestPlusScalacheckVersion,
    "org.scalatestplus.play"  %% "scalatestplus-play"           % scalaTestPlusPlayVersion,
    "org.jsoup"               %  "jsoup"                        % "1.15.4",
    "uk.gov.hmrc"             %% s"bootstrap-test$playSuffix"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-test$playSuffix"   % hmrcMongoVersion,
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"        % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
