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
import models.{Address, Mode, NormalMode, SmallProducer, Warehouse}
import org.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.math.BigDecimal

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  "CheckYourAnswers Controller" - {

    //Calculations
    val zeroSubtotal:String  = "£0.00"
    val zeroBroughtForwardTotal: String = "£0.00"
    val zeroTotal:String = "£0.00"

    val quarter: BigDecimal = 1000
    val balanceBroughtForward: BigDecimal = 1000

    //Producer
    val alias: String = "Vegan Cola"
    val returnDate: String = "July to September 2022"
    val highBand: Long = 10000L
    val lowBand: Long = 10000L
    val sdilRef:String = "XCSDIL000000069"


    //Warehouse
    val tradingName:String = "Soft Juice Ltd"
    val line1: String = "3 Prospect St"
    val line2: String = "Reading"
    val line3: String = "Berkshire"
    val line4: String = "United Kingdom"
    val postcode: String = "CT44 0DF"


    val warhouseList:List[Warehouse] = List(Warehouse(tradingName,Address(line1, line2, line3, line4, postcode)))

    val emptySmallProducerList: Option[List[SmallProducer]] = None
    val optinalSmallProducerList: Option[List[SmallProducer]] = Some(List(SmallProducer(alias, sdilRef, (highBand, lowBand))))
    val emptyWarhouseList: Option[List[Warehouse]] = None
    val optinalWarhouseList: Option[List[Warehouse]] = Some(List(Warehouse(tradingName,Address(line1, line2, line3, line4, postcode))))

    val lowBandAnswerList:List[Long] = List(0L, 0L, 0L, 0L, 0L, 0L, 0L)
    val highBandAnswerList:List[Long] = List(0L, 0L, 0L, 0L, 0L, 0L, 0L)
    val lowBandAnswerListCost:List[String] = List("£0.00", "£0.00", "£0.00", "£0.00", "£0.00", "£0.00")
    val highBandAnswerListCost:List[String] = List("£0.00", "£0.00", "£0.00", "£0.00", "£0.00", "£0.00")

    lazy val checkYourAnswersRoute = routes.CheckYourAnswersController.onPageLoad().url

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val list = SummaryListViewModel(Seq.empty)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NormalMode,
          list,
          alias,
          returnDate,
          zeroSubtotal,
          zeroBroughtForwardTotal,
          zeroTotal,
          zeroTotal,
          emptySmallProducerList,
          emptyWarhouseList,
          lowBandAnswerList,
          highBandAnswerList,
          lowBandAnswerListCost,
          highBandAnswerListCost
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
