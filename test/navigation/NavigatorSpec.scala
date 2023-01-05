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

package navigation

import base.SpecBase
import controllers.routes
import pages._
import models._
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "navigate to correct page " - {

        "When current Page is " - {

          "Own brand packaged at own site page" - {

             def navigate(value: Boolean) = navigator.nextPage(OwnBrandsPage,
              NormalMode,
              UserAnswers("id", Json.obj("ownBrands" -> value)))

            "select Yes to navigate to How Many own brands packaged at own sites page" in {
              val result = navigate(true)
              result mustBe routes.BrandsPackagedAtOwnSitesController.onPageLoad(NormalMode)
            }

            "select No to navigate to packaged as contract packer page" in {
              val result = navigate(false)
              result mustBe routes.PackagedContractPackerController.onPageLoad(NormalMode)
            }
          }

          "How many own brands packaged at own sites" - {

            "select save and continue to navigate to packaged as contract packer page" in {
              val result = navigator.nextPage(BrandsPackagedAtOwnSitesPage,
                NormalMode,
                UserAnswers("id", Json.obj("ownBrands" -> true,
                  "brandsPackagedAtOwnSites" ->
                    Json.obj("lowBand" -> "100", "highBand" -> "100"))))
              result mustBe routes.PackagedContractPackerController.onPageLoad(NormalMode)
            }

          }

          "Packaged as a contract packer" - {

            def navigate(value: Boolean) = navigator.nextPage(PackagedContractPackerPage,
              NormalMode,
              UserAnswers("id", Json.obj("packagedContractPacker" -> value)))

            "select Yes to navigate to How Many packaged as contract packer" in {
              val result = navigate(true)
              result mustBe routes.HowManyAsAContractPackerController.onPageLoad(NormalMode)
            }

            "select No to navigate to exemptions for small producers page" in {
              val result = navigate(false)
              result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
            }

          }
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }
}
