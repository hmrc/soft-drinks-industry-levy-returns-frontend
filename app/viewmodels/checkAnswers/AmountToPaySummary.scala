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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utilitlies.Utilities
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.util.Locale

object AmountToPaySummary  {

  def calculatChargeFromPages(answers: UserAnswers, lowBandCostPerLitre: BigDecimal, highBandCostPerLitre: BigDecimal): List[BigDecimal] = {

    // Page 2 HowManyAsAContractPacker
    val howManyAsAContractPackerAnswers = answers.get(HowManyAsAContractPackerPage)
    val howManyAsAContractPackerLowBand = if (answers.get(PackagedContractPackerPage) == Some(true)) {
      howManyAsAContractPackerAnswers.map(answer => answer.lowBand).getOrElse(0L)
    } else 0L
    val howManyAsAContractPackerLowBandCost = lowBandCostPerLitre * howManyAsAContractPackerLowBand * 1
    val howManyAsAContractPackerHighBand = if (answers.get(PackagedContractPackerPage) == Some(true)) {
      howManyAsAContractPackerAnswers.map(answer => answer.highBand).getOrElse(0L)
    } else 0L
    val howManyAsAContractPackerHighBandCost = highBandCostPerLitre * howManyAsAContractPackerHighBand * 1
    val howManyAsAContractPackerTotal = howManyAsAContractPackerLowBandCost + howManyAsAContractPackerHighBandCost


    val smallProducerDetailsAnswers = answers.smallProducerList
    val smallProducerDetailsLowBandCost = smallProducerDetailsAnswers.map(answer => answer.litreage._1 * lowBandCostPerLitre * 0).sum
    val smallProducerDetailsHighBandCost = smallProducerDetailsAnswers.map(answer => answer.litreage._2 * lowBandCostPerLitre * 0).sum
    val smallProducerDetailsTotal = smallProducerDetailsLowBandCost + smallProducerDetailsHighBandCost

    // Page 3 add-small-producer
    val howManyBroughtIntoUkAnswers = answers.get(HowManyBroughtIntoUkPage)
    val howManyBroughtIntoUkLowBand = if (answers.get(BroughtIntoUKPage) == Some(true)) {
      howManyBroughtIntoUkAnswers.map(answer => answer.lowBand).getOrElse(0L)
    } else 0L
    val howManyBroughtIntoUkLowBandCost = (lowBandCostPerLitre * howManyBroughtIntoUkLowBand * 1)
    val howManyBroughtIntoUkHighBand = if (answers.get(BroughtIntoUKPage) == Some(true)) {
      howManyBroughtIntoUkAnswers.map(answer => answer.highBand).getOrElse(0L)
    } else 0L
    val howManyBroughtIntoUkHighBandCost = (highBandCostPerLitre * howManyBroughtIntoUkHighBand * 1)
    val howManyBroughtIntoUkTotal = howManyBroughtIntoUkLowBandCost + howManyBroughtIntoUkHighBandCost

    val howManyBroughtIntoTheUKFromSmallProducersAnswers = answers.get(HowManyBroughtIntoTheUKFromSmallProducersPage)
    val howManyBroughtIntoTheUKFromSmallProducersLowBand = if (answers.get(BroughtIntoUkFromSmallProducersPage) == Some(true)) {
      howManyBroughtIntoTheUKFromSmallProducersAnswers.map(answer => answer.lowBand).getOrElse(0L)
    } else 0L
    val howManyBroughtIntoTheUKFromSmallProducersLowBandCost = (lowBandCostPerLitre * howManyBroughtIntoTheUKFromSmallProducersLowBand * 1)
    val howManyBroughtIntoTheUKFromSmallProducersHighBand = if (answers.get(BroughtIntoUkFromSmallProducersPage) == Some(true)) {
      howManyBroughtIntoTheUKFromSmallProducersAnswers.map(answer => answer.highBand).getOrElse(0L)
    } else 0L
    val howManyBroughtIntoTheUKFromSmallProducersHighBandCost = (highBandCostPerLitre * howManyBroughtIntoTheUKFromSmallProducersHighBand * 1)
    val howManyBroughtIntoTheUKFromSmallProducersTotal = howManyBroughtIntoTheUKFromSmallProducersLowBandCost + howManyBroughtIntoTheUKFromSmallProducersHighBandCost

    val howManyCreditsForExportAnswers = answers.get(HowManyCreditsForExportPage)
    val howManyCreditsForExportLowBand = if (answers.get(ClaimCreditsForExportsPage) == Some(true)) {
      howManyCreditsForExportAnswers.map(answer => answer.lowBand).getOrElse(0L)
    } else 0L
    val howManyCreditsForExportLowBandCost = (lowBandCostPerLitre * howManyCreditsForExportLowBand * -1)
    val howManyCreditsForExportHighBand = if (answers.get(ClaimCreditsForExportsPage) == Some(true)) {
      howManyCreditsForExportAnswers.map(answer => answer.highBand).getOrElse(0L)
    } else 0L
    val howManyCreditsForExportHighBandCost = (lowBandCostPerLitre * howManyCreditsForExportHighBand * -1)
    val howManyCreditsForExportTotal = howManyCreditsForExportLowBandCost + highBandCostPerLitre * howManyCreditsForExportHighBand * -1

    val howManyCreditsForLostDamagedAnswers = answers.get(HowManyCreditsForLostDamagedPage)
    val howManyCreditsForLostDamagedLowBand = if (answers.get(ClaimCreditsForLostDamagedPage) == Some(true)) {
      howManyCreditsForLostDamagedAnswers.map(answer => answer.lowBand).getOrElse(0L)
    } else 0L
    val howManyCreditsForLostDamagedLowBandCost = (lowBandCostPerLitre * howManyCreditsForLostDamagedLowBand * -1)
    val howManyCreditsForLostDamagedHighBand = if (answers.get(ClaimCreditsForLostDamagedPage) == Some(true)) {
      howManyCreditsForLostDamagedAnswers.map(answer => answer.highBand).getOrElse(0L)
    } else 0L
    val howManyCreditsForLostDamagedTotal = lowBandCostPerLitre * howManyCreditsForLostDamagedLowBand * -1 + highBandCostPerLitre * howManyCreditsForLostDamagedHighBand * -1

    List(
      howManyAsAContractPackerTotal,
      smallProducerDetailsTotal,
      howManyBroughtIntoUkTotal,
      howManyBroughtIntoTheUKFromSmallProducersTotal,
      howManyCreditsForExportTotal,
      howManyCreditsForLostDamagedTotal
    )
  }

