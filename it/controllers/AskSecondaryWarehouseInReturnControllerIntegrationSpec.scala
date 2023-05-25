package controllers

import controllers.testSupport.helpers.ALFTestHelper
import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.Warehouse
import models.alf.init.{AppLevelLabels, ConfirmPageConfig, EditPageLabels, JourneyConfig, JourneyLabels, JourneyOptions, LanguageLabels, LookupPageLabels, SelectPageConfig, TimeoutConfig}
import models.backend.UkAddress
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class AskSecondaryWarehouseInReturnControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData {
  "AskSecondaryWarehouseInReturnController" should {
    "Ask for if user wants to register any UK warehouses where user used to store liable drinks" in {
      setUpData(emptyUserAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/ask-secondary-warehouses-in-return")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "user selects yes and saves and continues updating the user answers and ramps onto ALF, also NOT wiping the warehouse list" in {
      val warehouseToRemain = Map("foo" -> Warehouse(None, UkAddress(List.empty, "", None)))
      setUpData(emptyUserAnswers.copy(warehouseList = warehouseToRemain))

      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/secondary-warehouses/${sdilNumber}",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703%2Fsoft-drinks-industry-levy-returns-frontend%2Fask-secondary-warehouses-in-return"),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(false),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(SelectPageConfig(
            proposalListLimit = Some(10),
            showSearchAgainLink = Some(true)
          )),
          showBackButtons = Some(true),
          disableTranslations = Some(true),
          allowedCountryCodes = None,
          confirmPageConfig = Some(ConfirmPageConfig(
            showSearchAgainLink = Some(true),
            showSubHeadingAndInfo = Some(true),
            showChangeLink = Some(true),
            showConfirmChangeText = Some(true)
          )),
          timeoutConfig = Some(TimeoutConfig(
            timeoutAmount = 900,
            timeoutUrl = controllers.auth.routes.AuthController.signOut().url,
            timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
          )),
          serviceHref = Some(routes.IndexController.onPageLoad().url),
          pageHeadingStyle = Some("govuk-heading-m")
        ),
        labels = Some(
          JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("Soft Drinks Industry Levy"),
                phaseBannerHtml = None
              )),
              selectPageLabels = None,
              lookupPageLabels = Some(
                LookupPageLabels(
                  title = Some("Find UK warehouse address"),
                  heading = Some("Find UK warehouse address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK warehouse address"),
                  heading = Some("Enter the UK warehouse address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Trading name (optional)"))
              ),
              confirmPageLabels = None,
              countryPickerLabels = None
            ))
          )),
        requestedVersion = None
      )
      val expectedResult: Some[JsObject] = Some(
        Json.obj(
          "askSecondaryWarehouseInReturn" -> true
        ))

      val alfOnRampURL: String = "http://onramp.com"

      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/ask-secondary-warehouses-in-return")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> true))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(sdilNumber).map(userAnswers => userAnswers.warehouseList).get mustBe warehouseToRemain

          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }

      }
    }
    "user selects no and saves and continues, user is taken to check your answers, also wiping the warehouse list" in {
      val warehouseToBeWiped = Map("foo" -> Warehouse(None, UkAddress(List.empty, "", None)))
      setUpData(emptyUserAnswers.copy(warehouseList = warehouseToBeWiped))

      given
        .commonPrecondition

      val expectedResult: Some[JsObject] = Some(
        Json.obj(
          "askSecondaryWarehouseInReturn" -> false
        ))

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/ask-secondary-warehouses-in-return")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> false))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(sdilNumber).map(userAnswers => userAnswers.warehouseList).get mustBe Map.empty

        }

      }
    }

  }
}

