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

package pages

import controllers.routes
import models.{CheckMode, NormalMode}
import pages.behaviours.PageBehaviours

class BroughtIntoUKPageSpec extends PageBehaviours {

  "BroughtIntoUKPage" - {

    beRetrievable[Boolean](BroughtIntoUKPage)

    beSettable[Boolean](BroughtIntoUKPage)

    beRemovable[Boolean](BroughtIntoUKPage)

    "should contain the correct url" - {
      "when in NormalMode" in {
        BroughtIntoUKPage.url(NormalMode) mustBe routes.BroughtIntoUKController.onPageLoad(NormalMode).url
      }

      "when in CheckMode" in {
        BroughtIntoUKPage.url(CheckMode) mustBe routes.BroughtIntoUKController.onPageLoad(CheckMode).url
      }
    }
  }
}
