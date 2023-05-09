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

import models.alf.init._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsValue, Json}

class JourneyConfigSpec extends AnyFreeSpec with Matchers {

  "model" - {
    "should write to json" in {
      val model = JourneyConfig(
        version = 1,
        options = JourneyOptions(continueUrl = "url",
          homeNavHref = Some("home"),
        signOutHref = Some("sign"),
      accessibilityFooterUrl = Some("footer"),
      phaseFeedbackLink = Some("feedback"),
      deskProServiceName = Some("deskpro"),
      showPhaseBanner = Some(true),
      alphaPhase = Some(true),
      showBackButtons = Some(true),
      disableTranslations = Some(true),
      includeHMRCBranding = Some(true),
      ukMode = Some(true),
      allowedCountryCodes = Some(Set("")),
      selectPageConfig = Some(SelectPageConfig(
        proposalListLimit = Some(1),
        showSearchAgainLink = Some(true)
      )),
      confirmPageConfig = Some(ConfirmPageConfig(
        showSearchAgainLink = Some(true),
        showSubHeadingAndInfo = Some(true),
        showChangeLink = Some(true),
        showConfirmChangeText = Some(true)
      )),
      timeoutConfig = Some(TimeoutConfig(
        timeoutAmount = 1,
        timeoutUrl = "foo",
        timeoutKeepAliveUrl = Some("bar")
      )),
      serviceHref = Some("href"),
      pageHeadingStyle = Some("heading")),
        labels = None,
        requestedVersion = Some(1)
      )
      val res: JsValue =
        Json.parse("""{"version":1,"options":{"continueUrl":"url","homeNavHref":"home","signOutHref":"sign","accessibilityFooterUrl":"footer","phaseFeedbackLink":"feedback","deskProServiceName":"deskpro","showPhaseBanner":true,"alphaPhase":true,"showBackButtons":true,"disableTranslations":true,"includeHMRCBranding":true,"ukMode":true,"allowedCountryCodes":[""],"selectPageConfig":{"proposalListLimit":1,"showSearchAgainLink":true},"confirmPageConfig":{"showSearchAgainLink":true,"showSubHeadingAndInfo":true,"showChangeLink":true,"showConfirmChangeText":true},"timeoutConfig":{"timeoutAmount":1,"timeoutUrl":"foo","timeoutKeepAliveUrl":"bar"},"serviceHref":"href","pageHeadingStyle":"heading"},"requestedVersion":1}""")
      Json.toJson(model) mustBe res

    }
  }
}
