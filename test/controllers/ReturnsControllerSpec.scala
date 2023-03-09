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

import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.model.headers.Date
import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.{FinancialLineItem, ReturnPeriod, SmallProducer, Unknown, UserAnswers}
import play.api.inject.bind
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class ReturnsControllerSpec extends SpecBase {

  "ReturnSent Controller" - {

    "must show own brands row with answer no, when answered" in {
      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)

      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)

      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))

      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand"-> 1000 , "highBand"-> 1000)
      )
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 1000 , "highBand"-> 2000)
      )

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()


      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exemptionsForSmallProducers"))
        page.getElementsByTag("dt").text() must include(Messages("exemptionsForSmallProducers.checkYourAnswersLabel"))
      }
    }

    "must show brought into uk with answer no, when answered" in {
      val userAnswersData = Json.obj("broughtIntoUK" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000)
      )

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()


      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000)
      )

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.broughtIntoUkSmallProducer"))
        page.getElementsByTag("dt").text() must include(Messages("broughtIntoUKFromSmallProducers.checkYourAnswersLabel"))

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

    "must show claim credits for exports row when present and answer is no" in {
      val userAnswersData = Json.obj("claimCreditsForExports" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()


      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimCreditsForExports.checkYourAnswersLabel"))
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected" in {
      val userAnswersData = Json.obj(
        "claimCreditsForExports" -> true,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.exported"))
        page.getElementsByTag("dt").text() must include(Messages("claimCreditsForExports.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£-1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£-4,800.00")
      }
    }

    "must show show lost or damaged row when present and answer is no" in {
      val userAnswersData = Json.obj("claimCreditsForLostDamaged" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()


      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
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
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.lostDestroyed"))
        page.getElementsByTag("dt").text() must include(Messages("claimCreditsForLostDamaged.checkYourAnswersLabel"))

        page.getElementsByTag("dt").text() must include(Messages("litresInTheLowBand"))
        page.getElementsByTag("dd").text() must include("10000")
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include(" £-1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£-4,800.00")
      }
    }

    "must show producer added when a producer has been added" in {
      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true,
        "addASmallProducer" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List(sparkyJuice, superCola))

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 1000)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()



      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("returnSent.RegisteredSites"))
      }
    }

    "must show correct message to user when nothing is owed " in {
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 0 , "highBand"-> 0)
      )

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 0)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amountDue" ).text must include(Messages("checkYourAnswers.noPayNeeded.title"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£0.00")
      }
    }

    "must show correct message to user when user is owed funding" in {
      val userAnswersData = Json.obj(
        "claimCreditsForLostDamaged" -> true,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 0)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amountDue" ).text must include(Messages("checkYourAnswers.creditedPay.title"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include(" -£6,600.00")
      }
    }

    "must show correct message to user when user owes funds" in {
      val userAnswersData = Json.obj(
        "broughtIntoUK" -> true,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10000, "highBand" -> 20000)
      )


      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 0)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amountDue" ).text must include(Messages("checkYourAnswers.amountToPay.title"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£6,600.00")
      }
    }

    "must show correct total for quarter " in {
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 1000 , "highBand"-> 2000)
      )

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 0)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amountDue" ).text must include(Messages("checkYourAnswers.amountToPay.title"))
        page.getElementsByTag("dt").text() must include(Messages("totalThisQuarter.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£660.00")
      }
    }

    "must show correct balance brought forward" in {
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 1000 , "highBand"-> 2000)
      )

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 0)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amountDue" ).text must include(Messages("checkYourAnswers.amountToPay.title"))
        page.getElementsByTag("dt").text() must include(Messages("balanceBroughtForward.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£0")
      }
    }

    "must show correct total" in {

      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 1000 , "highBand"-> 2000)
      )

      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())

      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))
      when(mockSdilConnector.balance(any(), any())(any())) thenReturn Future.successful(0)
      val financialLineItem: FinancialLineItem = Unknown(LocalDate.now, "someTitle", 0)
      when(mockSdilConnector.balanceHistory(any(), any())(any())) thenReturn Future.successful(List(financialLineItem))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))

      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("amountDue" ).text must include(Messages("checkYourAnswers.amountToPay.title"))
        page.getElementsByTag("dt").text() must include(Messages("total.checkYourAnswersLabel"))
        page.getElementsByTag("dd").text() must include("£660.00")
      }
    }

  }
}
