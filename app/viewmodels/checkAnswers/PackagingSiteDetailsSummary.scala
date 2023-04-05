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
              ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden")),
              ActionItemViewModel("site.remove", routes.IndexController.onPageLoad().url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden"))
            )))
          } else {
            Some(Actions("", Seq(
              ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.edit.hidden"))
            )))
          }
        )
    }
  }

  private def addressFormatting(site: Site): String = {
    val addressFormat = determineAddressFormat(site)

    val commaFormattedSiteAddress = site.address.lines.map(line => { if (line.isEmpty) "" else line + ", " })

    lazy val addressNoTradingName = {
      s"""${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    lazy val addressWithTradingName = {
      s"""${HtmlFormat.escape(site.tradingName.getOrElse(""))}<br>${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    lazy val separatePostCodeAddressNoTradingName = {
      s"""${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}<br>${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    lazy val separatePostCodeAddressWithTradingName = {
      s"""${HtmlFormat.escape(site.tradingName.getOrElse(""))}<br>${HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))}<br>${HtmlFormat.escape(site.address.postCode)}""".stripMargin
    }

    addressFormat match {
      case SeparatePostCodeAddressNoTradingName => separatePostCodeAddressNoTradingName
      case AddressNoTradingName => addressNoTradingName
      case AddressWithTradingName => addressWithTradingName
      case SeparatePostCodeAddressWithTradingName => separatePostCodeAddressWithTradingName
    }
  }

  private def determineAddressFormat(site: Site): AddressMatching = {

    val addressLength = site.address.lines.toString().length

    if (site.tradingName.getOrElse("") == "") {
      if ((addressLength > 44 && addressLength < 50) || (addressLength > 97 && addressLength < 104)) {
        SeparatePostCodeAddressNoTradingName
      } else {
        AddressNoTradingName
      }
    } else {
      if ((addressLength > 44 && addressLength < 50) || (addressLength > 97 && addressLength < 104)) {
        SeparatePostCodeAddressWithTradingName
      } else {
        AddressWithTradingName
      }
    }
  }
}
sealed trait AddressMatching
case object SeparatePostCodeAddressNoTradingName extends AddressMatching
case object AddressNoTradingName extends AddressMatching
case object SeparatePostCodeAddressWithTradingName extends AddressMatching
case object AddressWithTradingName extends AddressMatching
