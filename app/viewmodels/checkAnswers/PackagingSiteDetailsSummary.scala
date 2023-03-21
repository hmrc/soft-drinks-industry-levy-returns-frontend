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
import models.backend.Site
import models.{CheckMode, UserAnswers}
import pages.PackagingSiteDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import scala.language.postfixOps

object PackagingSiteDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PackagingSiteDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "packagingSiteDetails.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("packagingSiteDetails.change.hidden"))
          )
        )
    }


  def row2(packagingSiteList: List[Site])(implicit messages: Messages): List[SummaryListRow] = {
    packagingSiteList.map {
      site =>
        ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(site.address.lines.toString())
          )
        )
        SummaryListRow(
          key = Key(
            content =
              HtmlContent(addressFormatting(site)),
            classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds"
          ),
          actions = if (packagingSiteList.length > 1) {
            Some(Actions("", Seq(
              ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url) //TODO
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden")),
              ActionItemViewModel("site.remove", routes.IndexController.onPageLoad().url) //TODO
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden"))
            )))
          } else {
            Some(Actions("", Seq(
              ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url) //TODO
                .withVisuallyHiddenText(messages("packagingSiteDetails.edit.hidden"))
            )))
          }
        )
    }
  }

  private def addressFormatting(site: Site)(implicit messages: Messages): String = {
    val commaFormattedSiteAddress = site.address.lines.map(line => { if (line.isEmpty) "" else line + ", " })

    val addressNoTradingName = {
      s"""${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    val addressWithTradingName = {
      s"""${HtmlFormat.escape(site.tradingName.get)}<br>${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}
      ${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    val separatePostCodeAddressNoTradingName = {
      s"""${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}<br>${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    val separatePostCodeAddressWithTradingName = {
      s"""${HtmlFormat.escape(site.tradingName.get)}<br>${
        HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))
      }<br>${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    val addressFormat = determineAddressFormat(site)

    addressFormat match {
      case "separatePostCodeAddressNoTradingName" => separatePostCodeAddressNoTradingName
      case "addressNoTradingName" => addressNoTradingName
      case "addressWithTradingName" => addressWithTradingName
      case "separatePostCodeAddressWithTradingName" => separatePostCodeAddressWithTradingName
    }
  }

  private def determineAddressFormat(site: Site) = {
    val addressLength = site.address.lines.toString().length
    if (site.tradingName.get.isEmpty) {
      if ((addressLength > 41 && addressLength < 47) || (addressLength > 97 && addressLength < 104)) {
        "separatePostCodeAddressNoTradingName"
      } else {
        "addressNoTradingName"
      }
    } else {
      if ((addressLength < 42 || addressLength > 46) && (addressLength < 98 || addressLength > 105)) {
        "addressWithTradingName"
      } else {
        "separatePostCodeAddressWithTradingName"
      }
    }
  }
}
