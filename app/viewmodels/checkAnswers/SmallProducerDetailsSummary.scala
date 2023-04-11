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

import config.FrontendAppConfig
import controllers.routes
import models.{CheckMode, SmallProducer, UserAnswers}
import pages.SmallProducerDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import utilitlies.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._


object SmallProducerDetailsSummary  {

  def rows(answers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    Seq(
      returnsLowBandRow(answers),
      returnsLowBandLevyRow(answers, config.lowerBandCostPerLitre),
      returnsHighBandRow(answers),
      returnsHighBandLevyRow(answers, config.higherBandCostPerLitre)
    ).flatten
  }

  def producerList(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(SmallProducerDetailsPage).map {
      _ =>
        val value = answers.smallProducerList.length.toString
        SummaryListRow(
          key     = "smallProducerDetails.producerList.checkYourAnswersLabel",
          value   = ValueViewModel(value)
        )
    }
  }

  def returnsLowBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(SmallProducerDetailsPage).map {
      _ =>
        val value = HtmlFormat.escape(answers.smallProducerList.map(lowBand => lowBand.litreage._1).sum.toString)
        SummaryListRow(
          key = "litresInTheLowBand",
          value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
          classes = "govuk-summary-list__row--no-border",
        )
    }
  }

  def returnsLowBandLevyRow(answers: UserAnswers, lowBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(SmallProducerDetailsPage).map {
      _ =>
        val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(0)).toString
        SummaryListRowViewModel(
          key = "lowBandLevy",
          value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
          actions = Seq()
        )
    }
  }

  def returnsHighBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(SmallProducerDetailsPage).map {
      _ =>
        val value = HtmlFormat.escape(answers.smallProducerList.map(highBand => highBand.litreage._2).sum.toString) + "<br/>"
        SummaryListRow(
          key = "litresInTheHighBand",
          value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
          classes = "govuk-summary-list__row--no-border"
        )
    }
  }

  def returnsHighBandLevyRow(answers: UserAnswers, highBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SmallProducerDetailsPage).map {
      _ =>
        val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(0)).toString
        SummaryListRowViewModel(
          key = "highBandLevy",
          value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
          actions = Seq()
        )
    }

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SmallProducerDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "smallProducerDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value).withCssClass("align-right"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.SmallProducerDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("smallProducerDetails.change.hidden"))
          )
        )
    }

  def row2(smallProducersList: List[SmallProducer])(implicit messages: Messages): List[SummaryListRow] = {
    smallProducersList.map {
    smallProducer =>
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(smallProducer.alias)
        )
      )
      SummaryListRowViewModel(
        key     = smallProducer.sdilRef,
        value   = value,
        actions = Seq(
          ActionItemViewModel("site.edit", routes.AddASmallProducerController.onEditPageLoad(smallProducer.sdilRef).url)
            .withVisuallyHiddenText(messages("smallProducerDetails.edit.hidden")),
          ActionItemViewModel("site.remove", routes.RemoveSmallProducerConfirmController.onPageLoad(smallProducer.sdilRef).url)
            .withVisuallyHiddenText(messages("smallProducerDetails.remove.hidden"))
        )
      )
    }
  }

  def lowBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val litreageTotal = answers.smallProducerList.map(smallProducer => smallProducer.litreage._1).sum
    Some(SummaryListRow(
      key = "litresInTheLowBand",
      value = ValueViewModel(HtmlContent(litreageTotal.toString)).withCssClass("align-right"),
      classes = "govuk-summary-list__row--no-border",
      actions = Some(Actions("",
        items =
          Seq(
            ActionItemViewModel("site.change", routes.SmallProducerDetailsController.onPageLoad(CheckMode).url)
              .withAttribute(("id", "change-lowband-litreage-small-producers"))
              .withVisuallyHiddenText(messages("contractPackedForRegisteredSmallProducers.lowband.hidden"))
          )))))
  }

  def lowBandLevyRow(answers: UserAnswers, lowBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {

    val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(0)).toString

    Some(SummaryListRowViewModel(
      key = "lowBandLevy",
      value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
      actions = Seq()
    ))
  }

  def highBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val litreageTotal = answers.smallProducerList.map(smallProducer => smallProducer.litreage._2).sum
    Some(SummaryListRow(
      key = "litresInTheHighBand",
      value = ValueViewModel(HtmlContent(litreageTotal.toString)).withCssClass("align-right"),
      classes = "govuk-summary-list__row--no-border",
      actions = Some(Actions("",
        items =
          Seq(
            ActionItemViewModel("site.change", routes.SmallProducerDetailsController.onPageLoad(CheckMode).url)
              .withAttribute(("id", "change-highband-litreage-small-producers"))
              .withVisuallyHiddenText(messages("contractPackedForRegisteredSmallProducers.highband.hidden"))
          )))))
  }

  def highBandLevyRow(answers: UserAnswers, highBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {

    val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(0)).toString

    Some(SummaryListRowViewModel(
      key = "highBandLevy",
      value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
      actions = Seq()
    ))
  }


}
