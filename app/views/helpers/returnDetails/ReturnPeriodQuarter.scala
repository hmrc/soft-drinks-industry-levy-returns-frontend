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

package views.helpers.returnDetails

import models.ReturnPeriod
import play.api.i18n.Messages

object ReturnPeriodQuarter {

  def formatted(returnPeriod: ReturnPeriod)(implicit messages: Messages): String = {
    returnPeriod.quarter match {
      case 0 => s"${Messages("firstQuarter")} ${returnPeriod.year}"
      case 1 => s"${Messages("secondQuarter")} ${returnPeriod.year}"
      case 2 => s"${Messages("thirdQuarter")} ${returnPeriod.year}"
      case 3 => s"${Messages("fourthQuarter")} ${returnPeriod.year}"
    }
  }
}
