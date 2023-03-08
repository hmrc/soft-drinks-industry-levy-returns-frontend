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
import connectors.SoftDrinksIndustryLevyConnector
import models.{ReturnCharge, ReturnPeriod, SmallProducer, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import org.mockito.MockitoSugar.mock

import scala.concurrent.Future


class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfter {

  val bareBoneUserAnswers = UserAnswers(sdilNumber, Json.obj(), List())
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  val returnPeriods = List(ReturnPeriod(2018, 1), ReturnPeriod(2019, 1))
  val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(100))
  val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(200))
  val financialItemList = List(financialItem1, financialItem2)

  when(mockSdilConnector.retrieveSubscription(any(), any())(any())).thenReturn(Future.successful(Some(aSubscription)))
  when(mockSdilConnector.returns_pending(any())(any())).thenReturn(Future.successful(List.empty[ReturnPeriod]))
  when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
  when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(Future.successful(financialItemList))
  when(mockSdilConnector.balance(any(), any())(any())).thenReturn(Future.successful(BigDecimal(100)))

  "Check Your Answers Controller" - {

    "must return OK and contain company alias and return correct description for period 0 in grey pre header" in {

      // TODO - Period is set to a default value in FakeIdentifierAction -
      //  Allow tests to set the period for more flexibility
      val application = applicationBuilder(Some(bareBoneUserAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2023"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 1 in grey pre header" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2023"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 2 in grey pre header" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2023"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must return OK and contain company alias and return correct description for period 3 in grey pre header" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2023"
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h1").text() mustEqual Messages("checkYourAnswers.title")
        page.getElementById("pre-header-caption").text() mustEqual expectedPreHeader
      }
    }

    "must throw and exception when return period is not returned" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers), None).build()
      val result = running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        route(application, request).value
      }

      intercept[RuntimeException](
        result mustBe an[RuntimeException]
      )
    }

    "must show own brands packaged at own site row when no selected" in {
      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
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

    "must show own brands packaged at own site row containing calculation when yes is selected" in {
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand"-> 10000 , "highBand"-> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
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
        page.getElementsByTag("dd").text() must include("£1800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-own-site").attributes().get("href") mustEqual s"$baseUrl/change-how-many-own-brands-packaged-at-own-sites"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("ownBrandsPackagedAtYourOwnSite.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("ownBrandsPackagedAtYourOwnSite.highband.hidden"))
      }
    }

    "must show own brands packaged at own site row containing small calculations when yes is selected" in {
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 5, "highBand" -> 3)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
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

    "must show packaged contract packer row when present and answer is no" in {
      val userAnswersData = Json.obj("packagedContractPacker" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
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
        page.getElementsByTag("dd").text() must include("£1800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-contract-packer").attributes().get("href") mustEqual s"$baseUrl/change-how-many-packaged-as-contract-packer"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedAtYourOwnSite.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedAtYourOwnSite.highband.hidden"))
      }
    }

    "must show exemption for small producers row when present and answer is no" in {
      val userAnswersData = Json.obj("exemptionsForSmallProducers" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
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

    "must show exemption for small producers row when yes is selected" in {
      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )

      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
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
        page.getElementsByTag("dd").text() must include("£720.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("6000")
        page.getElementById("change-highband-litreage-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-small-producer-details"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£1440.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedForRegisteredSmallProducers.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("contractPackedForRegisteredSmallProducers.highband.hidden"))
      }
    }

    "must show brought into the UK row when present and answer is no" in {
      val userAnswersData = Json.obj("broughtIntoUK" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
        page.getElementsByTag("dd").text() must include("£1800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-brought-into-uk").attributes().get("href") mustEqual s"$baseUrl/change-how-many-brought-into-uk"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUK.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUK.highband.hidden"))
      }
    }

    "must show brought into the UK from small producers row when present and answer is no" in {
      val userAnswersData = Json.obj("broughtIntoUkFromSmallProducers" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
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
        page.getElementsByTag("dd").text() must include("£1800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-litreage-brought-into-uk-small-producers").attributes().get("href") mustEqual s"$baseUrl/change-how-many-into-uk-small-producers"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUKFromSmallProducers.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("broughtIntoTheUKFromSmallProducers.highband.hidden"))
      }
    }

    "must show claim credits for exports row when present and answer is no" in {
      val userAnswersData = Json.obj("claimCreditsForExports" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForExportedLiableDrinks"))
        page.getElementById("change-exports").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-exports"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-export-credits").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-exports"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("-£1800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-export-credits").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-exports"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("-£4800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("exported.highband.hidden"))
      }
    }

    "must show show lost or damaged row when present and answer is no" in {
      val userAnswersData = Json.obj("claimCreditsForLostDamaged" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("lostOrDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForLostOrDestroyedLiableDrinks"))
        page.getElementById("change-credits-lost-damaged").attributes().get("href") mustEqual s"$baseUrl/change-claim-credits-for-lost-damaged"

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementById("change-lowband-lost-destroyed").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-lost-damaged"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("-£1800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-lost-destroyed").attributes().get("href") mustEqual s"$baseUrl/change-how-many-credits-for-lost-damaged"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("-£4800.00")

        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.lowband.hidden"))
        page.getElementsByClass("govuk-visually-hidden").text() must include(Messages("lostOrDestroyed.highband.hidden"))
      }
    }

    "must return OK and contain amount to pay section for large producer" in {
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

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      }
    }

    "must return OK and contain amount to pay section with negative quarter total" in {
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

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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
      }
    }

    "must return OK and contain amount to pay section for small producer" in {
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

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£1188.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£1488.00")

      }
    }

    "must show submit returns section when a return is available" in {
      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("sendYourReturn"))
        page.getElementsByTag("p").text() must include(Messages("sendYourReturnConfirmation"))
        page.getElementById("confirm-and-submit").text() must include(Messages("confirmDetailsAndSendReturn"))
        page.getElementById("confirm-and-submit").attributes().get("href") mustEqual s"$baseUrl/check-your-answers"
      }
    }

    "must return OK and contain you do not need to pay anything when return amount due is 0" in {
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


      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

    "must return OK and contain amount you will be credited header when return amount due is in credit" in {
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(-100))
      val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(-200))
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

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual (Messages("amountYouWillBeCredited"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£42.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("-£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£258.00")

      }
    }

    "must return OK and contain amount you will be credited header when return amount due is in credit" +
      "and both totalForQuarter and balanceBroughtForward are negative" in {
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
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 100, "highBand" -> 100),
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
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

      }
    }

    "must return OK and contain amount you will be credited header when return amount due is in debit" +
      "totalForQuarter is positive and balanceBroughtForward is negative where total is negative" in {
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(-100))
      val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(-200))
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

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text mustEqual (Messages("amountToPay"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter"))
        page.getElementsByClass("total-for-quarter").text() must include("£4200.00")
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward"))
        page.getElementsByClass("balance-brought-forward").text() must include("-£300.00")
        page.getElementsByTag("dt").text() must include(Messages("total"))
        page.getElementsByClass("total").text() must include("£3900.00")

      }
    }

  }
}