  def totalThisQuarter(answers: UserAnswers, lowBandCostPerLitre: BigDecimal, highBandCostPerLitre: BigDecimal, smallProducerStatus: Boolean)(implicit messages: Messages): SummaryListRow = {
    val smallProducerAnswerListTotal = calculatChargeFromPages(answers, lowBandCostPerLitre, highBandCostPerLitre)

    def calculateSubtotal(
                           costLower: BigDecimal,
                           costHigher: BigDecimal,
                           smallProducerAnswerListTotal: List[BigDecimal]
                         ): BigDecimal = {
      if (smallProducerStatus) {
        smallProducerAnswerListTotal.sum
      } else {
        val ownBranduserAnswers = answers.get(BrandsPackagedAtOwnSitesPage)
        val lowBand = ownBranduserAnswers.map(answer => answer.lowBand).getOrElse(0L)
        val highBand = ownBranduserAnswers.map(answer => answer.highBand).getOrElse(0L)
        val ownBrandTotal = costLower * lowBand * 1 + costHigher * highBand * 1
        val largeProducerListTotal = ownBrandTotal + smallProducerAnswerListTotal.sum
        largeProducerListTotal
      }
    }

    val totalThisQuarter = Utilities.formatAmountOfMoneyWithPoundSign(calculateSubtotal(
      lowBandCostPerLitre,
      highBandCostPerLitre,
      smallProducerAnswerListTotal)): String

    SummaryListRow(
      key = "totalThisQuarter.checkYourAnswersLabel",
      value = ValueViewModel(HtmlContent(totalThisQuarter)),
      actions = None
    )

  }

  def balanceBroughtForward(balanceBroughtForward: BigDecimal)(implicit messages: Messages): SummaryListRow = {
    val formattedBalanceBroughtForward = Utilities.formatAmountOfMoneyWithPoundSign(balanceBroughtForward)
    SummaryListRow(
      key = "balanceBroughtForward.checkYourAnswersLabel",
      value = ValueViewModel(HtmlContent(formattedBalanceBroughtForward)),
      actions = None
    )
  }

