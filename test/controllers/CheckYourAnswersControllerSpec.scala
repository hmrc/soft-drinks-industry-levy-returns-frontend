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
import controllers.actions.IdentifierAction
import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{Address, Mode, NormalMode, ReturnPeriod, SmallProducer, Warehouse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDate
import scala.concurrent.Future
import scala.math.BigDecimal

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {



  //Warehouse
  val tradingName:String = "Soft Juice Ltd"
  val line1: String = "3 Prospect St"
  val line2: String = "Reading"
  val line3: String = "Berkshire"
  val line4: String = "United Kingdom"
  val postcode: String = "CT44 0DF"
  val warhouseList:List[Warehouse] = List(Warehouse(tradingName,Address(line1, line2, line3, line4, postcode)))
  val optinalWarhouseList: Option[List[Warehouse]] = Some(List(Warehouse(tradingName,Address(line1, line2, line3, line4, postcode))))

  val lowBandAnswerList:List[Long] = List(0L, 0L, 0L, 0L, 0L, 0L, 0L)
  val highBandAnswerList:List[Long] = List(0L, 0L, 0L, 0L, 0L, 0L, 0L)
  val lowBandAnswerListCost:List[String] = List("£0.00", "£0.00", "£0.00", "£0.00", "£0.00", "£0.00")
  val highBandAnswerListCost:List[String] = List("£0.00", "£0.00", "£0.00", "£0.00", "£0.00", "£0.00")
  lazy val checkYourAnswersRoute = routes.CheckYourAnswersController.onPageLoad().url


  val quarter: String = "£0.00"
  val balanceBroughtForward: String = "£0.00"
  val total: String = "£0.00"
  val financialStatus: String = "noPayNeeded"
  val smallProducerCheck: Option[List[SmallProducer]] = None
  val warehouseCheck: Option[List[Warehouse]] = None
  val returnPeriod = ReturnPeriod(2022, 3)

  "CheckYourAnswers Controller" - {



    "must return OK and return the correct period" in {


      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      val mockIdentifierAction = mock[IdentifierAction]

      when(mockSdilConnector.oldestPendingReturnPeriod(any())(any())).thenReturn {
        Future.successful(Some(returnPeriod))
      }

      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) .thenReturn {
        Future.successful(Some(aSubscription))
      }



      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      )
        .build()


      running(application) {

        val request = FakeRequest(GET, checkYourAnswersRoute)

        val list = SummaryListViewModel(Seq.empty)
        val alias: String = "Super Lemonade Plc"
        val returnDate: String = "July to September 2022"
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          mode = NormalMode,
          list = list,
          alias = alias: String,
          returnDate = returnDate: String,
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
