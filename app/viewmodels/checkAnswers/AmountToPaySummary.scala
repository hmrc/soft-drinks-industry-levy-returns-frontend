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

import models.UserAnswers
import pages._
import play.api.i18n.Messages
import play.api.libs.json.Format.GenericFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountToPaySummary  {

  def amountToPayRow(answers: UserAnswers,
                         lowBandCostPerLitre: BigDecimal,
                         highBandCostPerLitre: BigDecimal,
                         smallProducer: Boolean,
                         balanceBroughtForward: BigDecimal)(implicit messages: Messages) = {

    val totalForQuarter = calculateTotalForQuarter(answers, lowBandCostPerLitre, highBandCostPerLitre, smallProducer)
    val total = totalForQuarter + balanceBroughtForward
//    println(Console.YELLOW + s"Quarter $totalForQuarter" + Console.WHITE)
//    println(Console.YELLOW + s"Forward $balanceBroughtForward" + Console.WHITE)
//    println(Console.YELLOW + s"Total $total" + Console.WHITE)

    val sectionHeader =
      if (total == 0) {
        Messages("youDoNotNeedToPayAnything")
      } else if(total < 0) {
        Messages("amountYouWillBeCredited")
      } else {
        Messages("amountToPay")
      }

    val summary = SummaryListViewModel(rows = Seq(
      SummaryListRowViewModel(
        key = "totalThisQuarter",
        value = ValueViewModel(HtmlContent(formatAmount(totalForQuarter))).withCssClass("total-for-quarter"),
        actions = Seq()
      ),
      SummaryListRowViewModel(
        key = "balanceBroughtForward",
        value = ValueViewModel(HtmlContent(formatAmount(balanceBroughtForward))).withCssClass("balance-brought-forward"),
        actions = Seq()
      ),
      SummaryListRowViewModel(
        key = "total",
        value = ValueViewModel(HtmlContent(formatAmount(total))).withCssClass("total"),
        actions = Seq()
      ))
    )

    (sectionHeader, summary)
  }

  private def formatAmount(amount: BigDecimal) = {
    if(amount <0)
      "-£" + String.format("%.2f", amount.toDouble * -1)
    else
      "£" + String.format("%.2f", amount.toDouble)
  }

  private def calculateTotalForQuarter(userAnswers: UserAnswers,
                                       lowBandCostPerLitre: BigDecimal,
                                       highBandCostPerLitre: BigDecimal,
                                       smallProducer: Boolean) = {

    calculateLowBandTotalForQuarter(userAnswers, lowBandCostPerLitre, smallProducer) +
    calculateHighBandTotalForQuarter(userAnswers, highBandCostPerLitre, smallProducer)
  }

  private def calculateLowBandTotalForQuarter(userAnswers: UserAnswers, lowBandCostPerLitre: BigDecimal, smallProducer: Boolean): BigDecimal = {
    val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L)
    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L)
    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L)
    val litresExported = userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L)
    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L)

    val total =  litresBroughtIntoTheUk + litresAsContractPacker
    val totalCredits =  litresExported + litresLostOrDamaged

    smallProducer match {
      case true => (total - totalCredits) * lowBandCostPerLitre
      case _ => (total + litresPackedAtOwnSite - totalCredits) * lowBandCostPerLitre
    }
  }

  private def calculateHighBandTotalForQuarter(userAnswers: UserAnswers, highBandCostPerLitre: BigDecimal, smallProducer: Boolean): BigDecimal = {
    val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L)
    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L)
    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L)
    val litresExported = userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L)
    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L)

    val total = litresBroughtIntoTheUk + litresAsContractPacker
    val totalCredits = litresExported + litresLostOrDamaged

    smallProducer match {
      case true => (total - totalCredits) * highBandCostPerLitre
      case _ => (total + litresPackedAtOwnSite - totalCredits) * highBandCostPerLitre
    }
  }
}