  def total(answers: UserAnswers, lowBandCostPerLitre: BigDecimal, highBandCostPerLitre: BigDecimal, smallProducerStatus: Boolean, balanceBroughtForward: BigDecimal)(implicit messages: Messages): SummaryListRow = {
    val smallProducerAnswerListTotal = calculatChargeFromPages(answers, lowBandCostPerLitre, highBandCostPerLitre)

    def calculateSubtotal(
                           costLower: BigDecimal,
                           costHigher: BigDecimal,
                           smallProducerAnswerListTotal: List[BigDecimal]
                         ): BigDecimal = {
      if (smallProducerStatus) {
        smallProducerAnswerListTotal.sum
      } else {
        val ownBranduserAnswers = answers.get(BrandsPackagedAtOwnSitesPage)
        val lowBand = ownBranduserAnswers.map(answer => answer.lowBand).getOrElse(0L)
        val highBand = ownBranduserAnswers.map(answer => answer.highBand).getOrElse(0L)
        val ownBrandTotal = costLower * lowBand * 1 + costHigher * highBand * 1
        val largeProducerListTotal = ownBrandTotal + smallProducerAnswerListTotal.sum
        largeProducerListTotal
      }
    }

    val total: String = Utilities.formatAmountOfMoneyWithPoundSign(calculateSubtotal(lowBandCostPerLitre,
      highBandCostPerLitre,
      smallProducerAnswerListTotal
    ) - balanceBroughtForward)

    SummaryListRow(
      key = "total.checkYourAnswersLabel",
      value = ValueViewModel(HtmlContent(total)),
      actions = None
    )
  }

  def balance(answers: UserAnswers, lowBandCostPerLitre: BigDecimal, highBandCostPerLitre: BigDecimal, smallProducerStatus: Boolean, balanceBroughtForward: BigDecimal): BigDecimal = {
    val smallProducerAnswerListTotal = calculatChargeFromPages(answers, lowBandCostPerLitre, highBandCostPerLitre)

    def calculateSubtotal(
                           costLower: BigDecimal,
                           costHigher: BigDecimal,
                           smallProducerAnswerListTotal: List[BigDecimal]
                         ): BigDecimal = {
      if (smallProducerStatus) {
        smallProducerAnswerListTotal.sum
      } else {
        val ownBranduserAnswers = answers.get(BrandsPackagedAtOwnSitesPage)
        val lowBand = ownBranduserAnswers.map(answer => answer.lowBand).getOrElse(0L)
        val highBand = ownBranduserAnswers.map(answer => answer.highBand).getOrElse(0L)
        val ownBrandTotal = costLower * lowBand * 1 + costHigher * highBand * 1
        val largeProducerListTotal = ownBrandTotal + smallProducerAnswerListTotal.sum
        largeProducerListTotal
      }
    }

    calculateSubtotal(lowBandCostPerLitre,
      highBandCostPerLitre,
      smallProducerAnswerListTotal
    ) - balanceBroughtForward
  }

  // TODO see what can be refactored from the above into the below

  def amountToPayRow(totalForQuarter: BigDecimal,
                     balanceBroughtForward: BigDecimal,
                     total: BigDecimal)(implicit messages: Messages) = {

    val summary = amountToPaySummary(balanceBroughtForward, totalForQuarter, total)
    (sectionHeaderTitle(total), summary, amountInCredit(total))
  }

  private def amountToPaySummary(balanceBroughtForward: BigDecimal, totalForQuarter: BigDecimal, total: BigDecimal)(implicit messages: Messages) = {

    val negatedBalanceBroughtForward = balanceBroughtForward * -1

    SummaryListViewModel(rows = Seq(
      SummaryListRowViewModel(
        key = "totalThisQuarter",
        value = ValueViewModel(HtmlContent(Utilities.formatAmountOfMoneyWithPoundSign(totalForQuarter))).withCssClass("total-for-quarter align-right"),
        actions = Seq()
      ),
      SummaryListRowViewModel(
        key = "balanceBroughtForward",
        value = ValueViewModel(HtmlContent(Utilities.formatAmountOfMoneyWithPoundSign(negatedBalanceBroughtForward))).withCssClass("balance-brought-forward align-right"),
        actions = Seq()
      ),
      SummaryListRowViewModel(
        key = "total",
        value = ValueViewModel(HtmlContent(Utilities.formatAmountOfMoneyWithPoundSign(total))).withCssClass("total align-right"),
        actions = Seq()
      ))
    )
  }

  private def amountInCredit(total: BigDecimal)(implicit messages: Messages) = {
    if (total < 0)
      Some(Messages("yourSoftDrinksLevyAccountsWillBeCredited", Utilities.formatAmountOfMoneyWithPoundSign(total * -1)))
    else
      None
  }

  private def sectionHeaderTitle(total: BigDecimal)(implicit messages: Messages) = {
    if (total == 0)
      Messages("youDoNotNeedToPayAnything")
    else if (total < 0)
      Messages("amountYouWillBeCredited")
    else
      Messages("amountToPay")
  }

}

