/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import controllers.testSupport.helpers.ALFTestHelper
import models.alf.init.*
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class PackAtBusinessAddressControllerIntegrationSpec extends ControllerITTestHelper with TryValues {

  "PackAtBusinessAddressController" should {

    "Ask the user if the address shown, is a UK packaging site they operate to produce liable drinks " in {

      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/pack-at-business-address-in-return")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "user selected no, user should be taken to ALF" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/packing-sites/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-returns-frontend"),
          phaseFeedbackLink = Some(
            s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703%2Fsoft-drinks-industry-levy-returns-frontend%2Fpack-at-business-address-in-return"
          ),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(false),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(
            SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )
          ),
          showBackButtons = Some(true),
          disableTranslations = Some(true),
          allowedCountryCodes = None,
          confirmPageConfig = Some(
            ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )
          ),
          timeoutConfig = Some(
            TimeoutConfig(
              timeoutAmount = 900,
              timeoutUrl = controllers.auth.routes.AuthController.signOut().url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )
          ),
          serviceHref = Some(frontendAppConfig.sdilHomeUrl),
          pageHeadingStyle = Some("govuk-heading-l")
        ),
        labels = Some(
          JourneyLabels(
            en = Some(
              LanguageLabels(
                appLevelLabels = Some(
                  AppLevelLabels(
                    navTitle = Some("Soft Drinks Industry Levy"),
                    phaseBannerHtml = None
                  )
                ),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK packaging site address"),
                    heading = Some("Find UK packaging site address"),
                    postcodeLabel = Some("Postcode")
                  )
                ),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK packaging site address"),
                    heading = Some("Enter the UK packaging site address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = Some("Packaging site name (optional)")
                  )
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              )
            )
          )
        ),
        requestedVersion = None
      )
      val expectedResultInDB: Some[JsObject] = Some(
        Json.obj(
          "packAtBusinessAddress" -> false
        )
      )
      val alfOnRampURL: String = "http://onramp.com"

      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)
        .alf
        .getSuccessResponseFromALFInit(alfOnRampURL)

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/pack-at-business-address-in-return")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "false"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB

          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }
      }
    }

    "user selected yes" in {
      val expectedResultInDB: Some[JsObject] = Some(
        Json.obj(
          "packAtBusinessAddress" -> true
        )
      )

      val testSite = Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), None, Some("Super Lemonade Plc"), None)

      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/pack-at-business-address-in-return")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "true"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/packaging-site-details")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB
          getAnswers(sdilNumber).map(userAnswers => userAnswers.packagingSiteList).get.values must contain(testSite)
        }
      }
    }
    testUnauthorisedUser(baseUrl + "/pack-at-business-address-in-return")
  }

}
