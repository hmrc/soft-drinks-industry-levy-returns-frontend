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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.{ReturnCharge, ReturnPeriod, SmallProducer, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.{CacheMap, SDILSessionCache, SessionRepository}
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future


class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfter {

  val bareBoneUserAnswers = UserAnswers(sdilNumber, Json.obj(), List())
  val defaultReturnPeriod = Some(ReturnPeriod(year = 2023, quarter = 1))
  val cacheMap = CacheMap("ID",Map())
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionRepository = mock[SessionRepository]
  val mockConfig = mock[FrontendAppConfig]
  val mockSDILSessionCache = mock[SDILSessionCache]

  when(mockConfig.balanceAllEnabled).thenReturn(false)
  when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal(0.18))
  when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal(0.24))
  when(mockSdilConnector.retrieveSubscription(any(), any())(any())).thenReturn(Future.successful(Some(aSubscription)))
  when(mockSdilConnector.returns_pending(any())(any())).thenReturn(Future.successful(List.empty[ReturnPeriod]))
  when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
  when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(financialItemList))
  when(mockSdilConnector.balance(any(), any())(any())).thenReturn(Future.successful(BigDecimal(100)))
  when(mockSDILSessionCache.save(any(),any(),any())(any())).thenReturn(Future.successful(cacheMap))

  "Check Your Answers Controller onPageLoad" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad().url
      }
    }

    "must return OK and contain company alias and return correct description for period 0 in grey pre header" in {

      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2022, quarter = 0))).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("firstQuarter")} 2022"

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 1 in grey pre header" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2021, quarter = 1))).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2021"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 2 in grey pre header" in {

      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2020, quarter = 2))).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("thirdQuarter")} 2020"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 3 in grey pre header" in {

      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2019, quarter = 3))).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("fourthQuarter")} 2019"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must throw and exception when return period is not returned" in {

      val application = applicationBuilder(Some(bareBoneUserAnswers), None).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        route(application, request).value
      }

      intercept[RuntimeException](
        result mustBe an[RuntimeException]
      )
    }

    "must show own brands packaged at own site row when no selected" in {

      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("ownBrandsPackagedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingOwnBrandsPackagedAtYourOwnSite"))
        page.getElementById("change-own-brands").attributes().get("href") mustEqual s"$baseUrl/change-own-brands-packaged-at-own-sites"

        page.getElementsByTag("dt").text() mustNot include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litresInTheHighBand"))
      }
    }

    "must show own brands packaged at own site row containing calculation when yes is selected" in {

      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand"-> 10000 , "highBand"-> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("ownBrandsPackagedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingOwnBrandsPackagedAtYourOwnSite"))
        page.getElementById("change-own-brands").attributes().get("href") mustEqual s"$baseUrl/change-own-brands-packaged-at-own-sites"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-own-site").attributes().get("href") mustEqual s"$baseUrl/change-how-many-own-brands-packaged-at-own-sites"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-own-site").attributes().get("href") mustEqual s"$baseUrl/change-how-many-own-brands-packaged-at-own-sites"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("ownBrandsPackagedAtYourOwnSite.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("ownBrandsPackagedAtYourOwnSite.highband.litres.hidden"))
      }
    }

    "must show own brands packaged at own site row containing small calculations when yes is selected" in {

      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 5, "highBand" -> 3)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("dd").text() must include("5")
        page.getElementsByTag("dd").text() must include("£0.90")
        page.getElementsByTag("dd").text() must include("3")
        page.getElementsByTag("dd").text() must include("£0.72")
      }
    }

    "must show packaged contract packer row when present and answer is no" in {

      val userAnswersData = Json.obj("packagedContractPacker" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("contractPackedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingContractPackedAtYourOwnSite"))
        page.getElementById("change-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-packaged-as-contract-packer"
      }
    }

    "must show packaged contract packer row containing calculation when yes is selected" in {

      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 10000 , "highBand"-> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("contractPackedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingContractPackedAtYourOwnSite"))
        page.getElementById("change-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-packaged-as-contract-packer"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-how-many-packaged-as-contract-packer"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-how-many-packaged-as-contract-packer"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedAtYourOwnSite.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedAtYourOwnSite.highband.litres.hidden"))
      }
    }

    "must show exemption for small producers row when present and answer is no" in {

      val userAnswersData = Json.obj("exemptionsForSmallProducers" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("contractPackedForRegisteredSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("exemptionForRegisteredSmallProducers"))
        page.getElementById("change-exemption-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-exemptions-for-small-producers"

        page.getElementsByTag("dt").text() mustNot include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litresInTheHighBand"))
      }
    }

    "must show exemption for small producers row when yes is selected" in {

      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )

      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("contractPackedForRegisteredSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("exemptionForRegisteredSmallProducers"))
        page.getElementById("change-exemption-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-exemptions-for-small-producers"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("4000")
        page.getElementById("change-lowband-litreage-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-small-producer-details"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("6000")
        page.getElementById("change-highband-litreage-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-small-producer-details"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedForRegisteredSmallProducers.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedForRegisteredSmallProducers.highband.litres.hidden"))
      }
    }

    "must show brought into the UK row when present and answer is no" in {

      val userAnswersData = Json.obj("broughtIntoUK" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoTheUK"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUK"))
        page.getElementById("change-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk"
      }
    }

    "must show brought into the UK row containing calculation when yes is selected" in {

      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoTheUK"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUK"))
        page.getElementById("change-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-how-many-brought-into-uk"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-how-many-brought-into-uk"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUK.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUK.highband.litres.hidden"))
      }
    }

    "must show brought into the UK from small producers row when present and answer is no" in {

      val userAnswersData = Json.obj("broughtIntoUkFromSmallProducers" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoTheUKFromSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUKFromSmallProducers"))
        page.getElementById("change-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk-from-small-producers"
      }
    }

    "must show brought into the UK from small producers row containing calculation when yes is selected" in {

      val userAnswersData = Json.obj(
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoTheUKFromSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUKFromSmallProducers"))
        page.getElementById("change-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk-from-small-producers"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-how-many-into-uk-small-producers"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-how-many-into-uk-small-producers"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUKFromSmallProducers.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUKFromSmallProducers.highband.litres.hidden"))
      }
    }

    "must show claim credits for exports row when present and answer is no" in {

      val userAnswersData = Json.obj("claimCreditsForExports" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForExportedLiableDrinks"))
        page.getElementById("change-exports").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-exports"
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected" in {

      val userAnswersData = Json.obj(
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForExportedLiableDrinks"))
        page.getElementById("change-exports").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-exports"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-export-credits").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-exports"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("-£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-export-credits").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-exports"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("-£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.highband.litres.hidden"))
      }
    }

    "must show show lost or damaged row when present and answer is no" in {

      val userAnswersData = Json.obj("claimCreditsForLostDamaged" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("lostOrDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForLostOrDestroyedLiableDrinks"))
        page.getElementById("change-credits-lost-damaged").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-lost-damaged"
      }
    }

    "must show lost or damaged row containing calculation when yes is selected" in {

      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("lostOrDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForLostOrDestroyedLiableDrinks"))
        page.getElementById("change-credits-lost-damaged").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-lost-damaged"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-lost-destroyed").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-lost-damaged"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("-£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-lost-destroyed").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-lost-damaged"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("-£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.highband.litres.hidden"))
      }
    }

    "must return OK and contain amount to pay section for large producer" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(false))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 1000, "highBand" -> 2000),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (1000L, 2000L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£660.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£960.00")
        page.getElementById("cya-sub-header").text() mustEqual(Messages("youNeedToPay", "£960.00"))
      }
    }

    "must return OK and contain amount to pay header when return amount is in debit" +
      " totalForQuarter is negative and balanceBroughtForward is positive" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(false))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 10, "highBand" -> 20),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("-£6.60")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£293.40")
        page.getElementsByClass("total").text() mustNot include("-£293.40")
      }
    }

    "must return OK and contain amount to pay section for small producer" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 1000, "highBand" -> 2000),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 100, "highBand" -> 200),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 100, "highBand" -> 200),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (1000L, 2000L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£1,188.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£1,488.00")

      }
    }

    "must show submit returns section when a return is available" in {

      when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(List()))
      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("sendYourReturn"))
        page.getElementsByTag("p").text() must include(Messages("sendYourReturnConfirmation"))

        val formOnPageForSubmit = page.getElementsByTag("form")
        formOnPageForSubmit.first().attr("action") mustEqual s"$baseUrl/check-your-answers/nil-return/true"
        formOnPageForSubmit.first().getElementsByTag("button").first().text() must include(Messages("confirmDetailsAndSendReturn"))
        page.getElementById("print-page-cya").text() must include(Messages("site.print"))
      }
    }

    "must return OK and contain you do not need to pay anything when return amount is 0" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(List()))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L,0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L,0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("youDoNotNeedToPayAnything"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£0.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£0.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£0.00")

      }
    }

    "must return OK and contain amount you will be credited header when return amount is in credit" +
      " and both totalForQuarter and balanceBroughtForward are negative (on page)" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(100))
      val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(200))
      val financialItemList = List(financialItem1, financialItem2)
      when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(financialItemList))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 100, "highBand" -> 100),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual (Messages("amountYouWillBeCredited"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("-£42.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("-£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("-£342.00")
        page.getElementById("cya-sub-header").text() mustEqual(Messages("yourSoftDrinksLevyAccountsWillBeCredited", "£342.00"))

      }
    }

    "must return OK and contain amount you will be credited header when return amount is in credit" +
      " and totalForQuarter is positive and balanceBroughtForward is negative (on page)" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(100))
      val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(200))
      val financialItemList = List(financialItem1, financialItem2)
      when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(financialItemList))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 100),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual (Messages("amountYouWillBeCredited"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£42.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("-£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("-£258.00")
        page.getElementById("cya-sub-header").text() mustEqual(Messages("yourSoftDrinksLevyAccountsWillBeCredited", "£258.00"))
      }
    }

    "must return OK and contain amount you will be credited header when return amount is in credit" +
      " and totalForQuarter is negative and balanceBroughtForward is positive (on page)" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(-100))
      val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(-200))
      val financialItemList = List(financialItem1, financialItem2)
      when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(financialItemList))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual (Messages("amountYouWillBeCredited"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("-£420.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("-£120.00")
        page.getElementById("cya-sub-header").text() mustEqual(Messages("yourSoftDrinksLevyAccountsWillBeCredited", "£120.00"))

      }
    }

    "must return OK and contain amount to pay header when return amount is in debit" +
      " totalForQuarter is positive and balanceBroughtForward is negative (on page)" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(100))
      val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(200))
      val financialItemList = List(financialItem1, financialItem2)
      when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(financialItemList))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000),
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("referenceNumber" -> "XZSDIL000000235", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual (Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("-£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£3,900.00")
        page.getElementsByClass("total").text() mustNot include("-£3,900.00")

      }
    }

    "must return OK and contain registered UK sites section header when packaging site present" in {

      val userAnswersData = Json.obj("packAtBusinessAddress" -> true)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("registeredUkSites").text mustEqual Messages("registeredUkSites")
        page.getElementsByTag("dt").text() must include(Messages("packagingSites"))
      }
    }

    "must return OK and contain registered UK sites section header when packaging site not present" in {
      val userAnswersData = Json.obj("packAtBusinessAddress" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.body.text() mustNot include(Messages("registeredUkSites"))
        page.getElementsByTag("dt").text() mustNot include(Messages("packagingSites"))
      }
    }

    "must return OK and contain company alias with 0 total when there is no activity to report (nilReturn)" in {

      val application = applicationBuilder(Some(bareBoneUserAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2023"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.noActivityToReport().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must throw an exception when no return period is available when constructing the page" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers), None).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        route(application, request).value
      }

      intercept[RuntimeException](
        result mustBe an[RuntimeException]
      )
    }

    "must call balance instead of balanceHistory when balanceAllEnabled config is set to false" in {
      when(mockConfig.balanceAllEnabled).thenReturn(false)
      val application = applicationBuilder(Some(bareBoneUserAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must log error when fails on caching the amounts calculated" in {
      val secondMockSdilSessionCache = mock[SDILSessionCache]
      when(secondMockSdilSessionCache.save(any(),any(),any())(any())).thenReturn(Future.successful(CacheMap("",Map())))
      val application = applicationBuilder(Some(bareBoneUserAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(secondMockSdilSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        route(application, request).value
      }

      verify(secondMockSdilSessionCache, times(1)).save(any(),any(),any())(any())
      intercept[RuntimeException](
        result mustBe an[RuntimeException]
      )
    }

    "must log error when fails on resolving one of main future calls" in {
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(None)
      val application = applicationBuilder(Some(bareBoneUserAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        route(application, request).value
      }

      intercept[RuntimeException](
        result mustBe an[RuntimeException]
      )
    }

    "must log error when exception thrown on resolving one of main future calls" in {
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.failed(new RuntimeException(""))
      val application = applicationBuilder(Some(bareBoneUserAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        route(application, request).value
      }

      intercept[RuntimeException](
        result mustBe an[RuntimeException]
      )
    }

    "must not include brought into uk from small producers in the calculation and should display cost as 0 for those" in {
      when(mockSdilConnector.balance(any(), any())(any())).thenReturn(Future.successful(BigDecimal(0)))
      when(mockConfig.balanceAllEnabled).thenReturn(false)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(false))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000),
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000),
        "exemptionsForSmallProducers" -> false,
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000),
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 444, "highBand" -> 444),
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£12,600.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£0.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£12,600.00")

        page.getElementsByTag("dd").text() must not include("£79.92")
        page.getElementsByTag("dd").text() must not include("£106.56")

      }
    }
  }
  "Check your Answers Controller onSubmit" - {
    "should redirect to Returns controller with nil return true" in {
      val userAnswersData = Json.obj()
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(nilReturn = true).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe routes.ReturnsController.onPageLoad(true).url
      }
    }
    "should redirect to Returns controller with nil return false" in {
      val userAnswersData = Json.obj()
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), defaultReturnPeriod).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockConfig),
        bind[SDILSessionCache].toInstance(mockSDILSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(nilReturn = false).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe routes.ReturnsController.onPageLoad(false).url
      }
    }
  }
}
