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
import models.retrieved.RetrievedActivity
import models.{ Amounts, SmallProducer }
import orchestrators.ReturnsOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class ReturnSentControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfter {

  "ReturnSent Controller" - {

    val mockOrchestrator = mock[ReturnsOrchestrator]

    "must not show return sent page as return has not been sent" in {
      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = emptyUserAnswers.copy(returnPeriod = returnPeriod, data = userAnswersData, submitted = false)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, userAnswers.isNilReturn).url
      }

    }

    "must show own brands row with answer no, when answered" in {
      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.ownBrands"))
        page.getElementsByTag("dt").text() must include(Messages("ReportingOwnBrandsPackagedAtYourOwnSite.checkYourAnswersLabel"))
      }
    }

    "must show own brands row with answer yes, with calculation when answered" in {
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.ownBrands"))
        page.getElementsByTag("dt").text() must include(Messages("ReportingOwnBrandsPackagedAtYourOwnSite.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("1000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£180.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("1000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£240.00")
      }
    }

    "must show Contract packed at your own site with answer no, when answered" in {
      val userAnswersData = Json.obj("packagedContractPacker" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.packagedContractPacker"))
        page.getElementsByTag("dt").text() must include(Messages("reportingContractPackedAtYourOwnSite.checkYourAnswersLabel"))
      }
    }

    "must show Contract packed at your own sites row with answer yes, with calculation when answered" in {
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.packagedContractPacker"))
        page.getElementsByTag("dt").text() must include(Messages("reportingContractPackedAtYourOwnSite.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("1000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£180.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("2000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£480.00")
      }
    }

    "must show Exemptions For Small Producers row with answer no, when answered" in {
      val userAnswersData = Json.obj("exemptionsForSmallProducers" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exemptionsForSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("exemptionForRegisteredSmallProducers"))
      }
    }

    "must show brought into uk with answer no, when answered" in {
      val userAnswersData = Json.obj("broughtIntoUK" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.broughtIntoUk"))
        page.getElementsByTag("dt").text() must include(Messages("broughtIntoUK.checkYourAnswersLabel"))
      }
    }

    "must show brought into uk row with answer yes, with calculation when answered" in {
      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.broughtIntoUk"))
        page.getElementsByTag("dt").text() must include(Messages("broughtIntoUK.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("1000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£180")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("2000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£480")
      }
    }

    "must show brought into the UK from small producers row when present and answer is no" in {
      val userAnswersData = Json.obj("broughtIntoUkFromSmallProducers" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.broughtIntoUkSmallProducer"))
        page.getElementsByTag("dt").text() must include(Messages("broughtIntoUKFromSmallProducers.checkYourAnswersLabel"))
      }
    }

    "must show brought into uk small producer row with answer yes, with calculation when answered" in {
      val userAnswersData = Json.obj(
        "broughtIntoUkFromSmallProducers" -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.broughtIntoUkSmallProducer"))
        page.getElementsByTag("dt").text() must include(Messages("broughtIntoUKFromSmallProducers.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("1000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("0.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("2000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("0.00")
      }
    }

    "must show claim credits for exports row when present and answer is no" in {
      val userAnswersData = Json.obj("claimCreditsForExports" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForExportedLiableDrinks"))
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected" in {
      val userAnswersData = Json.obj(
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimingCreditForExportedLiableDrinks"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("−£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("−£4,800.00")
      }
    }

    "must show show lost or damaged row when present and answer is no" in {
      val userAnswersData = Json.obj("claimCreditsForLostDamaged" -> false)
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.lostDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimCreditsForLostDamaged.checkYourAnswersLabel"))
      }
    }

    "must show lost or damaged row containing calculation when yes is selected" in {
      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)
      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.lostDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimCreditsForLostDamaged.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("−£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("−£4,800.00")
      }
    }

    "must show producer added when a producer has been added" in {
      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000))
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), submitted = true)

      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exemptionsForSmallProducers"))
      }
    }

    "must show correct message to user when nothing is owed " in {
      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amountsZero)
      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£0.00")
      }
    }

    "must show correct message to user when user is owed funding" in {
      val amounts = Amounts(0, 0, -6600)
      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementById("amount-to-pay-title").text must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include(" −£6,600.00")
      }
    }

    "must show correct message to user when user owes funds" in {
      val amounts = Amounts(0, 0, 6600)
      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£6,600.00")
      }
    }

    "must show correct total for quarter " in {
      val amounts = Amounts(660, 0, 660)
      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£660.00")
      }
    }

    "must show correct balance brought forward" in {
      val amounts = Amounts(660, 0, 660)
      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£0.00")
        page.getElementsByTag("dd").text() must include("£660.00")
      }
    }

    "must show correct total when non nil return" in {
      val amounts = Amounts(660, 0, 660)
      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amount-to-pay-title").text must include(Messages("summary"))
        page.getElementsByTag("dt").text() must include(Messages("total.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£660.00")
      }
    }

    "must display 0 lowband and highband amounts when for own brands and small producer" in {
      val amounts = Amounts(660, 0, 660)

      val userAnswers = emptyUserAnswers.copy(submitted = true)
      val subscription = aSubscription.copy(activity = RetrievedActivity(true, true, false, false, false))
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod), Some(subscription)).overrides(
        bind[ReturnsOrchestrator].toInstance(mockOrchestrator)).build()

      when(mockOrchestrator.getCalculatedAmountsForReturnSent(any(), any(), any())(any(), any())) thenReturn Future.successful(amounts)

      running(application) {
        val request = FakeRequest(GET, routes.ReturnSentController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

  }
}