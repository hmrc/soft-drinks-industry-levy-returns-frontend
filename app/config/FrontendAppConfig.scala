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

import com.google.inject.{ Inject, Singleton }
import com.typesafe.config.Config
import models.Mode
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URLEncoder

@Singleton
class FrontendAppConfig @Inject() (servicesConfig: ServicesConfig, configuration: Configuration) {

  val appName: String = servicesConfig.getString("appName")
  val returnsFrontendBaseUrl: String = servicesConfig.baseUrl("soft-drinks-industry-levy-returns-frontend")

  private val contactHost: String = servicesConfig.getConfString("contact-frontend.host", throw new Exception("missing config contact-frontend.host"))
  private val contactFormServiceIdentifier: String = "soft-drinks-industry-levy-returns-frontend"

  def feedbackUrl(implicit request: RequestHeader): String = {
    val backUrl = URLEncoder.encode(returnsFrontendBaseUrl + request.uri, "UTF-8")
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=$backUrl"
  }

  private val accessibilityHost: String = servicesConfig.getConfString("accessibility-statement.host", throw new Exception("missing config accessibility-statement.host"))
  def accessibilityFooterUrl = s"$accessibilityHost/accessibility-statement/soft-drinks-industry-levy-returns-frontend"

  val basGatewayBaseUrl: String = servicesConfig.baseUrl("bas-gateway")
  val sdilBaseUrl: String = servicesConfig.baseUrl("soft-drinks-industry-levy")
  val sdilHomeUrl: String = servicesConfig.getString("sdilHomeUrl")

  val loginUrl: String = s"$basGatewayBaseUrl/bas-gateway/sign-in"
  val loginContinueUrl: String = sdilHomeUrl
  val signOutUrl: String = s"$basGatewayBaseUrl/bas-gateway/sign-out-without-state"

  private val exitSurveyBaseUrl: String = servicesConfig.baseUrl("feedback-frontend")
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/soft-drinks-industry-levy-returns-frontend"

  val timeout: Int = servicesConfig.getInt("timeout-dialog.timeout")
  val countdown: Int = servicesConfig.getInt("timeout-dialog.countdown")

  val cacheTtl: Int = servicesConfig.getInt("mongodb.timeToLiveInSeconds")
  val sdilCacheTTL: Int = servicesConfig.getInt("mongodb.short-lived.timeToLiveInSeconds")

  val lowerBandCostPerLitre: BigDecimal = BigDecimal(servicesConfig.getString("lowerBandCostPerLitre"))
  val higherBandCostPerLitre: BigDecimal = BigDecimal(servicesConfig.getString("higherBandCostPerLitre"))

  val lowerBandCostPerLitrePostApril2025: BigDecimal = BigDecimal(servicesConfig.getString("lowerBandCostPerLitrePostApril2025"))
  val higherBandCostPerLitrePostApril2025: BigDecimal = BigDecimal(servicesConfig.getString("higherBandCostPerLitrePostApril2025"))

  val balanceAllEnabled: Boolean = servicesConfig.getBoolean("balanceAll.enabled")
  val addressLookUpFrontendTestEnabled: Boolean = servicesConfig.getBoolean("addressLookupFrontendTest.enabled")
  val addressLookupService: String = servicesConfig.baseUrl("address-lookup-frontend")
  val addressLookupOffRampUrl: String = servicesConfig.getString("addressLookupOffRampUrl")

  object AddressLookupConfig {

    private val addressLookupInitConfig: Config = configuration
      .getOptional[Configuration](s"address-lookup-frontend-init-config")
      .getOrElse(throw new IllegalArgumentException(s"Configuration for address-lookup-frontend-init-config not found"))
      .underlying

    val alphaPhase: Boolean = addressLookupInitConfig.getBoolean("alphaPhase")
    val version: Int = addressLookupInitConfig.getInt("version")
    val selectPageConfigProposalLimit: Int = addressLookupInitConfig.getInt("select-page-config.proposalListLimit")

    object WarehouseDetails {

      def offRampUrl(siteId: String, mode: Mode): String = {
        s"$addressLookupOffRampUrl${controllers.addressLookupFrontend.routes.RampOffController.secondaryWareHouseDetailsOffRamp(siteId, "", mode).url.replace("?id=", "")}"
      }
    }

    object PackingDetails {
      def offRampUrl(siteId: String, mode: Mode): String = {
        s"$addressLookupOffRampUrl${controllers.addressLookupFrontend.routes.RampOffController.packingSiteDetailsOffRamp(siteId, "", mode).url.replace("?id=", "")}"
      }
    }
  }

}