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

package utilities

import base.SpecBase
import models.{ FinancialLineItem, PaymentOnAccount, ReturnCharge, ReturnChargeInterest, ReturnPeriod }
import utilitlies.ReturnsHelper
import utilitlies.ReturnsHelper.listItemsWithTotal

import java.time.LocalDate

class ReturnsHelperSpec extends SpecBase {

  val year = 2022
  val date1: LocalDate = LocalDate.of(year, 12, 1)
  val date2: LocalDate = LocalDate.of(year, 6, 1)

  val fi1: PaymentOnAccount = PaymentOnAccount(date1, "test", BigDecimal(132.00))
  val fi2: ReturnCharge = ReturnCharge(ReturnPeriod.apply(date2), BigDecimal(-120.00))
  val fi3: ReturnCharge = ReturnCharge(ReturnPeriod.apply(date1), BigDecimal(-12.00))

  "Returns Helper " - {
    val oneFinancialItem: List[FinancialLineItem] = List(fi1)
    val twoFinancialItems: List[FinancialLineItem] = List(fi1, fi2)
    val threeFinancialItems: List[FinancialLineItem] = List(fi1, fi2, fi3)
    val expectedResultsOneItem = List((fi1, 132.00))
    val expectedResultsTwoItems = List((fi2, 12.00), (fi1, 132.00))
    val expectedResultsThreeItems = List((fi3, 0.00), (fi2, 12.00), (fi1, 132.00))

    "List items with total " - {
      "should provide a running total of the line items" - {
        "when there is only 1 financial line item" in {
          val result = listItemsWithTotal(oneFinancialItem)
          result mustBe expectedResultsOneItem
        }

        "when there are two financial line items" in {
          val result = listItemsWithTotal(twoFinancialItems)
          result mustBe expectedResultsTwoItems
        }

        "when there are three financial line items" in {
          val result = listItemsWithTotal(threeFinancialItems)
          result mustBe expectedResultsThreeItems
        }
      }

      "Extract total " - {
        "should return the amount at the front of the line items with total list " in {
          val expectedResult = 132.00
          val result = ReturnsHelper.extractTotal(listItemsWithTotal(oneFinancialItem))

          val expectedResult2 = 12.00
          val result2 = ReturnsHelper.extractTotal(listItemsWithTotal(twoFinancialItems))

          result mustBe expectedResult
          result2 mustBe expectedResult2
        }

        "should return 0 if list is empty" in {
          val financialItems: List[(FinancialLineItem, BigDecimal)] = List.empty
          val result = ReturnsHelper.extractTotal(financialItems)

          result mustBe 0
        }
      }
    }
  }
}
