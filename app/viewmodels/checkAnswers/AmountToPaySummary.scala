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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.{BrandsPackagedAtOwnSitesPage, HowManyAsAContractPackerPage, HowManyBroughtIntoTheUKFromSmallProducersPage, HowManyBroughtIntoUkPage, HowManyCreditsForExportPage, HowManyCreditsForLostDamagedPage, Page, QuestionPage}
import play.api.i18n.Messages
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountToPaySummary  {

  def totalForQuarterRow(answers: UserAnswers,
                         lowBandCostPerLitre: BigDecimal,
                         highBandCostPerLitre: BigDecimal,
                         smallProducer: Boolean)(implicit messages: Messages): SummaryListRow = {

    val totalForQuarter = calculateTotalForQuarter(answers, lowBandCostPerLitre, highBandCostPerLitre, smallProducer)
    val formattedTotalForQuarter = "£" + String.format("%.2f", totalForQuarter.toDouble)
    
    SummaryListRowViewModel(
      key = "totalThisQuarter",
      value = ValueViewModel(HtmlContent(formattedTotalForQuarter)).withCssClass("total-for-quarter"),
      actions = Seq()
    )
  }


//  def balanceBroughtForwardRow(answers: UserAnswers, lowBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {
//
//    answers.get(HowManyCreditsForExportPage).map {
//      answer =>
//        val levy = "-£" + String.format("%.2f", (answer.lowBand * lowBandCostPerLitre.toDouble))
//        val value = HtmlFormat.escape(levy).toString
//
//        SummaryListRowViewModel(
//          key = "lowBandLevy",
//          value = ValueViewModel(HtmlContent(value)),
//          actions = Seq()
//        )
//    }
//  }
//
//  def totalRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
//    answers.get(HowManyCreditsForExportPage).map {
//      answer =>
//        val value = HtmlFormat.escape(answer.highBand.toString).toString + "<br/>"
//
//        SummaryListRow(
//          key = "litresInTheHighBand",
//          value = ValueViewModel(HtmlContent(value)),
//          classes = "govuk-summary-list__row--no-border",
//          actions = Some(
//            Actions("",
//              items =
//                Seq(
//                  ActionItemViewModel("site.change", routes.HowManyCreditsForExportController.onPageLoad(CheckMode).url)
//                    .withAttribute("id", "change-highband-export-credits")
//                    .withVisuallyHiddenText(messages("exported.highband.hidden"))
//                )))
//        )
//    }

  private def calculateTotalForQuarter(userAnswers: UserAnswers,
                                       lowBandCostPerLitre: BigDecimal,
                                       highBandCostPerLitre: BigDecimal,
                                       smallProducer: Boolean) = {

    calculateLowBandTotalForQuarter(userAnswers, lowBandCostPerLitre, smallProducer) +
    calculateHighBandTotalForQuarter(userAnswers, highBandCostPerLitre, smallProducer)
  }

  private def calculateLowBandTotalForQuarter(userAnswers: UserAnswers, lowBandCostPerLitre: BigDecimal, smallProducer: Boolean): BigDecimal = {

    val total = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L) +
      userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L)

    val totalCredits = userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L) +
      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L)

    if(!smallProducer) {
      val ownBrands = userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L)
      (total + ownBrands - totalCredits) * lowBandCostPerLitre
    } else
      (total - totalCredits) * lowBandCostPerLitre
  }

  private def calculateHighBandTotalForQuarter(userAnswers: UserAnswers, highBandCostPerLitre: BigDecimal, smallProducer: Boolean): BigDecimal = {

    val total = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L) +
      userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L)

    val totalCredits =
      userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L) +
      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L)

    if (!smallProducer){
      val ownBrands = userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L)
      (total + ownBrands - totalCredits) * highBandCostPerLitre
    }
    else
      (total - totalCredits) * highBandCostPerLitre
  }
}

