/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import java.time.LocalDate

import models.backend.{ Contact, Site, UkAddress }
import models.retrieved.{ RetrievedActivity, RetrievedSubscription }
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class DefaultUserAnswersDataSpec extends AnyWordSpec with Matchers {

  val dummyAddress: UkAddress = UkAddress(List("Line 1", "Line 2"), "AB1 2CD", Some("ALF123"))
  val dummySite: Site = Site(address = dummyAddress, ref = Some("REF001"), tradingName = Some("Test Site"), closureDate = None)
  val dummyContact: Contact = Contact(Some("John Smith"), Some("Director"), "01234567890", "john@example.com")
  val defaultDate: LocalDate = LocalDate.of(2023, 1, 1)

  "DefaultUserAnswersData" should {

    "serialize and deserialize correctly to/from JSON" in {
      val data = DefaultUserAnswersData(
        ownBrands = Some(true),
        packagedContractPacker = true,
        exemptionsForSmallProducers = true,
        broughtIntoUK = true,
        broughtIntoUkFromSmallProducers = true,
        claimCreditsForExports = true,
        claimCreditsForLostDamaged = true
      )

      val json = Json.toJson(data)
      val parsed = json.as[DefaultUserAnswersData]

      parsed mustBe data
    }

    "populate correctly from RetrievedSubscription with smallProducer = true" in {
      val activity = RetrievedActivity(
        smallProducer = true,
        largeProducer = false,
        contractPacker = false,
        importer = false,
        voluntaryRegistration = false
      )

      val subscription = RetrievedSubscription(
        utr = "1234567890",
        sdilRef = "XZSDIL000123456",
        orgName = "Test Org",
        address = dummyAddress,
        activity = activity,
        liabilityDate = defaultDate,
        productionSites = List(dummySite),
        warehouseSites = List(dummySite),
        contact = dummyContact,
        deregDate = None
      )

      val result = new DefaultUserAnswersData(subscription)

      result mustBe DefaultUserAnswersData(
        ownBrands = None, // because smallProducer = true
        packagedContractPacker = false,
        exemptionsForSmallProducers = false,
        broughtIntoUK = false,
        broughtIntoUkFromSmallProducers = false,
        claimCreditsForExports = false,
        claimCreditsForLostDamaged = false
      )
    }

    "populate correctly from RetrievedSubscription with smallProducer = false" in {
      val activity = RetrievedActivity(
        smallProducer = false,
        largeProducer = true,
        contractPacker = true,
        importer = true,
        voluntaryRegistration = false
      )

      val subscription = RetrievedSubscription(
        utr = "1234567890",
        sdilRef = "XZSDIL000654321",
        orgName = "Another Org",
        address = dummyAddress,
        activity = activity,
        liabilityDate = defaultDate,
        productionSites = List(dummySite),
        warehouseSites = List(dummySite),
        contact = dummyContact,
        deregDate = None
      )

      val result = new DefaultUserAnswersData(subscription)

      result mustBe DefaultUserAnswersData(
        ownBrands = Some(false), // because smallProducer = false
        packagedContractPacker = false,
        exemptionsForSmallProducers = false,
        broughtIntoUK = false,
        broughtIntoUkFromSmallProducers = false,
        claimCreditsForExports = false,
        claimCreditsForLostDamaged = false
      )
    }
  }
}
