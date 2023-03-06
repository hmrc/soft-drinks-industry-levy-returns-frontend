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

package models.customerAddress

import config.AppConfig
import models.User
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json._

case class AddressLookupJsonBuilder(continueUrl: String)(implicit user: User[_], messagesApi: MessagesApi, config: AppConfig) {

  // general journey overrides
  val showPhaseBanner: Boolean = true
  val conf: AppConfig = config
  //  val deskproServiceName: String = conf.contactFormServiceIdentifier
  val accessibilityFooterUrl: String = conf.accessibilityReportUrl
  val deskproServiceName: String = conf.deskproServiceName
  //  val relativeTimeoutUrl: String = conf.unauthorisedSignOutUrl.replace("http://localhost:9553", "")
  val relativeTimeoutUrl: String = "/soft-drinks-industry-levv-returns-frontend/account/sign-out"

  object Version2 {

    val eng: Messages = MessagesImpl(Lang("en"), messagesApi)
    val wel: Messages = MessagesImpl(Lang("cy"), messagesApi)

    val version: Int = 2

    val navTitle: Messages => String = message => "/soft-drinks-industry-levv-returns-frontend" //ServiceNameUtil.generateHeader(user, message)

    val timeoutConfig: JsObject = Json.obj(
      "timeoutAmount" -> config.timeoutPeriod,
      "timeoutUrl" -> relativeTimeoutUrl
    )
    val selectPageLabels: Messages => JsObject = message => Json.obj(
      "title" -> message("address_lookupPage.selectPage.heading"),
      "heading" -> message("address_lookupPage.selectPage.heading"),
      "submitLabel" -> message("common.continue"),
      "editAddressLinkText" -> message("address_lookupPage.selectPage.editLink")
    )

    val lookupPageLabels: Messages => JsObject = message => Json.obj(
      "title" -> message("address_lookupPage.heading"),
      "heading" -> message("address_lookupPage.heading"),
      "filterLabel" -> message("address_lookupPage.filter"),
      "postcodeLabel" -> message("address_lookupPage.postcode"),
      "submitLabel" -> message("address_lookupPage.lookupPage.submit"),
      "manualAddressLinkText" -> message("address_lookupPage.overseas")
    )

    val confirmPageLabels: Messages => JsObject = message => Json.obj(
      "title" -> message("address_lookupPage.confirmPage.heading"),
      "heading" -> message("address_lookupPage.confirmPage.heading"),
      "showConfirmChangeText" -> false
    )

    val editPageLabels: Messages => JsObject = message => Json.obj(
      "submitLabel" -> message("common.continue"),
      "postcodeLabel" -> message("address_lookupPage.postalCode")
    )

    val phaseBannerHtml: Messages => String = message =>
      s"${message("feedback.before")}" +
        s" <a id='beta-banner-feedback' href='${conf.feedbackUrl}'>${message("feedback.link")}</a>" +
        s" ${message("feedback.after")}"
  }

}

object AddressLookupJsonBuilder {

  implicit val writes: Writes[AddressLookupJsonBuilder] = (data: AddressLookupJsonBuilder) => {
    Json.obj(fields =
      "version" -> 2,
      "options" -> Json.obj(
        "continueUrl" -> data.continueUrl,
        "accessibilityFooterUrl" -> data.accessibilityFooterUrl,
        "deskProServiceName" -> data.deskproServiceName,
        "showPhaseBanner" -> data.showPhaseBanner,
        "ukMode" -> true,
        "timeoutConfig" -> data.Version2.timeoutConfig
      ),
      "labels" -> Json.obj(
        "en" -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> data.Version2.navTitle(data.Version2.eng),
            "phaseBannerHtml" -> data.Version2.phaseBannerHtml(data.Version2.eng)
          ),
          "selectPageLabels" -> data.Version2.selectPageLabels(data.Version2.eng),
          "lookupPageLabels" -> data.Version2.lookupPageLabels(data.Version2.eng),
          "confirmPageLabels" -> data.Version2.confirmPageLabels(data.Version2.eng),
          "editPageLabels" -> data.Version2.editPageLabels(data.Version2.eng)
        )
      )
    )
  }
}
