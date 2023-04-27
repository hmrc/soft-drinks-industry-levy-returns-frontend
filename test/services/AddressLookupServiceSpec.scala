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

package services

import base.SpecBase
import connectors.AddressLookupConnector
import org.mockito.MockitoSugar.{mock, when}
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

class AddressLookupServiceSpec extends SpecBase {

  implicit val hc = HeaderCarrier()



  val mockSdilConnector = mock[AddressLookupConnector]

  val service = new AddressLookupService(mockSdilConnector)

  "getAddress" - {
    "return a address" in {
      when(mockSdilConnector.getAddress("123456789")(hc,implicitly)).thenReturn(Future.successful(Right(customerAddressMax)))

      val res = service.getAddress("123456789")

      whenReady(res) {result =>
        result mustBe Right(customerAddressMax)
      }
    }
  }

}
