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
import models.{ CheckMode, NormalMode }
import pages.behaviours.PageBehaviours

class OwnBrandsPageSpec extends PageBehaviours {

  "OwnBrandsPage" - {

    beRetrievable[Boolean](OwnBrandsPage)

    beSettable[Boolean](OwnBrandsPage)

    beRemovable[Boolean](OwnBrandsPage)

    "should contain the correct url" - {
      "when in NormalMode" in {
        OwnBrandsPage.url(NormalMode) mustBe routes.OwnBrandsController.onPageLoad(NormalMode).url
      }

      "when in CheckMode" in {
        OwnBrandsPage.url(CheckMode) mustBe routes.OwnBrandsController.onPageLoad(CheckMode).url
      }
    }
  }
}
