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

package views

import models.UserAnswers
import org.jsoup.nodes.Element
import play.api.i18n.Messages

trait ReturnDetailsSummaryRowTestHelper extends ViewSpecHelper with ReturnDetailsExpectedResults {

  object Selectors {
    val heading = "govuk-heading-l"
    val subHeading = "govuk-heading-m"
    val caption = "govuk-caption-l"
    val bodyMargin5 = "govuk-body govuk-!-margin-bottom-5"
    val summaryList = "govuk-summary-list"
    val summaryListRow = "govuk-summary-list__row"
    val summaryListKey = "govuk-summary-list__key"
    val summaryListValue = "govuk-summary-list__value  align-right"
    val hidden = "govuk-visually-hidden"
    val link = "govuk-link"
    val panel = "govuk-panel govuk-panel--confirmation"
    val panel_title = "govuk-panel__title"
    val panel_body = "govuk-panel__body"
    val bulletList = "govuk-list govuk-list--bullet"
    val details = "govuk-details"
    val detailsText = "govuk-details__summary-text"
    val bodyM = "govuk-body-m"
    val body = "govuk-body"
    val button = "govuk-button"
  }



  def testSummaryLists(key: String, element: Element, userAnswers: UserAnswers, isCheckAnswers: Boolean) = {
    val summaryLists = element.getElementsByClass(Selectors.summaryList)

    "should contain the expected number of summaryLists" in {
      val expectedNumberOfSummaryListItems = if (userAnswers.packagingSiteList.nonEmpty) {
        9
      } else {
        8
      }
      summaryLists.size() mustEqual expectedNumberOfSummaryListItems
    }

    returnDetailsSummaryListsWithListNames.foreach { case (subHeaderId, listName) =>

      if (!(subHeaderId == SummaryHeadingIds.registeredSites && userAnswers.packagingSiteList.isEmpty)) {

        val arrayElementNumber = returnDetailsSummaryListsWithArrayElement(subHeaderId)
        val summaryList: Element = summaryLists.get(arrayElementNumber)
        s"should include an $listName section" - {
          testSummaryHeading(subHeaderId, element)
          if (subHeaderId == SummaryHeadingIds.registeredSites) {
            testRegisteredSites(subHeaderId, summaryList, isCheckAnswers)
          } else if(subHeaderId == SummaryHeadingIds.amountToPay) {
            testAmountToPay(summaryList)
          } else if (UserAnswersTestData.questionFieldsAllTrue(key) && UserAnswersTestData.includesNoLitres(key)) {
            testSummaryListWithYesNoAndNoLitres(subHeaderId, summaryList, "Yes", isCheckAnswers)
          } else if (UserAnswersTestData.questionFieldsAllTrue(key)) {
            testSummaryListWithYesNoAndLitres(subHeaderId, key, summaryList, userAnswers, isCheckAnswers)
          } else {
            testSummaryListWithYesNoAndNoLitres(subHeaderId, summaryList, "No", isCheckAnswers)
          }
        }
      }
    }
  }

  def testSummaryHeading(subHeadingId: String, element: Element) = {
    "that has the correct subheading" in {
      val expectedHeading = if(subHeadingId == SummaryHeadingIds.amountToPay) {
        "amountToPay"
      } else {
        subHeadingId
      }
      val elementSubHeading = element.getElementById(subHeadingId)
      elementSubHeading.className() mustEqual Selectors.subHeading
      elementSubHeading.text() mustEqual Messages(expectedHeading)
    }
  }

  def testAmountToPay(summaryList: Element) = {
    val summaryRows = summaryList.getElementsByClass(Selectors.summaryListRow)
    "that contains a summary list with 3 rows" in {
      summaryRows.size() mustEqual 3
    }
    "which includes a row for totalThisQuarter" - {
      val summaryRow = summaryRows.get(0)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "Total this quarter"
      }
      "that has the correct value" in {
        summaryRow.getElementsByTag("dd").text() mustEqual "£1,000.00"
      }
    }

