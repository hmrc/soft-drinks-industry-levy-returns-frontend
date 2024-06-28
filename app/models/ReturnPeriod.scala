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

import play.api.libs.json.{ Json, OFormat }

import java.time.LocalDate

case class ReturnPeriod(year: Int, quarter: Int) {
  require(quarter <= 3 && quarter >= 0)
  require(year >= 2018)

  def start: LocalDate = {
    val dayOfWeek = if (year == 2018 && quarter == 1) {
      5
    } else {
      1
    }
    LocalDate.of(year, quarter * 3 + 1, dayOfWeek)
  }

  def end: LocalDate = next.start.minusDays(1)

  def deadline: LocalDate = end.plusDays(30)

  def next: ReturnPeriod = {
    val nextReturnQuarter = (quarter + 1) % 4
    val nextReturnYear = if (nextReturnQuarter < quarter) {
      year + 1
    } else {
      year
    }
    ReturnPeriod(nextReturnYear, nextReturnQuarter)
  }
}

object ReturnPeriod {
  def apply(date: LocalDate): ReturnPeriod = ReturnPeriod(date.getYear, quarter(date))
  def quarter(date: LocalDate): Int = { date.getMonthValue - 1 } / 3
  implicit val format: OFormat[ReturnPeriod] = Json.format[ReturnPeriod]
}