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

import base.ReturnsTestData._
import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.RequiredUserAnswers
import helpers.LoggerHelper
import models.requests.DataRequest
import models.{ Amounts, ReturnPeriod, SmallProducer }
import orchestrators.ReturnsOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfter
import pages.Page
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utilitlies.GenericLogger
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfter with LoggerHelper {

  val mockOrchestrator: ReturnsOrchestrator = mock[ReturnsOrchestrator]
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

  def withRequiredAnswersComplete(guiceApplicationBuilder: GuiceApplicationBuilder): GuiceApplicationBuilder = {
    lazy val requiredAnswers: RequiredUserAnswers = new RequiredUserAnswers(mock[GenericLogger]) {
      override def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = action
    }
    guiceApplicationBuilder.overrides(bind[RequiredUserAnswers].to(requiredAnswers))
  }

  private val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
  private val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

  "Check Your Answers Controller onPageLoad" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = withRequiredAnswersComplete(applicationBuilder(userAnswers = Some(submittedAnswers))).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = withRequiredAnswersComplete(applicationBuilder(userAnswers = Some(submittedAnswers))).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and contain company alias and return correct description for period 0 in grey pre header" in {

      val application = withRequiredAnswersComplete(applicationBuilder(Some(emptyUserAnswers.copy(returnPeriod = ReturnPeriod(year = 2022, quarter = 0))), Some(ReturnPeriod(year = 2022, quarter = 0))))
        .overrides(
          bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))

      val expectedPreHeader = s"This return is for ${aSubscription.orgName} for ${Messages("firstQuarter")} 2022"

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("cya-returnPeriod").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 1 in grey pre header" in {
      val application = withRequiredAnswersComplete(
        applicationBuilder(Some(emptyUserAnswers.copy(returnPeriod = ReturnPeriod(year = 2021, quarter = 1))), Some(ReturnPeriod(year = 2021, quarter = 1)))).overrides(
          bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))

      val expectedPreHeader = s"This return is for ${aSubscription.orgName} for ${Messages("secondQuarter")} 2021"

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("cya-returnPeriod").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 2 in grey pre header" in {

      val application = withRequiredAnswersComplete(applicationBuilder(Some(emptyUserAnswers.copy(returnPeriod = ReturnPeriod(year = 2020, quarter = 2))), Some(ReturnPeriod(year = 2020, quarter = 2)))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))

      val expectedPreHeader = s"This return is for ${aSubscription.orgName} for ${Messages("thirdQuarter")} 2020"

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("cya-returnPeriod").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 3 in grey pre header" in {

      val application = withRequiredAnswersComplete(applicationBuilder(Some(emptyUserAnswers.copy(returnPeriod = ReturnPeriod(year = 2019, quarter = 3))), Some(ReturnPeriod(year = 2019, quarter = 3)))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))

      val expectedPreHeader = s"This return is for ${aSubscription.orgName} for ${Messages("fourthQuarter")} 2019"

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("cya-returnPeriod").text() mustEqual expectedPreHeader
      }
    }

    "must redirect to journey recovery when no return period" in {

      val application = withRequiredAnswersComplete(applicationBuilder(None, None)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        route(application, request).value
      }
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
    }

    "must not show own brands packaged when user is a small producer" in {

      val application = withRequiredAnswersComplete(applicationBuilder(
        Some(emptyUserAnswers),
        Some(ReturnPeriod(year = 2019, quarter = 3)), Some(subscriptionWithSmallProducerActivity))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() mustNot include(Messages("ownBrandsPackagedAtYourOwnSite"))
        page.getElementsByTag("dt").text() mustNot include(Messages("reportingOwnBrandsPackagedAtYourOwnSite"))
      }
    }

    "must show own brands packaged at own site row when no selected" in {

      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must show own brands packaged at own site row containing calculation when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must show own brands packaged at own site row containing calculation when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-own-site").attributes().get("href") mustEqual s"$baseUrl/change-how-many-own-brands-packaged-at-own-sites"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("ownBrandsPackagedAtYourOwnSite.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("ownBrandsPackagedAtYourOwnSite.highband.litres.hidden"))
      }
    }

    "must show own brands packaged at own site row containing small calculations when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 5, "highBand" -> 3))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("dd").text() must include("5")
        page.getElementsByTag("dd").text() must include("£0.90")
        page.getElementsByTag("dd").text() must include("3")
        page.getElementsByTag("dd").text() must include("£0.72")
      }
    }

    "must show own brands packaged at own site row containing small calculations when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 5, "highBand" -> 3))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("dd").text() must include("5")
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£0.90")
        page.getElementsByTag("dd").text() must include("3")
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£0.72")
      }
    }

    "must show packaged contract packer row when present and answer is no" in {

      val userAnswersData = Json.obj("packagedContractPacker" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("contractPackedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingContractPackedAtYourOwnSite"))
        page.getElementById("change-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-packaged-as-contract-packer"
      }
    }

    "must show packaged contract packer row containing calculation when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must show packaged contract packer row containing calculation when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-how-many-packaged-as-contract-packer"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedAtYourOwnSite.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedAtYourOwnSite.highband.litres.hidden"))
      }
    }

    "must show exemption for small producers row when present and answer is no" in {

      val userAnswersData = Json.obj("exemptionsForSmallProducers" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must show exemption for small producers row when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))

      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))

      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must show exemption for small producers row when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))

      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))

      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("6000")
        page.getElementById("change-highband-litreage-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-small-producer-details"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedForRegisteredSmallProducers.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedForRegisteredSmallProducers.highband.litres.hidden"))
      }
    }

    "must show brought into the UK row when present and answer is no" in {

      val userAnswersData = Json.obj("broughtIntoUK" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoUK"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUK"))
        page.getElementById("change-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk"
      }
    }

    "must show brought into the UK row containing calculation when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoUK"))
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

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoUK.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoUK.highband.litres.hidden"))
      }
    }

    "must show brought into the UK row containing calculation when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoUK"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUK"))
        page.getElementById("change-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-litreage-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-how-many-brought-into-uk"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-how-many-brought-into-uk"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoUK.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoUK.highband.litres.hidden"))
      }
    }

    "must show brought into the UK from small producers row when present and answer is no" in {

      val userAnswersData = Json.obj("broughtIntoUkFromSmallProducers" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("broughtIntoTheUKFromSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("reportingLiableDrinksBroughtIntoTheUKFromSmallProducers"))
        page.getElementById("change-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-brought-into-uk-from-small-producers"
      }
    }

    "must show brought into the UK from small producers row containing calculation when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must show brought into the UK from small producers row containing calculation when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-how-many-into-uk-small-producers"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUKFromSmallProducers.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUKFromSmallProducers.highband.litres.hidden"))
      }
    }

    "must show claim credits for exports row when present and answer is no" in {

      val userAnswersData = Json.obj("claimCreditsForExports" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForExportedLiableDrinks"))
        page.getElementById("change-exports").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-exports"
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        page.getElementsByTag("dd").text() must include("−£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-export-credits").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-exports"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("−£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.highband.litres.hidden"))
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("−£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-export-credits").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-exports"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("−£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.highband.litres.hidden"))
      }
    }

    "must show show lost or damaged row when present and answer is no" in {

      val userAnswersData = Json.obj("claimCreditsForLostDamaged" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("lostOrDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForLostOrDestroyedLiableDrinks"))
        page.getElementById("change-credits-lost-damaged").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-lost-damaged"
      }
    }

    "must show lost or damaged row containing calculation when yes is selected - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = preApril2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        page.getElementsByTag("dd").text() must include("−£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-lost-destroyed").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-lost-damaged"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("−£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.highband.litres.hidden"))
      }
    }

    "must show lost or damaged row containing calculation when yes is selected - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, returnPeriod = taxYear2025ReturnPeriod)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("−£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-lost-destroyed").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-lost-damaged"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        //        TODO: Correct this value
        page.getElementsByTag("dd").text() must include("−£4,800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.lowband.litres.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.highband.litres.hidden"))
      }
    }

    "must return OK and contain amount to pay section for small producer - pre April 2025 rates" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 100, "highBand" -> 200))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (1000L, 2000L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = preApril2025ReturnPeriod)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£1,000.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£3,000.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£2,000.00")

      }
    }

    "must return OK and contain amount to pay section for small producer - 2025 tax year rates" in {

      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 100, "highBand" -> 200))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (1000L, 2000L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = taxYear2025ReturnPeriod)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        //        TODO: Correct this value
        page.getElementsByClass("total-for-quarter").text() must include("£1,000.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£3,000.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£2,000.00")

      }
    }

    "must show submit returns section when a return is available" in {

      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("sendYourReturn"))
        page.getElementsByTag("p").text() must include(Messages("sendYourReturnConfirmation"))

        val formOnPageForSubmit = page.getElementsByTag("form")
        formOnPageForSubmit.first().attr("action") mustEqual s"$baseUrl/check-your-answers"
        formOnPageForSubmit.first().getElementsByTag("button").first().text() must include(Messages("confirmDetailsAndSendReturn"))
        page.getElementById("print-page-cya").text() must include(Messages("site.print"))
      }
    }

    "must return OK and contain you do not need to pay anything when return amount is 0" in {
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola))
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amountsZero))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£0.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£0.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£0.00")

      }
    }

    "must return OK and contain amount to pay header when return amount - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val amounts1 = Amounts(4200, 300, 3900)
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = preApril2025ReturnPeriod)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts1))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual Messages("summary")
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("−£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£3,900.00")
        page.getElementsByClass("total").text() mustNot include("−£3,900.00")

      }
    }

    "must return OK and contain amount to pay header when return amount - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val amounts1 = Amounts(4200, 300, 3900)
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = taxYear2025ReturnPeriod)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts1))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual Messages("summary")
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        //        TODO: Correct this value
        page.getElementsByClass("total-for-quarter").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("−£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£3,900.00")
        page.getElementsByClass("total").text() mustNot include("−£3,900.00")

      }
    }

    "must return OK and contain amount owed header when total is negative - pre April 2025 rates" in {

      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))
      val amounts1 = Amounts(-4200, -300, -3900)
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = preApril2025ReturnPeriod)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts1))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual Messages("summary")
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("−£4,200.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("−£3,900.00")
      }
    }

    "must return OK and contain amount owed header when total is negative - 2025 tax year rates" in {

      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))
      val amounts1 = Amounts(-4200, -300, -3900)
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = taxYear2025ReturnPeriod)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts1))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual Messages("summary")
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        //        TODO: Correct this value
        page.getElementsByClass("total-for-quarter").text() must include("−£4,200.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("−£3,900.00")
      }
    }

    "must show packaging site section if the user is a new packer and " +
      "selected the default packaging site we presented to them during the journey" in {
        val userAnswersData = Json.obj(
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000),
          "packAtBusinessAddress" -> true)

        val userAnswers = emptyUserAnswers.copy(data = userAnswersData, packagingSiteList = packagingSiteListWith1)
        val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
          bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
        running(application) {
          when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val page = Jsoup.parse(contentAsString(result))
          page.getElementById("registeredUkSites").text mustEqual Messages("registeredUkSites")
          page.getElementsByTag("dt").text() must include(Messages("You have 1 packaging site"))
        }
      }

    "must show packaging site section if the user changed the default packaging site we presented to them during the journey" in {
      val userAnswersData = Json.obj(
        "packAtBusinessAddress" -> false,
        "packAtBusinessAddress" -> false,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000))

      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, packagingSiteList = packagingSiteListWith1)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("registeredUkSites").text mustEqual Messages("registeredUkSites")
        page.getElementsByTag("dt").text() must include(Messages("You have 1 packaging site"))
      }
    }

    "must show correct number of packaging sites if multiple packaging sites present" in {
      val userAnswers = emptyUserAnswers.copy(
        data = Json.obj(
          "packAtBusinessAddress" -> false,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000)),
        packagingSiteList = packagingSiteListWith2)

      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("registeredUkSites").text mustEqual Messages("registeredUkSites")
        page.getElementsByTag("dt").text() must include(Messages("You have 2 packaging sites"))
      }
    }

    "must show correct number of warehouses if multiple are present" in {
      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000),
        "askSecondaryWarehouseInReturn" -> true)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, packagingSiteList = packagingSiteListWith1, warehouseList = warhouseSiteListWith2)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("registeredUkSites").text mustEqual Messages("registeredUkSites")
        page.getElementsByTag("dt").text() must include(Messages("You have 2 warehouses"))
      }
    }

    "must return OK and contain registered UK sites section header when warehouse site present" in {
      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000),
        "askSecondaryWarehouseInReturn" -> true)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, warehouseList = warhouseSiteListWith1)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("registeredUkSites").text mustEqual Messages("registeredUkSites")
        page.getElementsByTag("dt").text() must include(Messages("You have 1 warehouse"))
      }
    }

    "must return OK and contain registered UK sites section header when packaging site not present" in {
      val userAnswersData = Json.obj("packAtBusinessAddress" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amounts))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.body.text() mustNot include(Messages("registeredUkSites"))
        page.getElementsByTag("dt").text() mustNot include(Messages("packagingSites"))
      }
    }

    "must return OK and contain company alias with 0 total when there is no activity to report (nilReturn)" in {

      val userAnswers = emptyUserAnswers.copy(isNilReturn = true)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      val expectedPreHeader = s"This return is for ${aSubscription.orgName} for ${Messages("thirdQuarter")} 2018"
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.successful(amountsZero))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("cya-returnPeriod").text() mustEqual expectedPreHeader
      }
    }

    "must log error and redirect to journey recovery when fails to get the amounts calculated" in {
      val application = withRequiredAnswersComplete(applicationBuilder(Some(emptyUserAnswers))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn (Future.failed(new RuntimeException("ERROR")))
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual ("ERROR")
              event.getMessage mustEqual (s"Exception occurred while retrieving SDIL data for id")
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }

  "Check your Answers Controller onSubmit" - {
    "should submit return and redirect to return sent" in {
      val userAnswersData = Json.obj()
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)
      val application = withRequiredAnswersComplete(applicationBuilder(Some(userAnswers), Some(defaultReturnsPeriod))).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()
      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)
        when(mockOrchestrator.completeReturnAndUpdateUserAnswers()(any(), any(), any())) thenReturn (Future.successful(()))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe routes.ReturnSentController.onPageLoad.url
      }
    }
  }
}
