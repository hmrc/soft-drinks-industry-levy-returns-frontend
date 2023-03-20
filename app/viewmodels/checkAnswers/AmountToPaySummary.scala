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

import models.{CheckMode, FinancialLineItem, ReturnPeriod, UserAnswers}
import pages.{BrandsPackagedAtOwnSitesPage, BroughtIntoUKPage, BroughtIntoUkFromSmallProducersPage, ClaimCreditsForExportsPage, ClaimCreditsForLostDamagedPage, HowManyAsAContractPackerPage, HowManyBroughtIntoTheUKFromSmallProducersPage, HowManyBroughtIntoUkPage, HowManyCreditsForExportPage, HowManyCreditsForLostDamagedPage, PackagedContractPackerPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}

import java.util.Locale
import scala.concurrent.{Await, Future}
import scala.math.BigDecimal
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountToPaySummary {


  val currencyFormatter =
    java.text.NumberFormat.getCurrencyInstance(Locale.UK)

  def formatAmountOfMoneyWithPoundSign(d: BigDecimal): String = {
    currencyFormatter.format(d)
  }

  def calculatChargeFromPages(answers: UserAnswers, lowBandCostPerLitre: BigDecimal, highBandCostPerLitre: BigDecimal):List[BigDecimal] = {

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

  def balanceSummary(answers: UserAnswers,
                            lowBandCostPerLitre: BigDecimal,
                            highBandCostPerLitre: BigDecimal,
                            smallProducerStatus:Boolean,
                            balanceBroughtForward:BigDecimal)(implicit messages: Messages): SummaryList = {

    val smallProducerAnswerListTotal = calculatChargeFromPages(answers, lowBandCostPerLitre, highBandCostPerLitre)

    def calculateSubtotal(
                           costLower:BigDecimal,
                           costHigher:BigDecimal,
                           smallProducerAnswerListTotal:List[BigDecimal]
                         ): BigDecimal = {
      if(smallProducerStatus){
        smallProducerAnswerListTotal.sum
      }else{
        val ownBranduserAnswers = answers.get(BrandsPackagedAtOwnSitesPage)
        val lowBand = ownBranduserAnswers.map(answer => answer.lowBand).getOrElse(0L)
        val highBand = ownBranduserAnswers.map(answer => answer.highBand).getOrElse(0L)
        val ownBrandTotal = costLower * lowBand * 1 + costHigher * highBand * 1
        val largeProducerListTotal =   ownBrandTotal + smallProducerAnswerListTotal.sum
        largeProducerListTotal
      }
    }

    val totalThisQuarter = formatAmountOfMoneyWithPoundSign(calculateSubtotal(
      lowBandCostPerLitre,
      highBandCostPerLitre,
      smallProducerAnswerListTotal)):String

    val total:String = formatAmountOfMoneyWithPoundSign(calculateSubtotal(lowBandCostPerLitre,
      highBandCostPerLitre,
      smallProducerAnswerListTotal
    ) - balanceBroughtForward)

    val formattedBalanceBroughtForward =    if(balanceBroughtForward < 0) {
      {f"+£${balanceBroughtForward.abs}%,.2f"}
    } else {
      {f"-£${balanceBroughtForward.abs}%,.2f"}
    }

    SummaryList(
      rows = Seq(
      SummaryListRow(
        key = "totalThisQuarter.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(totalThisQuarter)),
        actions = None
      ),
      SummaryListRow(
        key = "balanceBroughtForward.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(formattedBalanceBroughtForward)),
        actions = None
      ),
        SummaryListRow(
          key = "total.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(total)),
          actions = None
        )
      ))
  }

  def balance(answers: UserAnswers, lowBandCostPerLitre: BigDecimal, highBandCostPerLitre: BigDecimal, smallProducerStatus:Boolean, balanceBroughtForward:BigDecimal):BigDecimal ={
    val smallProducerAnswerListTotal = calculatChargeFromPages(answers, lowBandCostPerLitre, highBandCostPerLitre)
    def calculateSubtotal(
                           costLower:BigDecimal,
                           costHigher:BigDecimal,
                           smallProducerAnswerListTotal:List[BigDecimal]
                         ): BigDecimal = {
      if(smallProducerStatus){
        smallProducerAnswerListTotal.sum
      }else{
        val ownBranduserAnswers = answers.get(BrandsPackagedAtOwnSitesPage)
        val lowBand = ownBranduserAnswers.map(answer => answer.lowBand).getOrElse(0L)
        val highBand = ownBranduserAnswers.map(answer => answer.highBand).getOrElse(0L)
        val ownBrandTotal = costLower * lowBand * 1 + costHigher * highBand * 1
        val largeProducerListTotal =   ownBrandTotal + smallProducerAnswerListTotal.sum
        largeProducerListTotal
      }
    }
    calculateSubtotal(lowBandCostPerLitre,
      highBandCostPerLitre,
      smallProducerAnswerListTotal
    ) - balanceBroughtForward
  }
}
