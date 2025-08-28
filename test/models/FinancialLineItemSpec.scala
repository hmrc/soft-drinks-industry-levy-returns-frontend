/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDate

class FinancialLineItemSpec extends SpecBase {

  "ReturnCharge" - {
    "should return the correct description" in {
      val period = ReturnPeriod(year = 2023, quarter = 0)
      val charge = ReturnCharge(period, BigDecimal(100))
      charge.messageKey mustBe "Return for January to March 2023"
    }
  }

  "ReturnChargeInterest" - {
    "should return the correct message key" in {
      val date = LocalDate.of(2023, 6, 1)
      val item = ReturnChargeInterest(date, BigDecimal(50))
      item.messageKey mustBe messagesProvider("financiallineitem.returnchargeinterest", "01 June 2023")
    }
  }

  "CentralAssessment" - {
    "should return the correct message key" in {
      val item = CentralAssessment(LocalDate.of(2023, 7, 1), BigDecimal(200))
      item.messageKey mustBe messagesProvider("financiallineitem.centralassessment")
    }
  }

  "CentralAsstInterest" - {
    "should return the correct message key" in {
      val item = CentralAsstInterest(LocalDate.of(2023, 8, 1), BigDecimal(25))
      item.messageKey mustBe messagesProvider("financiallineitem.centralasstinterest")
    }
  }

  "OfficerAssessment" - {
    "should return the correct message key" in {
      val item = OfficerAssessment(LocalDate.of(2023, 9, 1), BigDecimal(300))
      item.messageKey mustBe messagesProvider("financiallineitem.officerassessment")
    }
  }

  "OfficerAsstInterest" - {
    "should return the correct message key" in {
      val item = OfficerAsstInterest(LocalDate.of(2023, 10, 1), BigDecimal(35))
      item.messageKey mustBe messagesProvider("financiallineitem.officerasstinterest")
    }
  }

  "PaymentOnAccount" - {
    "should return the correct message key" in {
      val item = PaymentOnAccount(LocalDate.of(2023, 11, 1), "REF123", BigDecimal(400))
      item.messageKey mustBe messagesProvider("financiallineitem.paymentonaccount", "REF123")
    }
  }

  "Unknown" - {
    "should return the title as message key" in {
      val item = Unknown(LocalDate.of(2023, 12, 1), "Some unknown title", BigDecimal(10))
      item.messageKey mustBe "Some unknown title"
    }
  }

  "FinancialLineItem JSON formatting" - {
    "should serialize and deserialize all subtypes correctly" in {
      val items: Seq[FinancialLineItem] = Seq(
        ReturnCharge(ReturnPeriod(2023, 1), BigDecimal(100)),
        ReturnChargeInterest(LocalDate.of(2023, 6, 1), BigDecimal(50)),
        CentralAssessment(LocalDate.of(2023, 7, 1), BigDecimal(200)),
        CentralAsstInterest(LocalDate.of(2023, 8, 1), BigDecimal(25)),
        OfficerAssessment(LocalDate.of(2023, 9, 1), BigDecimal(300)),
        OfficerAsstInterest(LocalDate.of(2023, 10, 1), BigDecimal(35)),
        PaymentOnAccount(LocalDate.of(2023, 11, 1), "POA123", BigDecimal(400)),
        Unknown(LocalDate.of(2023, 12, 1), "Some unknown title", BigDecimal(10)))

      items.foreach { item =>
        val json = Json.toJson(item)
        val parsed = json.as[FinancialLineItem]
        parsed mustBe item
      }
    }
  }
}
