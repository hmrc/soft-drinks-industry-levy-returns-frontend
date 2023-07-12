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

package connectors

import base.ReturnsTestData._
import base.SpecBase
import connectors.httpParsers.AddressLookupHttpParser.AddressLookupInitJourneyReads
import connectors.httpParsers.ResponseHttpParser.HttpResult
import helpers.LoggerHelper
import mocks.MockHttp
import models.alf.AlfResponse
import models.alf.init.{JourneyConfig, JourneyOptions}
import models.core.ErrorModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.Logger
import play.api.http.Status
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.HttpResponse
import utilitlies.GenericLogger

import scala.concurrent.Future

class AddressLookupConnectorSpec extends SpecBase with MockitoSugar with MockHttp {

  val errorModel: HttpResponse = HttpResponse(Status.BAD_REQUEST, "Error Message")

  val testAddressLookupConnector = new AddressLookupConnector(mockHttp, frontendAppConfig)

  lazy val id = "111111111"

  "AddressLookupConnector" - {

    "format the getAddressUrl correctly for" - {
      "calling getAddressUrl when feature switch is disabled" in {
        val testUrl = testAddressLookupConnector.getAddressUrl(id, addressLookupFrontendTestEnabled = false)
        testUrl mustEqual s"${frontendAppConfig.addressLookupService}/api/confirmed?id=$id"
      }
      "calling getAddressUrl when feature switch is enabled" in {
        val testUrl = testAddressLookupConnector.getAddressUrl(id, addressLookupFrontendTestEnabled = true)
        testUrl mustEqual s"${frontendAppConfig.returnsFrontendBaseUrl}${controllers.test.routes.AddressFrontendStubController.addresses(id).url}"
      }
    }
    "return correct initAddressUrl correctly" - {
      "when feature switch is disabled" in {
        testAddressLookupConnector.initJourneyUrl(addressLookupFrontendTestEnabled = false) mustBe s"${frontendAppConfig.addressLookupService}/api/init"
      }
      "when feature switch is enabled" in {
        testAddressLookupConnector.initJourneyUrl(addressLookupFrontendTestEnabled = true) mustBe
          s"${frontendAppConfig.returnsFrontendBaseUrl}${controllers.test.routes.AddressFrontendStubController.initialise().url}"
      }
    }

    "getAddress" - {

      def getAddressResult: Future[HttpResult[AlfResponse]] = testAddressLookupConnector.getAddress(id)(implicitly,implicitly)

        "return a AlfResponse Model" in {
          setupMockHttpGet(testAddressLookupConnector.getAddressUrl(id, addressLookupFrontendTestEnabled = true))(Right(customerAddressMax))
          await(getAddressResult) mustBe Right(customerAddressMax)
        }

      "given an error should" - {

        "return an Left with an ErrorModel" in {
          setupMockHttpGet(testAddressLookupConnector.getAddressUrl(id, addressLookupFrontendTestEnabled = true))(Left(errorModel))
          await(getAddressResult) mustBe Left(errorModel)
        }
      }
    }

    "initJourney" - {
      val journeyConfig = JourneyConfig(1, JourneyOptions(""), None, None)

      s"should return url if ${Status.ACCEPTED} returned and ${HeaderNames.LOCATION} exists" in {
        val response = AddressLookupInitJourneyReads.read("", "", HttpResponse(Status.ACCEPTED, "", Map(HeaderNames.LOCATION -> Seq("foo"))))
        setupMockHttpPost(testAddressLookupConnector.initJourneyUrl(addressLookupFrontendTestEnabled = true) )(response)
        await(testAddressLookupConnector.initJourney(journeyConfig)) mustBe response
      }

      s"return Left if ${Status.ACCEPTED} but no header exists" in {
        val response = AddressLookupInitJourneyReads.read("", "", HttpResponse(Status.ACCEPTED, "", Map.empty))
        setupMockHttpPost(testAddressLookupConnector.initJourneyUrl(addressLookupFrontendTestEnabled = true))(response)
        await(testAddressLookupConnector.initJourney(journeyConfig)) mustBe
          Left(ErrorModel(Status.ACCEPTED, s"No ${HeaderNames.LOCATION} key in response from init response from ALF"))
      }

      s"return Left if status is ${Status.BAD_REQUEST}" in {
        val response = AddressLookupInitJourneyReads.read("", "", HttpResponse(Status.BAD_REQUEST, "Error Message"))
        setupMockHttpPost(testAddressLookupConnector.initJourneyUrl(addressLookupFrontendTestEnabled = true))(response)
        await(testAddressLookupConnector.initJourney(journeyConfig)) mustBe Left(ErrorModel(Status.BAD_REQUEST, "Error Message returned from ALF"))
      }

      "return Left if status not accepted statuses from API" in {
        val response = AddressLookupInitJourneyReads.read("", "", HttpResponse(Status.INTERNAL_SERVER_ERROR, "Error Message"))
        setupMockHttpPost(testAddressLookupConnector.initJourneyUrl(addressLookupFrontendTestEnabled = true))(response)
        await(testAddressLookupConnector.initJourney(journeyConfig)) mustBe
          Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Unexpected error occurred when init journey from ALF"))
      }
    }
  }
}