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

package models

import base.SpecBase

import java.time.LocalDate

class ReturnPeriodSpec extends SpecBase {

  "ReturnPeriod" - {
    "start returns the start date of the quarter" in {
      val firstSdilReturnPeriod = ReturnPeriod(2018, 1)
      val quarter1 = ReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = ReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = ReturnPeriod(year = 2023, quarter = 2)
      val quarter4 = ReturnPeriod(year = 2023, quarter = 3)

      firstSdilReturnPeriod.start mustBe LocalDate.of(2018, 4, 5)
      quarter1.start mustBe LocalDate.of(2023, 1, 1)
      quarter2.start mustBe LocalDate.of(2023, 4, 1)
      quarter3.start mustBe LocalDate.of(2023, 7, 1)
      quarter4.start mustBe LocalDate.of(2023, 10, 1)
    }

    "end returns the end date of the quarter" in {
      val quarter1 = ReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = ReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = ReturnPeriod(year = 2023, quarter = 2)
      val quarter4 = ReturnPeriod(year = 2023, quarter = 3)

      quarter1.end mustBe LocalDate.of(2023, 3, 31)
      quarter2.end mustBe LocalDate.of(2023, 6, 30)
      quarter3.end mustBe LocalDate.of(2023, 9, 30)
      quarter4.end mustBe LocalDate.of(2023, 12, 31)
    }

    "deadline returns the end deadline of the quarter" in {
      val quarter1 = ReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = ReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = ReturnPeriod(year = 2023, quarter = 2)
      val quarter4 = ReturnPeriod(year = 2023, quarter = 3)

      quarter1.deadline mustBe LocalDate.of(2023, 4, 30)
      quarter2.deadline mustBe LocalDate.of(2023, 7, 30)
      quarter3.deadline mustBe LocalDate.of(2023, 10, 30)
      quarter4.deadline mustBe LocalDate.of(2024, 1, 30)
    }

    "next returns the next Return" in {
      val quarter0 = ReturnPeriod(2023, 0)
      val quarter1 = ReturnPeriod(year = 2023, quarter = 1)
      val quarter2 = ReturnPeriod(year = 2023, quarter = 2)
      val quarter3 = ReturnPeriod(year = 2023, quarter = 3)

      quarter0.next mustBe ReturnPeriod(2023, 1)
      quarter1.next mustBe ReturnPeriod(2023, 2)
      quarter2.next mustBe ReturnPeriod(2023, 3)
      quarter3.next mustBe ReturnPeriod(2024, 0)
    }
  }

}