    "which includes a row for balanceBroughtForward" - {
      val summaryRow = summaryRows.get(1)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "Balance brought forward"
      }
      "that has the correct value" in {
        summaryRow.getElementsByTag("dd").text() mustEqual "-£100.00"
      }
    }

    "which includes a row for Total" - {
      val summaryRow = summaryRows.get(2)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "Total"
      }
      "that has the correct value" in {
        summaryRow.getElementsByTag("dd").text() mustEqual "£1,100.00"
      }
    }
  }


  def testRegisteredSites(summarySubHeaderId: String, summaryList: Element, isCheckAnswers: Boolean) = {
    val summaryRows = summaryList.getElementsByClass(Selectors.summaryListRow)
    "that contains a summary list with 1 rows" in {
      summaryRows.size() mustEqual 1
    }
    testSummaryRowBooleanSection(summarySubHeaderId, summaryRows.get(0), "1", isCheckAnswers)
  }

  def testSummaryListWithYesNoAndNoLitres(summarySubHeaderId: String, summaryList: Element, yesNo: String, isCheckAnswers: Boolean) = {
    val summaryRows = summaryList.getElementsByClass(Selectors.summaryListRow)
    "that contains a summary list with 1 rows" in {
      summaryRows.size() mustEqual 1
    }
    testSummaryRowBooleanSection(summarySubHeaderId, summaryRows.get(0), yesNo, isCheckAnswers)
  }

  def testSummaryListWithYesNoAndLitres(summarySubHeaderId: String, key: String, summaryList: Element, userAnswers: UserAnswers, isCheckAnswers: Boolean) = {
    val summaryRows = summaryList.getElementsByClass(Selectors.summaryListRow)
    "that contains a summary list with 5 rows" in {
      summaryRows.size() mustEqual 5
    }
    testSummaryRowBooleanSection(summarySubHeaderId, summaryRows.get(0), "Yes", isCheckAnswers)

    "which includes a summary row for lowBand" - {
      val summaryRow = summaryRows.get(1)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "Litres in the low band"
      }
      "that has the correct value" in {
        summaryRow.getElementsByClass(Selectors.summaryListValue).text() mustEqual lowLitresValue(key, summarySubHeaderId, userAnswers)
      }
      if (isCheckAnswers) {
        "that contains the correct change link" in {
          val actionId = s"change-lowband-litreage-${returnDetailsSummaryListsWithLitresActionId(summarySubHeaderId)}"
          val element = summaryRow.getElementById(actionId)
          element.className() mustEqual Selectors.link
          val expectedHiddenKey = Messages(s"${returnDetailsSummaryListsWithLitresHiddenKey(summarySubHeaderId)}.lowband.litres.hidden")
          element.text() mustEqual s"Change $expectedHiddenKey"
          element.attr("href") must include(returnDetailsSummaryListsWithActionHrefsForLitres(summarySubHeaderId))
          element.getElementsByClass(Selectors.hidden).text() mustEqual expectedHiddenKey
        }
      } else {
        "that does not contain a change link" in {
          summaryRow.getElementsByClass(Selectors.link).size() mustBe 0
        }
      }
    }

    "which includes a summary row for lowBandLevy" - {
      val summaryRow = summaryRows.get(2)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "Low band levy"
      }
      "that has the correct value" in {
        summaryRow.getElementsByClass(Selectors.summaryListValue).text() mustEqual lowLevyValue(key, summarySubHeaderId, userAnswers)
      }
    }

    "which includes a summary row for highBand" - {
      val summaryRow = summaryRows.get(3)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "Litres in the high band"
      }
      "that has the correct value" in {
        summaryRow.getElementsByClass(Selectors.summaryListValue).text() mustEqual highLitresValue(key, summarySubHeaderId, userAnswers)
      }
      if (isCheckAnswers) {
        "that contains the correct change link" in {
          val actionId = s"change-highband-litreage-${returnDetailsSummaryListsWithLitresActionId(summarySubHeaderId)}"
          val element = summaryRow.getElementById(actionId)
          element.className() mustBe Selectors.link
          val expectedHiddenKey = Messages(s"${returnDetailsSummaryListsWithLitresHiddenKey(summarySubHeaderId)}.highband.litres.hidden")
          element.text() mustEqual s"Change $expectedHiddenKey"
          element.attr("href") must include(returnDetailsSummaryListsWithActionHrefsForLitres(summarySubHeaderId))
        }
      } else {
        "that does not contain a change link" in {
          summaryRow.getElementsByClass(Selectors.link).size() mustBe 0
        }
      }
    }

    "which includes a summary row for highBandLevy" - {
      val summaryRow = summaryRows.get(4)
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual "High band levy"
      }
      "that has the correct value" in {
        summaryRow.getElementsByClass(Selectors.summaryListValue).text() mustEqual highLevyValue(key, summarySubHeaderId, userAnswers)
      }
    }
  }

  private def testSummaryRowBooleanSection(summarySubHeaderId: String,
                                         summaryRow: Element,
                                         expectedValue: String,
                                         isCheckAnswers: Boolean
                                        ) = {
    s"which includes a summary row for ${returnDetailsSummaryListsWithListNames(summarySubHeaderId)}" - {
      "that has the correct key" in {
        summaryRow.getElementsByClass(Selectors.summaryListKey).text() mustEqual Messages(returnDetailsSummaryListsWithQuestionKey(summarySubHeaderId))
      }
      "that has the correct value" in {
        summaryRow.getElementsByClass(Selectors.summaryListValue).text() mustEqual expectedValue
      }
      if (isCheckAnswers) {
        "that contains the correct change link" in {
          val element = summaryRow.getElementById(returnDetailsSummaryListsWithActionIds(summarySubHeaderId))
          element.className() mustEqual Selectors.link
          val expectedHiddenKey = Messages(s"${returnDetailsSummaryListsWithActionHiddenKey(summarySubHeaderId)}.change.hidden")
          element.text() mustEqual s"Change $expectedHiddenKey"
          element.attr("href") must include(returnDetailsSummaryListsWithActionHrefsForQuestion(summarySubHeaderId))
        }
      } else {
        "that does not contain a change link" in {
          summaryRow.getElementsByClass(Selectors.link).size() mustBe 0
        }
      }
    }
  }

  private def lowLitresValue(key: String, summaryId: String, userAnswers: UserAnswers): String = {
    if (UserAnswersTestData.litresDefaultToZero(key, "lowband")) {
      "0"
    } else if (summaryId == SummaryHeadingIds.contractPackedForRegisteredSmallProducers) {
      userAnswers.smallProducerList.map(_.litreage._1).sum.toString
    } else {
      "1000"
    }
  }

  private def highLitresValue(key: String, summaryId: String, userAnswers: UserAnswers): String = {
    if (UserAnswersTestData.litresDefaultToZero(key, "highband")) {
      "0"
    } else if (summaryId == SummaryHeadingIds.contractPackedForRegisteredSmallProducers) {
      userAnswers.smallProducerList.map(_.litreage._2).sum.toString
    } else {
      "1000"
    }
  }

  private def lowLevyValue(key: String, summaryId: String, userAnswers: UserAnswers): String = {
    if (UserAnswersTestData.litresDefaultToZero(key, "lowband") ||
      summaryId == SummaryHeadingIds.broughtIntoTheUKFromSmallProducers) {
      "£0.00"
    } else if (isNegativeLevy(summaryId)) {
      "-£180.00"
    } else if (summaryId == SummaryHeadingIds.contractPackedForRegisteredSmallProducers) {
      userAnswers.smallProducerList.map(_.litreage._1).sum match {
        case 0 => "£0.00"
        case 1000 => "£180.00"
        case _ => "£540.00"
      }
    } else {
      "£180.00"
    }
  }

  private def highLevyValue(key: String, summaryId: String, userAnswers: UserAnswers): String = {
    if (UserAnswersTestData.litresDefaultToZero(key, "highband") ||
      List(SummaryHeadingIds.broughtIntoTheUKFromSmallProducers, SummaryHeadingIds.contractPackedForRegisteredSmallProducers).contains(summaryId)) {
      "£0.00"
    } else if (isNegativeLevy(summaryId)) {
      "-£240.00"
    } else if(summaryId == SummaryHeadingIds.contractPackedForRegisteredSmallProducers) {
      userAnswers.smallProducerList.map(_.litreage._2).sum match {
        case 0 => "£0.00"
        case 2000 => "£480.00"
        case _ => "£720.00"
      }
    } else {
      "£240.00"
    }
  }
}
