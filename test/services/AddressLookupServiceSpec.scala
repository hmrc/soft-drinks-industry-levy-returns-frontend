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
import models.{Address, UserAnswers, Warehouse}
import models.Address.extractValue
import models.backend.UkAddress
import org.mockito.MockitoSugar.{mock, when}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.AddressHelper

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

class AddressLookupServiceSpec extends SpecBase  {
  val fixedUUID:String = "12"
  val mockSdilConnector = mock[AddressLookupConnector]
  implicit val hc = HeaderCarrier()
  val service = new AddressLookupService(mockSdilConnector){
    override def generateId: String = fixedUUID
  }

  "getAddress" - {
    "return a address" in {
      when(mockSdilConnector.getAddress("123456789")(hc,implicitly)).thenReturn(Future.successful(Right(customerAddressMax)))

      val res = service.getAddress("123456789")

      whenReady(res) { result =>
        result mustBe Right(customerAddressMax)
      }
    }
  }

  "addAddressUserAnswers" - {
    "add to the cache the address of a warehouse when a user returns from address lookup frontend" in {
      val addressLookupState = Warehousedetails
      val warehouseMap = Map("1" -> Warehouse(Some("super cola"), UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")))
      val AddedWarehouse = Map("1" -> Warehouse(Some("super cola"), UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")),
        fixedUUID -> Warehouse(Some(organisation), UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode)))

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMax,
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap))

       res.warehouseList mustBe AddedWarehouse
    }

    "add to the cache the address of a warehouse when a user returns from address lookup frontend with missing address lines" in {
      val addressLookupState = Warehousedetails
      val warehouseMap = Map("1" -> Warehouse(Some("super cola"), UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")))
      val AddedWarehouseMissingLines = Map("1" -> Warehouse(Some("super cola"), UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")),
        "12" -> Warehouse(Some(organisation), UkAddress(List(addressLine1, addressLine2), postcode)))
      val customerAddressMissingLines: Address = Address(
        Some(organisation),
        Some(addressLine1),
        Some(addressLine2),
        None,
        None,
        Some(postcode),
        Some(countryCode)
      )

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap))

      res.warehouseList mustBe AddedWarehouseMissingLines
    }
  }
}
