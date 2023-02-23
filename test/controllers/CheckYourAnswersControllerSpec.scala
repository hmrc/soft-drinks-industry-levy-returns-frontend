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
import models.requests.IdentifierRequest
import models.{ReturnPeriod, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{BrandsPackagedAtOwnSitesPage, OwnBrandsPage}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfter {

  "Check Your Answers Controller" - {

    val bareBoneUserAnswers = UserAnswers(sdilNumber, Json.obj(), List())

    "must return OK and contain company alias and return correct description for period 0 in grey pre header" in {
      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2022, quarter = 0))).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("firstQuarter")} 2022"
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
      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2022, quarter = 1))).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("secondQuarter")} 2022"
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
      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2022, quarter = 2))).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("thirdQuarter")} 2022"
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
      val application = applicationBuilder(Some(bareBoneUserAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).build()
      val expectedPreHeader = s"${aSubscription.orgName} - ${Messages("fourthQuarter")} 2022"
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
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("ownBrandsPackagedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingOwnBrandsPackagedAtYourOwnSite"))
      }
    }

    "must show own brands packaged at own site row containing calculation when yes is selected" in {
      val userAnswersData = Json.obj(
        "ownBrands" -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand"-> 10000 , "highBand"-> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).build()

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
        page.getElementById("change-lowband-literage").attributes().get("href") mustEqual s"$baseUrl/change-how-many-own-brands-packaged-at-own-sites"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1800")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-literage").attributes().get("href") mustEqual s"$baseUrl/change-how-many-own-brands-packaged-at-own-sites"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4800")

      }
    }

    "must show packaged contract packer row when present and answer is no" in {
      val userAnswersData = Json.obj("packagedContractPacker" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).build()
      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text() must include(Messages("contractPackedAtYourOwnSite"))
        page.getElementsByTag("dt").text() must include(Messages("reportingContractPackedAtYourOwnSite"))

      }
    }

    "must show packaged contract packer row containing calculation when yes is selected" in {
      val userAnswersData = Json.obj(
        "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand"-> 10000 , "highBand"-> 20000)
      )
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(ReturnPeriod(year = 2022, quarter = 3))).build()
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
        page.getElementById("change-lowband-literage").attributes().get("href") mustEqual s"$baseUrl/change-how-many-packaged-as-contract-packer"
        page.getElementsByTag("dt").text() must include(Messages("lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1800")

        page.getElementsByTag("dt").text() must include(Messages("litresInTheHighBand"))
        page.getElementsByTag("dd").text() must include("20000")
        page.getElementById("change-highband-literage").attributes().get("href") mustEqual s"$baseUrl/change-how-many-packaged-as-contract-packer"
        page.getElementsByTag("dt").text() must include(Messages("highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4800")
      }
    }

  }
}
