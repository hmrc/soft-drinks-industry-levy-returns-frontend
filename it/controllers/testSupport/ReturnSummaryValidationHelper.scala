package controllers.testSupport

import models.Amounts
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.TryValues
import play.api.i18n.Messages
import utilitlies.CurrencyFormatter

trait ReturnSummaryValidationHelper extends Specifications
  with TestConfiguration
  with ITCoreTestData
  with TryValues {

  val headingIds = Array(
    "ownBrandsPackagedAtYourOwnSite",
    "contractPackedAtYourOwnSite",
    "contractPackedForRegisteredSmallProducers",
    "broughtIntoUK",
    "broughtIntoTheUKFromSmallProducers",
    "exported",
    "lostOrDestroyed",
    "amount-to-pay-title"
  )

  val summaryKeys = Array(
    "reportingOwnBrandsPackagedAtYourOwnSite",
    "reportingContractPackedAtYourOwnSite",
    "exemptionForRegisteredSmallProducers",
    "reportingLiableDrinksBroughtIntoTheUK",
    "reportingLiableDrinksBroughtIntoTheUKFromSmallProducers",
    "claimingCreditForExportedLiableDrinks",
    "claimingCreditForLostOrDestroyedLiableDrinks",
    "totalThisQuarter"
  )

  def validateSummaryRowsPresentWithAllNo(details: Element, amounts: Amounts, isCheckAnswers: Boolean = false) = {
    val summaryLists = details.getElementsByClass("govuk-summary-list")
    summaryLists.size() mustBe summaryKeys.size
    summaryKeys.zipWithIndex.foreach { case (summaryKey, index) =>
      val summaryRows = summaryLists.get(index).getElementsByClass("govuk-summary-list__row")
      if (headingIds(index) == "amount-to-pay-title") {
        val heading = details.getElementById("amount-to-pay-title").text()
        testAmountToPaySummary(summaryRows, heading, amounts)
      } else {
        details.getElementById(headingIds(index)).text() mustBe Messages(headingIds(index))
        summaryRows.size() mustBe 1
        summaryRows.get(0).text() must include(Messages(summaryKey))
        summaryRows.get(0).text() must include("No")
        summaryRows.get(0).getElementsByClass("govuk-link").size > 0  mustBe isCheckAnswers
      }
    }
  }

  def validateSummaryRowsPresentWithAllYes(details: Element, amounts: Amounts, isCheckAnswers: Boolean = false) = {
    val summaryLists = details.getElementsByClass("govuk-summary-list")
    summaryLists.size() mustBe summaryKeys.size + 1
    summaryKeys.zipWithIndex.foreach { case (summaryKey, index) =>
      val summaryRows = summaryLists.get(index).getElementsByClass("govuk-summary-list__row")
      if (headingIds(index) == "amount-to-pay-title") {
        val heading = details.getElementById("amount-to-pay-title").text()
        testAmountToPaySummary(summaryRows, heading, amounts)
      } else {
        details.getElementById(headingIds(index)).text() mustBe Messages(headingIds(index))
        summaryRows.size() mustBe 5
        val (expectedLowlevy, expectedHighLevy) = if(List("exemptionForRegisteredSmallProducers", "reportingLiableDrinksBroughtIntoTheUKFromSmallProducers").contains(summaryKey)) {
          ("£0.00", "£0.00")
        } else {
          if(List("claimingCreditForExportedLiableDrinks", "claimingCreditForLostOrDestroyedLiableDrinks").contains(summaryKey)) {
            ("-£180.00", "-£240.00")
          } else {
            ("£180.00", "£240.00")
          }
        }
        summaryRows.get(0).text() must include(Messages(summaryKey))
        summaryRows.get(0).text() must include("Yes")
        summaryRows.get(1).text() must include("1000")
        summaryRows.get(2).text() must include(expectedLowlevy)
        summaryRows.get(3).text() must include("1000")
        summaryRows.get(4).text() must include(expectedHighLevy)
        summaryRows.get(0).getElementsByClass("govuk-link").size > 0  mustBe isCheckAnswers
      }
    }
  }

  def testAmountToPaySummary(summaryRows: Elements, heading: String, amounts: Amounts) = {
    val expectedHeading = if (amounts.total > 0) {
      Messages("amountToPay")
    } else if (amounts.total < 0) {
      Messages("amountYouWillBeCredited")
    } else {
      Messages("youDoNotNeedToPayAnything")
    }
    heading mustBe expectedHeading
    summaryRows.size() mustBe 3
    summaryRows.get(0).text() must include("Total this quarter")
    summaryRows.get(0).text() must include(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.totalForQuarter))
    summaryRows.get(1).text() must include("Balance brought forward")
    summaryRows.get(1).text() must include(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.balanceBroughtForward * -1))
    summaryRows.get(2).text() must include("Total")
    summaryRows.get(2).text() must include(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total))
  }

}
