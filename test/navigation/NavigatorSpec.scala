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
import helpers.LoggerHelper
import pages._
import models._
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase with LoggerHelper {

  val navigator = new Navigator

  val id = "id"
  val superColaProducerAlias = "Super Cola Ltd"
  val superJuiceProducerAlias = "Super Juice Ltd"
  val referenceNumber1 = "XZSDIL000000234"
  val referenceNumber2 = "XZSDIL000000235"
  val litreage = (10L, 20L)

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers(id)) mustBe routes.IndexController.onPageLoad()
      }

      "navigate to correct page " - {

        "When current Page is " - {

          "Own brand packaged at own site page" - {

             def navigate(value: Boolean, mode: Mode = NormalMode) = navigator.nextPage(OwnBrandsPage,
               mode,
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

          "Remove Packaging details confirmation if" - {
            "Yes is selected go to Packing details page" in {
              val result = navigator.nextPage(RemovePackagingDetailsConfirmationPage, NormalMode, UserAnswers(id,Json.obj("removePackagingDetailsConfirmation" -> true)))
              result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
            }
            "No is selected go to Packing details page" in {
              val result = navigator.nextPage(RemovePackagingDetailsConfirmationPage, NormalMode, UserAnswers(id,Json.obj("removePackagingDetailsConfirmation" -> false)))
              result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
            }
          }

          "Secondary warehouse details page" - {

            "Should navigate to enter warehouse name when yes is selected" in {
              navigator.nextPage(SecondaryWarehouseDetailsPage,
                NormalMode,
                UserAnswers(id, Json.obj("secondaryWarehouseDetails" -> true))
              ) mustBe  routes.IndexController.onPageLoad()
            }
            "Should navigate to check your answers controller when no is selected" in {
              navigator.nextPage(SecondaryWarehouseDetailsPage,
                NormalMode,
                UserAnswers(id, Json.obj("secondaryWarehouseDetails" -> false))
              ) mustBe  routes.CheckYourAnswersController.onPageLoad()
            }
          }

          "Remove a warehouse confirmation page" - {

            "Should navigate to secondary warehouse details controller when yes is selected" in {
              navigator.nextPage(RemoveWarehouseConfirmPage,
                NormalMode,
                UserAnswers(id, Json.obj("removeWarehouse" -> true))
              ) mustBe  routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
            }
            "Should navigate to secondary warehouse details controller when no is selected" in {
              navigator.nextPage(RemoveWarehouseConfirmPage,
                NormalMode,
                UserAnswers(id, Json.obj("removeWarehouse" -> false))
              ) mustBe  routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
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

            def navigate(value: Boolean, mode: Mode = NormalMode) = navigator.nextPage(PackagedContractPackerPage,
              mode,
              UserAnswers(id, Json.obj("packagedContractPacker" -> value)))

            "select Yes to navigate to How Many packaged as contract packer" in {
              val result = navigate(true)
              result mustBe routes.HowManyAsAContractPackerController.onPageLoad(NormalMode)
            }

            "select No to navigate to exemptions for small producers page" in {
              val result = navigate(false)
              result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
            }

            "Should navigate to Check Your Answers page when no is selected in check mode" in {
              val result = navigate(false, CheckMode)
              result mustBe routes.CheckYourAnswersController.onPageLoad()
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

            def navigate(value: Boolean, mode: Mode = NormalMode) = navigator.nextPage(ExemptionsForSmallProducersPage,
              mode,
              UserAnswers(sdilNumber, Json.obj("exemptionsForSmallProducers" -> value)))

            "select Yes to navigate to Add small producer pager" in {
              val result = navigate(true)
              result mustBe routes.AddASmallProducerController.onPageLoad(NormalMode)
            }

            "should navigate to small producer details page when yes is selected and there are is greater than 0 small producers" in {
              val result = navigator.nextPage(ExemptionsForSmallProducersPage,
                NormalMode,
                UserAnswers(id, Json.obj("exemptionsForSmallProducers" -> true),
                  smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, litreage)))
              )

              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.BroughtIntoUKController.onPageLoad(NormalMode)
            }

            "Should navigate to Check Your Answers page when no is selected in check mode" in {
              val result = navigate(false, CheckMode)
              result mustBe routes.CheckYourAnswersController.onPageLoad()
            }

          }

          "Brought into UK" - {

            def navigate(value: Boolean, mode: Mode = NormalMode) = navigator.nextPage(BroughtIntoUKPage,
              mode,
              UserAnswers(sdilNumber, Json.obj("broughtIntoUK" -> value)))

            "select Yes to navigate to How many brought into UK pager" in {
              val result = navigate(true)
              result mustBe routes.HowManyBroughtIntoUkController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
            }

            "Should navigate to Check Your Answers page when no is selected in check mode" in {
              val result = navigate(false, CheckMode)
              result mustBe routes.CheckYourAnswersController.onPageLoad()
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

            def navigate(value: Boolean, mode: Mode = NormalMode) = navigator.nextPage(BroughtIntoUkFromSmallProducersPage,
              mode,
              UserAnswers(sdilNumber, Json.obj("broughtIntoUkFromSmallProducers" -> value)))

            "select Yes to navigate to How many brought into UK pager" in {
              val result = navigate(true)
              result mustBe routes.HowManyBroughtIntoTheUKFromSmallProducersController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
            }

            "Should navigate to Check Your Answers page when no is selected in check mode" in {
              val result = navigate(false, CheckMode)
              result mustBe routes.CheckYourAnswersController.onPageLoad()
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

            def navigate(value: Boolean, mode: Mode = NormalMode) = navigator.nextPage(ClaimCreditsForExportsPage,
              mode,
              UserAnswers(sdilNumber, Json.obj("claimCreditsForExports" -> value)))

            "select Yes to navigate to How many credits for export page" in {
              val result = navigate(true)
              result mustBe routes.HowManyCreditsForExportController.onPageLoad(NormalMode)
            }

            "select No to navigate to brought into uk page" in {
              val result = navigate(false)
              result mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
            }

            "Should navigate to Check Your Answers page when no is selected in check mode" in {
              val result = navigate(false, CheckMode)
              result mustBe routes.CheckYourAnswersController.onPageLoad()
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

          "Claim credits for lost or damaged " - {

            def navigate(value: Boolean, userAnswers: Boolean => UserAnswers,
                         sdilReturn: Option[SdilReturn],
                         subscription: Option[RetrievedSubscription] = Some(aSubscription)) = {
                navigator.nextPage(ClaimCreditsForLostDamagedPage,
                NormalMode, userAnswers(value), sdilReturn, subscription)
            }

            "select Yes to navigate to How many credits for lost damaged page" in {
              def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                Json.obj("claimCreditsForLostDamaged" -> value))
              val result = navigator.nextPage(ClaimCreditsForLostDamagedPage,
                NormalMode,
                userAnswers(true)
              )
              result mustBe routes.HowManyCreditsForLostDamagedController.onPageLoad(NormalMode)
            }

            "select No to navigate to check your answers controller page " - {
              "when user is a not a new Importer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))
                val sdilReturn = SdilReturn((0L,0L),(0L, 0L),List.empty,(0L, 0L),(0L,0L),(0L,0L),(0L,0L))
                val result = navigate(false, (_ => userAnswers(false)), Some(sdilReturn))
                result mustBe routes.CheckYourAnswersController.onPageLoad()
              }
            }

            "select No to navigate to return change registration controller page " - {
              "when user is a new Importer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (100L, 100L), (100L, 100L), (0L, 0L), (0L, 0L))
                val result = navigate(false, (_ => userAnswers(false)), Some(sdilReturn))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "select No to navigate to return change registration controller page " - {
              "when user is a new packer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val sdilReturn = SdilReturn((100L, 100L), (100L, 100L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(false, (_ => userAnswers(false)), Some(sdilReturn))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "select No to navigate to Index controller page " - {
              "when user is a new importer and subscription activity is importer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val importerActivity = RetrievedActivity(
                  smallProducer = false,
                  largeProducer = true,
                  contractPacker = false,
                  importer = true,
                  voluntaryRegistration = false)
                val importerSubscription = aSubscription.copy(activity = importerActivity)

                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (100L, 100L), (100L, 100L), (0L, 0L), (0L, 0L))
                val result = navigate(false, (_ => userAnswers(false)), Some(sdilReturn), Some(importerSubscription))
                result mustBe routes.CheckYourAnswersController.onPageLoad()
              }
            }

            "select No to navigate to Check your answers controller page " - {
              "when user is a new packer and subscription activity is packer" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val importerActivity = RetrievedActivity(
                  smallProducer = false,
                  largeProducer = true,
                  contractPacker = true,
                  importer = false,
                  voluntaryRegistration = false)
                val importerSubscription = aSubscription.copy(activity = importerActivity)

                val sdilReturn = SdilReturn((100L, 100L), (100L, 100L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(false, (_ => userAnswers(false)), Some(sdilReturn), Some(importerSubscription))
                result mustBe routes.CheckYourAnswersController.onPageLoad()
              }
            }

            "select No to navigate to Check your answers controller page " - {
              "when no subscription is available" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val sdilReturn = SdilReturn((100L, 100L), (100L, 100L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(false, (_ => userAnswers(false)), Some(sdilReturn), None)
                result mustBe routes.JourneyRecoveryController.onPageLoad()
              }
            }

            "select No to navigate to Check your answers controller page " - {
              "when no return is available" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val result = navigate(false, (_ => userAnswers(false)), None)
                result mustBe routes.JourneyRecoveryController.onPageLoad()
              }
            }

            "select No to navigate to Check your answers controller page " - {
              "when no return nor subscription is available" in {
                def userAnswers(value: Boolean) = UserAnswers(sdilNumber,
                  Json.obj(
                    "claimCreditsForLostDamaged" -> value))

                val result = navigate(false, (_ => userAnswers(false)), None, None)
                result mustBe routes.JourneyRecoveryController.onPageLoad()
              }
            }

          }

          "How many credits for lost or damaged " - {

            def navigate(userAnswers: UserAnswers,
                         sdilReturn: Option[SdilReturn] = None,
                         subscription: Option[RetrievedSubscription]) = {

              navigator.nextPage(HowManyCreditsForLostDamagedPage, NormalMode, userAnswers, sdilReturn, subscription)
            }

            "should redirect to check your answers page when current sdil is neither a packer nor an importer" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
              val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
              result mustBe routes.CheckYourAnswersController.onPageLoad()
            }

            "should redirect to check your answers page when current sdil is already a packer" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = true, importer = false, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
              val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
              result mustBe routes.CheckYourAnswersController.onPageLoad()
            }

            "should redirect to check your answers page when current sdil is already an importer" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = true, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
              val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
              result mustBe routes.CheckYourAnswersController.onPageLoad()
            }

            "should redirect to check your answers page when current sdil is already an importer and a packer" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = true, importer = true, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
              val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
              result mustBe routes.CheckYourAnswersController.onPageLoad()
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the pack large conditions for being a new packer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (1L, 1L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the pack small conditions for being a new packer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List(SmallProducer("","",(1L, 1L))), (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the pack small and pack large conditions for being a new packer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (1L, 1L), List(SmallProducer("", "", (1L, 1L))), (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the large import conditions for being a new importer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the small import conditions for being a new importer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (1L, 1L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the large and small import conditions for being a new importer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (1L, 1L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to return change registration page when current sdil is neither a packer nor an importer" - {
              "but meets the pack conditions and import conditions for being a new packer and new packer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (1L, 1L), List(SmallProducer("", "", (1L, 1L))), (1L, 1L), (1L, 1L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.ReturnChangeRegistrationController.onPageLoad()
              }
            }

            "should redirect to journey recovery page when no return is available" in {
              withCaptureOfLoggingFrom(navigator.logger) { events =>
                val result = navigate(emptyUserAnswers, None, Some(aSubscription))
                result mustBe routes.JourneyRecoveryController.onPageLoad()
                events.collectFirst {
                  case event =>
                    event.getLevel.levelStr mustEqual("WARN")
                    event.getMessage mustEqual(s"SDIL return not provided for ${aSubscription.sdilRef}")
                }.getOrElse(fail("No logging captured"))
              }
            }

            "should redirect to journey recovery page when no subscription is available" in {
              val sdilReturn = SdilReturn((0L, 0L), (1L, 1L), List.empty, (1L, 1L), (1L, 1L), (0L, 0L), (0L, 0L))
              withCaptureOfLoggingFrom(navigator.logger) { events =>
                val result = navigate(emptyUserAnswers, Some(sdilReturn), None)
                result mustBe routes.JourneyRecoveryController.onPageLoad()
                events.collectFirst {
                  case event =>
                    event.getLevel.levelStr mustEqual ("WARN")
                    event.getMessage mustEqual ("SDIL return or subscription not provided for current unknown user")
                }.getOrElse(fail("No logging captured"))
              }
            }

            "should redirect to journey recovery page when no return nor subscription is available" in {
              withCaptureOfLoggingFrom(navigator.logger) { events =>
                val result = navigate(emptyUserAnswers, None, None)
                result mustBe routes.JourneyRecoveryController.onPageLoad()
                events.collectFirst {
                  case event =>
                    event.getLevel.levelStr mustEqual ("WARN")
                    event.getMessage mustEqual ("SDIL return or subscription not provided for current unknown user")
                }.getOrElse(fail("No logging captured"))
              }
            }

          }

          "change registration page" - {

            def navigate(userAnswers: UserAnswers,
                         sdilReturn: Option[SdilReturn] = None,
                         subscription: Option[RetrievedSubscription]) = {

              navigator.nextPage(ReturnChangeRegistrationPage, NormalMode, userAnswers, sdilReturn, subscription)
            }

            "should redirect to Ask Secondary Warehouse page when user clicks change registration" - {
              "when meets the large import conditions for being a new importer" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
              val userAnswers = UserAnswers(id, Json.obj("returnChangeRegistration" -> ""))
              val result = navigate(userAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode)
              }
            }

            "should redirect to Pack At Business Address page when user clicks change registration" - {
              "when meets the pack conditions and import conditions for being a new packer and new importer" in {
                val sdilActivity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (1L, 1L), List(SmallProducer("", "", (1L, 1L))), (1L, 1L), (1L, 1L), (0L, 0L), (0L, 0L))
                val result = navigate(emptyUserAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
              }
            }

            "should redirect to journey recovery when no subscription is found" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
              val userAnswers = UserAnswers(id, Json.obj("returnChangeRegistration" -> ""))
              val result = navigate(userAnswers, Some(sdilReturn), None)
              result mustBe routes.JourneyRecoveryController.onPageLoad()
            }

            "should redirect to journey recovery when no sdil return is found" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val userAnswers = UserAnswers(id, Json.obj("returnChangeRegistration" -> ""))
              val result = navigate(userAnswers, None,Some(modifiedSubscription))
              result mustBe routes.JourneyRecoveryController.onPageLoad()
            }

          }

          "packing site details page" - {

            def navigate(userAnswers: UserAnswers,
                         sdilReturn: Option[SdilReturn] = None,
                         subscription: Option[RetrievedSubscription]) = {

              navigator.nextPage(PackagingSiteDetailsPage, NormalMode, userAnswers, sdilReturn, subscription)
            }

            "should redirect to packing site name when yes is selected" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
              val userAnswers = UserAnswers(id, Json.obj("packagingSiteDetails" -> true))
              val result = navigate(userAnswers, Some(sdilReturn), Some(modifiedSubscription))
              result mustBe routes.IndexController.onPageLoad()
            }

            "should redirect to check your answers when no is selected and user is not a new importer" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (0L, 0L), (0L, 0L), (0L, 0L), (0L, 0L))
              val userAnswers = UserAnswers(id, Json.obj("packagingSiteDetails" -> false))
              val result = navigate(userAnswers, Some(sdilReturn), Some(modifiedSubscription))
              result mustBe routes.CheckYourAnswersController.onPageLoad()
            }

            "should redirect to warehouse name when current sdil is not an importer" - {
              "but meets the large import conditions for being a new importer" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
                val userAnswers = UserAnswers(id, Json.obj("packagingSiteDetails" -> false))
                val result = navigate(userAnswers, Some(sdilReturn), Some(modifiedSubscription))
                result mustBe routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode)
              }
            }

            "should redirect to journey recovery when no subscription is found" in {
                val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
                val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
                val userAnswers = UserAnswers(id, Json.obj("packagingSiteDetails" -> false))
                val result = navigate(userAnswers, Some(sdilReturn), None)
                result mustBe routes.JourneyRecoveryController.onPageLoad()
            }

            "should redirect to journey recovery when no sdil return is found" in {
              val sdilActivity = RetrievedActivity(false, true, contractPacker = false, importer = false, false)
              val sdilReturn = SdilReturn((0L, 0L), (0L, 0L), List.empty, (1L, 1L), (0L, 0L), (0L, 0L), (0L, 0L))
              val modifiedSubscription = aSubscription.copy(activity = sdilActivity)
              val userAnswers = UserAnswers(id, Json.obj("packagingSiteDetails" -> false))
              val result = navigate(userAnswers, None,Some(modifiedSubscription))
              result mustBe routes.JourneyRecoveryController.onPageLoad()
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
                smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, litreage))))

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
                  smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, litreage))
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
                    SmallProducer(superColaProducerAlias, referenceNumber1, litreage),
                    SmallProducer(superJuiceProducerAlias, referenceNumber2, litreage))
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
                  smallProducerList = List(SmallProducer(superColaProducerAlias, referenceNumber1, litreage))
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
                    SmallProducer(superColaProducerAlias, referenceNumber1, litreage),
                    SmallProducer(superJuiceProducerAlias, referenceNumber2, litreage))
                )
              )
              result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
            }

          }

        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" - {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers(id)) mustBe routes.CheckYourAnswersController.onPageLoad()
      }


      "Own brands" - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(OwnBrandsPage, CheckMode, UserAnswers(id, Json.obj("ownBrands" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many packed at your own own site page when yes is selected" in {
          navigator.nextPage(OwnBrandsPage, CheckMode, UserAnswers(id, Json.obj("ownBrands" -> true))
          ) mustBe routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(BrandsPackagedAtOwnSitesPage,
            CheckMode,
            UserAnswers(id, Json.obj("ownBrands" -> true,
              "brandsPackagedAtOwnSites" ->
                Json.obj("lowBand" -> "100", "highBand" -> "100")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "Contract packer" - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(PackagedContractPackerPage, CheckMode, UserAnswers(id, Json.obj("packagedContractPacker" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many packed at your as contract packer page when yes is selected" in {
          navigator.nextPage(PackagedContractPackerPage, CheckMode, UserAnswers(id, Json.obj("packagedContractPacker" -> true))
          ) mustBe routes.HowManyAsAContractPackerController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(HowManyAsAContractPackerPage,
            CheckMode,
            UserAnswers(id, Json.obj("packagedContractPacker" -> true,
              "howManyAsAContractPacker" ->
                Json.obj("lowBand" -> "100", "highBand" -> "100")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

      }

      "Exemption for small producer " - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(ExemptionsForSmallProducersPage, CheckMode, UserAnswers(id, Json.obj("exemptionsForSmallProducers" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to small producer details page when yes is selected" in {
          navigator.nextPage(ExemptionsForSmallProducersPage, CheckMode, UserAnswers(id, Json.obj("exemptionsForSmallProducers" -> true))
          ) mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(HowManyAsAContractPackerPage,
            CheckMode,
            UserAnswers(id, Json.obj("exemptionsForSmallProducers" -> true,
              "addASmallProducer" -> Json.obj("lowBand" -> "10000", "highBand" -> "20000")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }


      "Brought into UK " - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(BroughtIntoUKPage, CheckMode, UserAnswers(id, Json.obj("broughtIntoUK" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many brought into UK page when yes is selected" in {
          navigator.nextPage(BroughtIntoUKPage, CheckMode, UserAnswers(id, Json.obj("broughtIntoUK" -> true))
          ) mustBe routes.HowManyBroughtIntoUkController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(HowManyBroughtIntoUkPage,
            CheckMode,
            UserAnswers(id, Json.obj("broughtIntoUK" -> true,
              "HowManyBroughtIntoUk" ->
                Json.obj("lowBand" -> "100", "highBand" -> "100")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

      }

      "Brought into from small producers UK " - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(BroughtIntoUkFromSmallProducersPage, CheckMode, UserAnswers(id, Json.obj("broughtIntoUkFromSmallProducers" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many brought into uk from small producers page when yes is selected" in {
          navigator.nextPage(BroughtIntoUkFromSmallProducersPage, CheckMode, UserAnswers(id, Json.obj("broughtIntoUkFromSmallProducers" -> true))
          ) mustBe routes.HowManyBroughtIntoTheUKFromSmallProducersController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(HowManyBroughtIntoTheUKFromSmallProducersPage,
            CheckMode,
            UserAnswers(id, Json.obj("broughtIntoUkFromSmallProducers" -> true,
              "howManyBroughtIntoTheUKFromSmallProducers" ->
                Json.obj("lowBand" -> "100", "highBand" -> "100")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

      }

      "Claim credits for exports " - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(ClaimCreditsForExportsPage, CheckMode, UserAnswers(id, Json.obj("claimCreditsForExports" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many credits for exports page when yes is selected" in {
          navigator.nextPage(ClaimCreditsForExportsPage, CheckMode, UserAnswers(id, Json.obj("claimCreditsForExports" -> true))
          ) mustBe routes.HowManyCreditsForExportController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(HowManyCreditsForExportPage,
            CheckMode,
            UserAnswers(id, Json.obj("claimCreditsForExports" -> true,
              "howManyCreditsForExport" ->
                Json.obj("lowBand" -> "100", "highBand" -> "100")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "Claim credits for lost or damaged " - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(ClaimCreditsForLostDamagedPage, CheckMode, UserAnswers(id, Json.obj("claimCreditsForLostDamaged" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many credits for lost or damaged page when yes is selected" in {
          navigator.nextPage(ClaimCreditsForLostDamagedPage, CheckMode, UserAnswers(id, Json.obj("claimCreditsForLostDamaged" -> true))
          ) mustBe routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode)
        }

        "Should navigate to Check Your Answers page when yes is selected and data present" in {
          navigator.nextPage(HowManyCreditsForLostDamagedPage,
            CheckMode,
            UserAnswers(id, Json.obj("claimCreditsForLostDamaged" -> true,
              "howManyCreditsForLostDamaged" ->
                Json.obj("lowBand" -> "100", "highBand" -> "100")))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "Check small producer details" - {

        "Should navigate to Check Your Answers page when no is selected" in {
          navigator.nextPage(SmallProducerDetailsPage, CheckMode, UserAnswers(id, Json.obj("smallProducerDetails" -> false))
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "Should navigate to how many credits for lost or damaged page when yes is selected" in {
          navigator.nextPage(SmallProducerDetailsPage, CheckMode, UserAnswers(id, Json.obj("smallProducerDetails" -> true))
          ) mustBe routes.AddASmallProducerController.onPageLoad(CheckMode)
        }

      }

      "Add a small producer " - {

        "Should navigate to small producer details controller when no is selected" in {
          navigator.nextPage(AddASmallProducerPage,
            CheckMode,
            UserAnswers(id, Json.obj("addASmallProducer" -> Json.obj("lowBand" -> "10000", "highBand" -> "20000")))
          ) mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
        }
      }

      "Remove a warehouse confirmation page" - {

        "Should navigate to secondary warehouse details controller when yes is selected" in {
          navigator.nextPage(RemoveWarehouseConfirmPage,
            CheckMode,
            UserAnswers(id, Json.obj("removeWarehouse" -> true))
          ) mustBe  routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
        }
        "Should navigate to secondary warehouse details controller when no is selected" in {
          navigator.nextPage(RemoveWarehouseConfirmPage,
            CheckMode,
            UserAnswers(id, Json.obj("removeWarehouse" -> false))
          ) mustBe  routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
        }
      }

      "Remove a small producer " - {

        "Should navigate to small producer details controller when yes is selected" in {
          navigator.nextPage(RemoveSmallProducerConfirmPage,
            CheckMode,
            UserAnswers(id, Json.obj("removeSmallProducerConfirm" -> true), smallProducerList = List())
          ) mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
        }

        "Should navigate to small producer details controller when no is selected" in {
          navigator.nextPage(RemoveSmallProducerConfirmPage,
            CheckMode,
            UserAnswers(id, Json.obj("removeSmallProducerConfirm" -> false), smallProducerList = List())
          ) mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
        }
      }

    }
  }

}
