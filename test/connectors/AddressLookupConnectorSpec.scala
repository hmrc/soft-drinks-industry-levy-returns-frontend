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

import base.SpecBase
import connectors.httpParsers.ResponseHttpParser.HttpResult
import mocks.MockHttp
import models.AlfResponse
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddressLookupConnectorSpec extends SpecBase with MockitoSugar with MockHttp {

  val errorModel: HttpResponse = HttpResponse(Status.BAD_REQUEST, "Error Message")
  val TestAddressLookupConnector = new AddressLookupConnector(mockHttp,frontendAppConfig)
  val addressLookupConnector = new AddressLookupConnector(http =mockHttp, frontendAppConfig)
  implicit val hc = HeaderCarrier()

  "AddressLookupConnector" - {

    "format the getAddressUrl correctly for" - {
      "calling getCustomerDetailsUrl" in {
        val testUrl = TestAddressLookupConnector.getAddressUrl(id)
        testUrl mustEqual s"${frontendAppConfig.addressLookupService}/api/confirmed?id=$id"
      }
    }

    "for getAddress method" - {

      def getAddressResult: Future[HttpResult[AlfResponse]] = TestAddressLookupConnector.getAddress(id)(implicitly,implicitly)

        "return a AlfResponse Model" in {
          setupMockHttpGet(TestAddressLookupConnector.getAddressUrl(id))(Right(customerAddressMax))
          await(getAddressResult) mustBe Right(customerAddressMax)
        }

      "given an error should" - {

        "return an Left with an ErrorModel" in {
          setupMockHttpGet(TestAddressLookupConnector.getAddressUrl(id))(Left(errorModel))
          await(getAddressResult) mustBe Left(errorModel)
        }
      }
    }
  }
}