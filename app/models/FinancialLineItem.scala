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

import play.api.i18n.Messages
import play.api.libs.json.{ Format, JsObject, JsResult, JsString, JsValue, Json }

import java.time.format.DateTimeFormatter
import java.time.{ LocalDate => Date }

sealed trait FinancialLineItem {
  def date: Date
  def amount: BigDecimal
  def messageKey(implicit messages: Messages): String
}

case class ReturnCharge(period: ReturnPeriod, amount: BigDecimal) extends FinancialLineItem {

  private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")
  private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
  private val fromMonth = period.start.format(monthFormatter)
  private val endPeriod = period.end.format(monthYearFormatter)
  override def messageKey(implicit messages: Messages): String =
    Messages("financialLineItem.returnCharge", fromMonth, endPeriod)
  def date: Date = period.deadline
}

object FinancialLineItem {

  implicit val formatPeriod: Format[ReturnPeriod] =
    Json.format[ReturnPeriod]

  implicit val formatter: Format[FinancialLineItem] =
    new Format[FinancialLineItem] {
      def reads(json: JsValue): JsResult[FinancialLineItem] =
        (json \ "type").as[String] match {
          case "ReturnCharge" => Json.format[ReturnCharge].reads(json)
        }

      def writes(o: FinancialLineItem): JsValue = o match {
        case i: ReturnCharge => Json.format[ReturnCharge].writes(i).as[JsObject] + ("type" -> JsString("ReturnCharge"))
      }
    }
}
