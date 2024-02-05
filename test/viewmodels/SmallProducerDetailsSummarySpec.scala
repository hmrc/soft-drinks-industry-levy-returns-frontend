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

package viewmodels

import base.ReturnsTestData.emptyUserAnswers
import base.SpecBase
import models.{ CheckMode, NormalMode, SmallProducer }
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.helpers.returnDetails.SmallProducerDetailsSummary

class SmallProducerDetailsSummarySpec extends SpecBase {
  val smallProducerList: List[SmallProducer] = List(SmallProducer("Super Cola Plc", "XCSDIL000000069", (20, 10)))
  lazy val smallProducerListWithTwoProducers: List[SmallProducer] = List(
    SmallProducer("Super Cola Plc", "XCSDIL000000069", (20, 10)),
    SmallProducer("Soft Juice", "XMSDIL000000113", (25, 80)))
  lazy val smallProducerListWithThreeProducers: List[SmallProducer] = List(
    SmallProducer("Super Cola Plc", "XCSDIL000000069", (20, 10)),
    SmallProducer("", "XMSDIL000000159", (15, 800)),
    SmallProducer("Soft Juice", "XMSDIL000000113", (25, 80)))

  "row" - {

    "should return nothing when the list is empty" in {
      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(NormalMode, List.empty)

      smallProducerDetailsSummaryRow mustBe SummaryList(List(), None, "", Map())
    }

    "should return a summary list row with the correct information and action links" in {
      val userAnswersWithSmallProducers = emptyUserAnswers.copy(smallProducerList = smallProducerList)

      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(NormalMode, userAnswersWithSmallProducers.smallProducerList)
      val rowActionListItems = smallProducerDetailsSummaryRow.rows.head.actions.toList.head.items

      smallProducerDetailsSummaryRow.rows.head.key.content.asHtml.toString mustBe "XCSDIL000000069"
      smallProducerDetailsSummaryRow.rows.head.value.content.asHtml.toString mustBe "Super Cola Plc"
      rowActionListItems.size mustBe 2
      rowActionListItems.head.content.asHtml.toString mustBe "Edit"
      rowActionListItems.last.content.asHtml.toString mustBe "Remove"
    }
  }

  "row should contain the same number of rows as the number of small producers" - {
    "should have 2 rows" in {
      val userAnswersWith2SmallProducers = emptyUserAnswers.copy(smallProducerList = smallProducerListWithTwoProducers)
      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(NormalMode, userAnswersWith2SmallProducers.smallProducerList)

      smallProducerDetailsSummaryRow.rows.size mustBe 2
    }

    "should have 1 row" in {
      val userAnswersWithSmallProducers = emptyUserAnswers.copy(smallProducerList = smallProducerList)
      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(NormalMode, userAnswersWithSmallProducers.smallProducerList)

      smallProducerDetailsSummaryRow.rows.size mustBe 1
    }

    "should have 3 rows" in {
      val userAnswersWith3SmallProducers = emptyUserAnswers.copy(smallProducerList = smallProducerListWithThreeProducers)
      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(NormalMode, userAnswersWith3SmallProducers.smallProducerList)

      smallProducerDetailsSummaryRow.rows.size mustBe 3
    }

    "should have 3 rows in check mode " in {
      val userAnswersWith3SmallProducers = emptyUserAnswers.copy(smallProducerList = smallProducerListWithThreeProducers)
      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(CheckMode, userAnswersWith3SmallProducers.smallProducerList)

      smallProducerDetailsSummaryRow.rows.size mustBe 3
    }
  }

}
