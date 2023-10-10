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

package repositories

import models.ReturnPeriod

object SDILSessionKeys {

  val SUBSCRIPTION = "SUBSCRIPTION"
  val AMOUNTS = "AMOUNTS"

  def balanceHistory(withAssessment: Boolean) = if(withAssessment) {
    "BALANCE_HISTORY_WITH_ASSESSMENT"
  } else {
    "BALANCE_HISTORY_WITH_NO_ASSESSMENT"
  }

  def balance(withAssessment: Boolean) = if (withAssessment) {
    "BALANCE_WITH_ASSESSMENT"
  } else {
    "BALANCE_WITH_NO_ASSESSMENT"
  }


  def smallProducerForPeriod(returnPeriod: ReturnPeriod) =
    s"SMALL_PRODUCER_YEAR_${returnPeriod.year}_QUARTER_${returnPeriod.quarter}"

}
