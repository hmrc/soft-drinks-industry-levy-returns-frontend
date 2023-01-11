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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._
import models.retrieved.RetrievedSubscription


@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Option[SdilReturn] => Option[RetrievedSubscription] => Call = {
    case RemoveSmallProducerConfirmPage => userAnswers => _  => _ => removeSmallProducerConfirmPageNavigation(userAnswers)
    case HowManyCreditsForExportPage => _ => _ => _ => routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    case ClaimCreditsForLostDamagedPage => userAnswers => sdilReturnOpt => subscriptionOpt =>
      claimCreditsForLostDamagedPageNavigation(userAnswers, sdilReturnOpt, subscriptionOpt)
    case ClaimCreditsForExportsPage => userAnswers => _  => _ =>claimCreditsForExportPageNavigation(userAnswers)
    case AddASmallProducerPage => _  => _  => _ => routes.SmallProducerDetailsController.onPageLoad(NormalMode)
    case BroughtIntoUkFromSmallProducersPage => userAnswers => _  => _ => broughtIntoUkfromSmallProducersPageNavigation(userAnswers)
    case HowManyBroughtIntoUkPage => _ => _  => _ => routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    case BroughtIntoUKPage => userAnswers => _ => _ => broughtIntoUkPageNavigation(userAnswers)
    case HowManyAsAContractPackerPage => _  => _  => _ => routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    case ExemptionsForSmallProducersPage => userAnswers => _  => _  => exemptionForSmallProducersPageNavigation(userAnswers)
    case SmallProducerDetailsPage => userAnswers => _  => _  => smallProducerDetailsPageNavigation(userAnswers)
    case BrandsPackagedAtOwnSitesPage => _ => _ => _  =>  routes.PackagedContractPackerController.onPageLoad(NormalMode)
    case PackagedContractPackerPage => userAnswers => _  => _ => packagedContractPackerPageNavigation(userAnswers)
    case OwnBrandsPage => userAnswers => _  => _ => ownBrandPageNavigation(userAnswers)
    case _ => _ => _  => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  private val EditRouteMap: Page => UserAnswers => Call = {
    case AddASmallProducerPage => _ => routes.SmallProducerDetailsController.onSubmit(NormalMode)
    case _ => _ => sys.error("This case should never reach")
  }

  def nextPage(page: Page,
               mode: Mode,
               userAnswers: UserAnswers,
               sdilReturn: Option[SdilReturn] = None,
               subscription: Option[RetrievedSubscription] = None): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(sdilReturn)(subscription)
      case CheckMode => checkRouteMap(page)(userAnswers)
      case EditMode => EditRouteMap(page)(userAnswers)
      case _ => sys.error("Mode should be Normal, Check or Edit")
    }

  private def ownBrandPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = OwnBrandsPage).contains(true)) {
      routes.BrandsPackagedAtOwnSitesController.onPageLoad(NormalMode)
    } else {
      routes.PackagedContractPackerController.onPageLoad(NormalMode)
    }
  }

  private def packagedContractPackerPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = PackagedContractPackerPage).contains(true)) {
      routes.HowManyAsAContractPackerController.onPageLoad(NormalMode)
    } else {
      routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    }
  }

  private def exemptionForSmallProducersPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = ExemptionsForSmallProducersPage).contains(true)) {
      routes.AddASmallProducerController.onPageLoad(NormalMode)
    } else {
      routes.BroughtIntoUKController.onPageLoad(NormalMode)
    }
  }

  private def broughtIntoUkPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = BroughtIntoUKPage).contains(true)) {
      routes.HowManyBroughtIntoUkController.onPageLoad(NormalMode)
    } else {
      routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    }
  }

  private def broughtIntoUkfromSmallProducersPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = BroughtIntoUkFromSmallProducersPage).contains(true)) {
      routes.HowManyBroughtIntoTheUKFromSmallProducersController.onPageLoad(NormalMode)
    } else {
      routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
    }
  }

  private def smallProducerDetailsPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = SmallProducerDetailsPage).contains(true)) {
      routes.AddASmallProducerController.onPageLoad(BlankMode)
    } else {
      routes.BroughtIntoUKController.onPageLoad(NormalMode)
    }
  }

 private def removeSmallProducerConfirmPageNavigation(userAnswers: UserAnswers) = {
   if(userAnswers.get(page = RemoveSmallProducerConfirmPage).contains(true)) {
     routes.AddASmallProducerController.onPageLoad(BlankMode)
   } else {
     routes.SmallProducerDetailsController.onPageLoad(NormalMode)
   }
 }

  private def claimCreditsForExportPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = ClaimCreditsForExportsPage).contains(true)) {
      routes.HowManyCreditsForExportController.onPageLoad(NormalMode)
    } else {
      routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    }
  }

  private def claimCreditsForLostDamagedPageNavigation(userAnswers: UserAnswers,
                                                       sdilReturnOpt: Option[SdilReturn],
                                                       subscriptionOpt: Option[RetrievedSubscription]) = {
    if(userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
      routes.HowManyCreditsForLostDamagedController.onPageLoad(NormalMode)
    } else {
      (sdilReturnOpt, subscriptionOpt)  match {
        case (Some(sdilReturn), Some(subscription)) =>{
          val isNewImporter = (sdilReturn.totalImported._1 > 0L && sdilReturn.totalImported._2 > 0L) && !subscription.activity.importer
          val isNewPacker = (sdilReturn.totalPacked._1 > 0L && sdilReturn.totalPacked._2 > 0L) && !subscription.activity.contractPacker
          if(isNewImporter || isNewPacker) routes.ReturnChangeRegistrationController.onPageLoad else routes.IndexController.onPageLoad
          //TODO IndexController to be replaced with CYA page
        }
        case _ => routes.IndexController.onPageLoad //TODO to be replaced with CYA page
      }
    }
  }



}
