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
import models.Address
import play.api.http.Status
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpException, HttpResponse}
import connectors.httpParsers.ResponseHttpParser.HttpResult
import mocks.MockHttp
import models.core.ErrorModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnectorSpec extends SpecBase with MockitoSugar with MockHttp {

  val errorModel: HttpResponse = HttpResponse(Status.BAD_REQUEST, "Error Message")
  val TestAddressLookupConnector = new AddressLookupConnector(mockHttp,frontendAppConfig)
  val addressLookupConnector = new AddressLookupConnector(http =mockHttp, frontendAppConfig)
  implicit val hc = HeaderCarrier()
  implicit val ec = ExecutionContext



  "AddressLookupConnector" - {

    "format the getAddressUrl correctly for" - {
      "calling getCustomerDetailsUrl" in {
        val testUrl = TestAddressLookupConnector.getAddressUrl(vrn)
        testUrl mustEqual s"${frontendAppConfig.addressLookupService}/api/confirmed?id=$vrn"
      }
    }

    "for getAddress method" - {

      def getAddressResult: Future[HttpResult[Address]] = TestAddressLookupConnector.getAddress(vrn)

      "called for a Right with CustomerDetails" - {

        "return a CustomerAddressModel" in {
          setupMockHttpGet(TestAddressLookupConnector.getAddressUrl(vrn))(Right(customerAddressMax))
          await(getAddressResult) mustBe Right(customerAddressMax)
        }
      }

      "given an error should" - {

        "return an Left with an ErrorModel" in {
          setupMockHttpGet(TestAddressLookupConnector.getAddressUrl(vrn))(Left(errorModel))
          await(getAddressResult) mustBe Left(errorModel)
        }
      }
    }
  }
}