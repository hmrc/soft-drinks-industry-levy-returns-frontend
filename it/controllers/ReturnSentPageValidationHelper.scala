/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import controllers.testSupport.ReturnSummaryValidationHelper
import org.jsoup.nodes.Document

trait ReturnSentPageValidationHelper extends ReturnSummaryValidationHelper {
  def validatePanelForReturnSent(page: Document) = {
    val panel = page.getElementsByClass("govuk-panel govuk-panel--confirmation").get(0)
    panel.getElementsByClass("govuk-panel__title").text() mustEqual "Return sent"
    panel.getElementsByClass("govuk-panel__body").text() mustEqual "Soft Drinks Industry Levy return April to June 2018 for the Super Lemonade Plc"
  }

  def validateWhatNextSection(page: Document, amountsToPay: Option[String] = None, amountCredit: Option[String] = None) = {
    val expectedText = (amountsToPay, amountCredit) match {
      case (Some(amount), _) => s"You need to pay $amount by 30 July 2018."
      case (_, Some(_)) =>
        "You do not need to do anything else." +
          " We will take the payment from your Soft Drinks Industry Levy account shortly."
      case _ => "You do not need to do anything else. Your next return"
    }

    val body = page.getElementById("whatNextText")
    body.text() must include(expectedText)
  }

}
