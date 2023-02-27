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

  val id = "id"
  val superColaProducerAlias = "Super Cola Ltd"
  val superJuiceProducerAlias = "Super Juice Ltd"
  val referenceNumber1 = "XZSDIL000000234"
  val referenceNumber2 = "XZSDIL000000235"
  val literage = (10L, 20L)

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers(id)) mustBe routes.IndexController.onPageLoad()
      }

      "navigate to correct page " - {

        "When current Page is " - {

          "Own brand packaged at own site page" - {

             def navigate(value: Boolean) = navigator.nextPage(OwnBrandsPage,
              NormalMode,
              UserAnswers(id, Json.obj("ownBrands" -> value)))

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
                UserAnswers(id, Json.obj("ownBrands" -> true,
                  "brandsPackagedAtOwnSites" ->
                    Json.obj("lowBand" -> "100", "highBand" -> "100"))))
              result mustBe routes.PackagedContractPackerController.onPageLoad(NormalMode)
            }

          }

          "Packaged as a contract packer" - {

            def navigate(value: Boolean) = navigator.nextPage(PackagedContractPackerPage,
              NormalMode,
              UserAnswers(id, Json.obj("packagedContractPacker" -> value)))

            "select Yes to navigate to How Many packaged as contract packer" in {
              val result = navigate(true)
              result mustBe routes.HowManyAsAContractPackerController.onPageLoad(NormalMode)
            }

            "select No to navigate to exemptions for small producers page" in {
              val result = navigate(false)
              result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
            }

          }

          "How many packaged as contract packer" - {

            "select save and continue to navigate to exemptions for small producers page" in {
              val result = navigator.nextPage(HowManyAsAContractPackerPage,
                NormalMode,
                UserAnswers(sdilNumber, Json.obj("ownBrands" -> true,
                  "howManyAsAContractPacker" ->
                    Json.obj("lowBand" -> "100", "highBand" -> "100"))))
              result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
            }

          }

          "Exemptions for small producers" - {

            def navigate(value: Boolean) = navigator.nextPage(ExemptionsForSmallProducersPage,
              NormalMode,
              UserAnswers(sdilNumber, Json.obj("exemptionsForSmallProducers" -> value)))

            "select Yes to navigate to Add small producer pager" in {
              val result = navigate(true)
              result mustBe routes.AddASmallProducerController.onPageLoad(NormalMode)
            }


            "should navigate to small producer details page when yes is selected and there are is greater than 0 small producers" in {
              val result = navigator.nextPage(ExemptionsForSmallProducersPage,
                NormalMode,
                UserAnswers(id, Json.obj("exemptionsForSmallProducers" -> true),
                  smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, literage)))
              )

              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.BroughtIntoUKController.onPageLoad(NormalMode)
            }

          }

          "Brought into UK" - {

            def navigate(value: Boolean) = navigator.nextPage(BroughtIntoUKPage,
              NormalMode,
              UserAnswers(sdilNumber, Json.obj("broughtIntoUK" -> value)))

            "select Yes to navigate to How many brought into UK pager" in {
              val result = navigate(true)
              result mustBe routes.HowManyBroughtIntoUkController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
            }

          }

          "Add small producer" - {

            "select save and continue to navigate to small producer details page" in {
              val result = navigator.nextPage(AddASmallProducerPage,
                NormalMode,
                UserAnswers(id, Json.obj("ownBrands" -> true,
                  "addASmallProducer" ->
                    Json.obj("producerName" -> superColaProducerAlias, "referenceNumber" -> referenceNumber1, "lowBand" -> "100", "highBand" -> "100"))))
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

          }

          "Brought into UK from small producers" - {

            def navigate(value: Boolean) = navigator.nextPage(BroughtIntoUkFromSmallProducersPage,
              NormalMode,
              UserAnswers(sdilNumber, Json.obj("broughtIntoUkFromSmallProducers" -> value)))

            "select Yes to navigate to How many brought into UK pager" in {
              val result = navigate(true)
              result mustBe routes.HowManyBroughtIntoTheUKFromSmallProducersController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
            }

          }

          "How many brought into the uk from small producers" - {
            "select save and continue to navigate to claim-credits-for-exports" in {
              val result = navigator.nextPage(HowManyBroughtIntoTheUKFromSmallProducersPage,
                NormalMode,
                UserAnswers(sdilNumber, Json.obj("broughtIntoUkFromSmallProducers" -> true,
                  "howManyBroughtIntoTheUKFromSmallProducers" ->
                    Json.obj("lowBand" -> "100", "highBand" -> "100"))))
              result mustBe routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
            }
          }

          "Claim credits for export" - {

            def navigate(value: Boolean) = navigator.nextPage(ClaimCreditsForExportsPage,
              NormalMode,
              UserAnswers(sdilNumber, Json.obj("claimCreditsForExports" -> value)))

            "select Yes to navigate to How many credits for export page" in {
              val result = navigate(true)
              result mustBe routes.HowManyCreditsForExportController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
            }
          }

          "How many credit for export" - {

            "select save and continue to navigate to claim credits for lost damaged page" in {
              val result = navigator.nextPage(ClaimCreditsForExportsPage,
                NormalMode,
                UserAnswers(sdilNumber, Json.obj("ownBrands" -> true,
                  "claimCreditsForExports" ->
                    Json.obj("lowBand" -> "100", "highBand" -> "100"))))
              result mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
            }
          }

          "Claim credits for Lost damaged " - {

            def navigate(value: Boolean, userAnswers: Boolean => UserAnswers, sdilReturn: SdilReturn) =
              navigator.nextPage(ClaimCreditsForLostDamagedPage,
              NormalMode,
              userAnswers(value),
              Some(sdilReturn),
              Some(aSubscription)
            )

            "select Yes to navigate to How many credits for lost damaged page" in {
              def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                Json.obj("claimCreditsForLostDamaged" -> value))
              val result = navigator.nextPage(ClaimCreditsForLostDamagedPage,
                NormalMode,
                userAnswers(true)
              )
              result mustBe routes.HowManyCreditsForLostDamagedController.onPageLoad(NormalMode)
            }

            "select No to navigate to return change registration page" - {

              "when user is a new Importer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "HowManyBroughtIntoUk" -> HowManyBroughtIntoUk(100L, 100L),
                    "claimCreditsForLostDamaged" -> value))
                val sdilReturn = SdilReturn((0L,0L),(0L, 0L),List.empty,(100L, 100L),(0L,0L),(0L,0L),(0L,0L))
                val result = navigate(false, (_ => userAnswers(false)), sdilReturn)
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }

              "when user is a new packer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "howManyAsAContractPacker" -> HowManyAsAContractPacker(100L, 100L),
                    "claimCreditsForLostDamaged" -> value))
                val sdilReturn = SdilReturn((0L,0L),(100L, 100L),List.empty,(0L, 0L),(0L,0L),(0L,0L),(0L,0L))
                val result = navigate(false, (_ => userAnswers(false)), sdilReturn)
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }

            }
            //TODO change Index controller page to check your answers page once ready
            "select No to navigate to Index controller page " - {
              "when user is a not a new Importer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))
                val sdilReturn = SdilReturn((0L,0L),(0L, 0L),List.empty,(0L, 0L),(0L,0L),(0L,0L),(0L,0L))
                val result = navigate(false, (_ => userAnswers(false)), sdilReturn)
                result mustBe routes.IndexController.onPageLoad() //TODO change it to check your answers page once ready
              }

            }
          }

          "Small producer details" -{

            "should navigate to add a small producer page when yes is selected" in {

              val result = navigator.nextPage(SmallProducerDetailsPage,
                NormalMode,
                UserAnswers(id, Json.obj("smallProducerDetails" -> true)))

              result mustBe routes.AddASmallProducerController.onPageLoad(BlankMode)

            }

            "should navigate to add a brought into UK page when no is selected and there are 0 small producers" in {

              val result = navigator.nextPage(SmallProducerDetailsPage,
                NormalMode,
                UserAnswers(id, Json.obj("smallProducerDetails" -> false),List()))

              result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)

            }

            "should navigate to add a brought into UK page when no is selected and there is 1 small producer" in {

              val result = navigator.nextPage(SmallProducerDetailsPage,
                NormalMode,
                UserAnswers(id, Json.obj("smallProducerDetails" -> false),
                smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, literage))))

              result mustBe routes.BroughtIntoUKController.onPageLoad(NormalMode)

            }

          }

          "Remove small producer confirm" - {

            "should redirect to add small producer page when user selects yes and clicks on " +
              "save and continue and the this is the only small producer on the list" in {

              val result = navigator.nextPage(
                RemoveSmallProducerConfirmPage,
                NormalMode,
                UserAnswers(
                  id,
                  Json.obj("removeSmallProducerConfirm" -> true),
                  smallProducerList = List()
                ))
              result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
            }

            "should redirect to small producer details page when user selects yes and clicks on " +
              "save and continue and the there is one small producer on the list" in {

              val result = navigator.nextPage(
                RemoveSmallProducerConfirmPage,
                NormalMode,
                UserAnswers(
                  id,
                  Json.obj("removeSmallProducerConfirm" -> true),
                  smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, literage))
                ))
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

            "should redirect to small producer details page when user selects yes and clicks on " +
              "save and continue and the there is more than one small producer on the list" in {

              val result = navigator.nextPage(
                RemoveSmallProducerConfirmPage,
                NormalMode,
                UserAnswers(
                  id,
                  Json.obj("removeSmallProducerConfirm" -> true),
                  smallProducerList = List(
                    SmallProducer(superColaProducerAlias, referenceNumber1, literage),
                    SmallProducer(superJuiceProducerAlias, referenceNumber2, literage))
                ))
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

            "should redirect to small producer details page when user selects no and clicks on " +
              "save and continue and the this is the only small producer on the list" in {

              val result = navigator.nextPage(
                RemoveSmallProducerConfirmPage,
                NormalMode,
                UserAnswers(
                  id,
                  Json.obj("removeSmallProducerConfirm" -> false),
                  smallProducerList = List()
                ))
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

            "should redirect to small producer details page when user selects no and clicks on " +
              "save and continue and the there is one small producer on the list" in {

              val result = navigator.nextPage(
                RemoveSmallProducerConfirmPage,
                NormalMode,
                UserAnswers(
                  id,
                  Json.obj("removeSmallProducerConfirm" -> false),
                  smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, literage))
                ))
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

            "should redirect to small producer details page when user selects no and clicks on " +
              "save and continue and the there is more than one small producer on the list" in {

              val result = navigator.nextPage(
                RemoveSmallProducerConfirmPage,
                NormalMode,
                UserAnswers(
                  id,
                  Json.obj("removeSmallProducerConfirm" -> false),
                  smallProducerList = List(
                    SmallProducer(superColaProducerAlias, referenceNumber1, literage),
                    SmallProducer(superJuiceProducerAlias, referenceNumber2, literage))
                )
              )
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

          }

        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers(id)) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }

}
