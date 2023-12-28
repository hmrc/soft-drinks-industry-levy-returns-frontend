import sbt._

object AppDependencies {
  import play.core.PlayVersion


  val playSuffix = s"-play-28"
  val bootstrapVersion = "7.22.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "7.19.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"       %% s"bootstrap-frontend$playSuffix" % bootstrapVersion,
    "org.typelevel"     %% "cats-core"                     % "2.9.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playSuffix"           % "1.6.0",
    "uk.gov.hmrc"       %% s"crypto-json$playSuffix"        % "7.6.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"            %% "scalatest"                    % "3.2.17"   ,
    "org.scalacheck"           %% "scalacheck"                   % "1.17.0"   ,
    "org.scalatestplus"        %% "scalacheck-1-17"              % "3.2.14.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"           % "5.1.0",
    "org.pegdown"              %  "pegdown"                      % "1.6.0",
    "org.jsoup"                %  "jsoup"                        % "1.15.4",
    "com.typesafe.play"        %% "play-test"                    % PlayVersion.current,
    "org.mockito"              %% "mockito-scala"                % "1.17.14",
    "uk.gov.hmrc"              %% s"bootstrap-test$playSuffix"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"        %% s"hmrc-mongo-test$playSuffix"  % "1.6.0",
    "com.vladsch.flexmark"      %  "flexmark-all"                 % "0.64.8",
    "com.github.tomakehurst"  % "wiremock-standalone"           % "2.27.2",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"        % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
