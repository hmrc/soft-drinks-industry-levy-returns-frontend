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

package models.alf

import models.alf.init.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*

class LanguageLabelsWritesSpec extends AnyWordSpec with Matchers {

  "LanguageLabels JSON writes" should {

    "serialize an entirely empty LanguageLabels to an empty JSON object" in {
      val json = Json.toJson(LanguageLabels())
      json shouldBe Json.obj()
      (json \ "appLevelLabels").toOption shouldBe None
      (json \ "selectPageLabels").toOption shouldBe None
      (json \ "lookupPageLabels").toOption shouldBe None
      (json \ "editPageLabels").toOption shouldBe None
      (json \ "confirmPageLabels").toOption shouldBe None
      (json \ "countryPickerLabels").toOption shouldBe None
    }

    "serialize a single nested section and omit its None fields" in {
      val labels = LanguageLabels(
        appLevelLabels = Some(AppLevelLabels(
          navTitle = Some("Service name"),
          phaseBannerHtml = None)))

      val json = Json.toJson(labels)
      json.as[JsObject].keys should contain only "appLevelLabels"

      val app = (json \ "appLevelLabels").as[JsObject]
      app.keys should contain only "navTitle"
      (app \ "navTitle").as[String] shouldBe "Service name"
      (app \ "phaseBannerHtml").toOption shouldBe None
    }

    "serialize SelectPageLabels with a subset of fields" in {
      val labels = LanguageLabels(
        selectPageLabels = Some(SelectPageLabels(
          title = Some("Choose an address"),
          heading = Some("Select an address"),
          submitLabel = Some("Continue"))))

      val json = Json.toJson(labels)
      val sel = (json \ "selectPageLabels").as[JsObject]

      (sel \ "title").as[String] shouldBe "Choose an address"
      (sel \ "heading").as[String] shouldBe "Select an address"
      (sel \ "submitLabel").as[String] shouldBe "Continue"

      (sel \ "headingWithPostcode").toOption shouldBe None
      (sel \ "proposalListLabel").toOption shouldBe None
      (sel \ "searchAgainLinkText").toOption shouldBe None
      (sel \ "editAddressLinkText").toOption shouldBe None
    }

    "include an empty object when a nested section is provided as Some(empty)" in {
      val labels = LanguageLabels(appLevelLabels = Some(AppLevelLabels()))
      val json = Json.toJson(labels)

      (json \ "appLevelLabels").toOption should not be empty
      (json \ "appLevelLabels").as[JsObject].keys shouldBe empty
    }

    "serialize a rich nested structure and omit None fields across the tree" in {
      val labels = LanguageLabels(
        appLevelLabels = Some(AppLevelLabels(
          navTitle = Some("My Service"),
          phaseBannerHtml = Some("Beta – give feedback"))),
        lookupPageLabels = Some(LookupPageLabels(
          title = Some("Find address"),
          heading = Some("What's your address?"),
          afterHeadingText = None,
          filterLabel = Some("Filter"),
          postcodeLabel = Some("Postcode"),
          submitLabel = Some("Find"),
          noResultsFoundMessage = Some("No results"),
          resultLimitExceededMessage = None,
          manualAddressLinkText = Some("Enter address manually"))),
        editPageLabels = Some(EditPageLabels(
          title = Some("Edit address"),
          heading = Some("Check and edit"),
          line1Label = Some("Address line 1"),
          postcodeLabel = Some("Postcode"),
          submitLabel = Some("Save"))),
        confirmPageLabels = Some(ConfirmPageLabels(
          title = Some("Confirm address"),
          heading = Some("Is this your address?"),
          infoMessage = Some("We will use this to contact you"),
          submitLabel = Some("Confirm"))),
        countryPickerLabels = Some(CountryPickerPageLabels(
          title = Some("Choose country"),
          heading = Some("Select your country"),
          countryLabel = Some("Country"),
          submitLabel = Some("Continue"))))

      val json = Json.toJson(labels).as[JsObject]

      json.keys should contain allOf ("appLevelLabels", "lookupPageLabels", "editPageLabels", "confirmPageLabels", "countryPickerLabels")
      json.keys should not contain "selectPageLabels"

      (json \ "appLevelLabels" \ "navTitle").as[String] shouldBe "My Service"
      (json \ "lookupPageLabels" \ "manualAddressLinkText").as[String] shouldBe "Enter address manually"
      (json \ "lookupPageLabels" \ "afterHeadingText").toOption shouldBe None
      (json \ "editPageLabels" \ "line1Label").as[String] shouldBe "Address line 1"
      (json \ "confirmPageLabels" \ "infoMessage").as[String] shouldBe "We will use this to contact you"
      (json \ "countryPickerLabels" \ "submitLabel").as[String] shouldBe "Continue"
    }
  }
}