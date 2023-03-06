/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import config.features.Features
import config.{ConfigKeys => Keys}
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val features: Features
  val addressLookupCallbackUrl: String
  val timeoutPeriod: Int
  val timeoutCountdown: Int
  def feedbackUrl: String
  def addressLookupService: String
  val accessibilityReportUrl: String
  // val addressLookupUrlHost: String
  val signInContinueBaseUrl: String
  val deskproServiceName: String

}

@Singleton
class FrontendAppConfig @Inject()(implicit configuration: Configuration, servicesConfig: ServicesConfig, environment: Environment) extends AppConfig {

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "soft-drinks-industry-levy-returns-frontend"

  override lazy val feedbackUrl: String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + exitSurveyBaseUrl).encodedUrl}"

  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = configuration.get[String]("urls.signOut")

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/soft-drinks-industry-levy-returns-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  override val features = new Features

  override def addressLookupService: String = {
    if (features.stubAddressLookup()) {
      host + "/vat-through-software/account/test-only/address-lookup-stub"
    } else {
      servicesConfig.baseUrl(Keys.addressLookupFrontend)
    }
  }

  //  override def agentClientLookupUrl: String =
  //    if (features.stubAgentClientLookup()) {
  //      testOnly.controllers.routes.StubAgentClientLookupController.show(controllers.routes.CustomerCircumstanceDetailsController.show.url).url
  //    } else {
  //      vatAgentClientLookupHandoff(controllers.routes.CustomerCircumstanceDetailsController.show.url)
  //    }

  override lazy val addressLookupCallbackUrl: String =
    signInContinueBaseUrl + servicesConfig.getString(Keys.addressLookupCallback)

  override lazy val signInContinueBaseUrl: String = servicesConfig.getString(Keys.signInContinueBaseUrl)

  override lazy val timeoutPeriod: Int = servicesConfig.getInt(Keys.timeoutPeriod)
  override lazy val timeoutCountdown: Int = servicesConfig.getInt(Keys.timeoutCountDown)

  override lazy val accessibilityReportUrl: String = servicesConfig.getString(Keys.accessibilityReportUrl)
  override lazy val deskproServiceName: String = servicesConfig.getString(Keys.deskproServiceName)

}
