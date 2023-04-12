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

import controllers.routes
import models._
import models.retrieved.RetrievedSubscription
import pages._
import play.api.mvc.Call
import services.ReturnService

import javax.inject.{Inject, Singleton}


@Singleton
class Navigator @Inject()(returnService: ReturnService) {

  private val normalRoutes: Page => UserAnswers => Option[SdilReturn] => Option[RetrievedSubscription] => Option[Boolean] => Option[Map[String,Boolean]]=> Call = {
    case RemoveSmallProducerConfirmPage => userAnswers => _ => _ => _ =>  _ =>removeSmallProducerConfirmPageNavigation(userAnswers)
    case HowManyCreditsForExportPage => _ => _ => _ => _ =>  _ =>routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    case HowManyCreditsForLostDamagedPage => _ => _ => _ => _ =>  _ =>routes.CheckYourAnswersController.onPageLoad()
    case ClaimCreditsForLostDamagedPage => userAnswers => _ => _ => _ => changedRegistation =>
      claimCreditsForLostDamagedPageNavigation(userAnswers, changedRegistation)
    case ClaimCreditsForExportsPage => userAnswers => _ => _ => _ =>  _ =>claimCreditsForExportPageNavigation(userAnswers)
    case AddASmallProducerPage => _ => _ => _ => _ =>  _ =>routes.SmallProducerDetailsController.onPageLoad(NormalMode)
    case BroughtIntoUkFromSmallProducersPage => userAnswers => _ => _ => _ =>  _ =>broughtIntoUkfromSmallProducersPageNavigation(userAnswers)
    case HowManyBroughtIntoUkPage => _ => _ => _ => _ =>  _ =>routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    case BroughtIntoUKPage => userAnswers => _ => _ => _ =>  _ =>broughtIntoUkPageNavigation(userAnswers)
    case HowManyBroughtIntoTheUKFromSmallProducersPage => _ => _ => _ => _ =>  _ =>routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
    case HowManyAsAContractPackerPage => _ => _ => _ => _ =>  _ =>routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    case ExemptionsForSmallProducersPage => userAnswers => _ => _ => _ =>  _ =>exemptionForSmallProducersPageNavigation(userAnswers)
    case SmallProducerDetailsPage => userAnswers => _ => _ => smallProducerMissing =>  _ =>smallProducerDetailsPageNavigation(userAnswers, smallProducerMissing)
    case BrandsPackagedAtOwnSitesPage => _ => _ => _ => _ =>  _ =>routes.PackagedContractPackerController.onPageLoad(NormalMode)
    case PackagedContractPackerPage => userAnswers => _ => _ => _ =>  _ =>packagedContractPackerPageNavigation(userAnswers)
    case OwnBrandsPage => userAnswers => _ => _ => _ =>  _ =>ownBrandPageNavigation(userAnswers)
    case _ => _ => _ => _ => _ =>  _ =>routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case OwnBrandsPage => userAnswers => checkOwnBrandPageNavigation(userAnswers)
    case BrandsPackagedAtOwnSitesPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case PackagedContractPackerPage => userAnswers => checkPackagedContractPackerPageNavigation(userAnswers)
    case HowManyAsAContractPackerPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case ExemptionsForSmallProducersPage => userAnswers => checkExemptionForSmallProducersPageNavigation(userAnswers)
    case SmallProducerDetailsPage => userAnswers => checkSmallProducerDetailsPageNavigation(userAnswers)
    case AddASmallProducerPage => _ => routes.SmallProducerDetailsController.onPageLoad(CheckMode)
    case RemoveSmallProducerConfirmPage => _ => routes.SmallProducerDetailsController.onPageLoad(CheckMode)
    case BroughtIntoUKPage => userAnswers => checkBroughtIntoUkPageNavigation(userAnswers)
    case HowManyBroughtIntoUkPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case BroughtIntoUkFromSmallProducersPage => userAnswers => checkBroughtIntoUkfromSmallProducersPageNavigation(userAnswers)
    case HowManyBroughtIntoTheUKFromSmallProducersPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case ClaimCreditsForExportsPage => userAnswers => checkClaimCreditsForExportPageNavigation(userAnswers)
    case HowManyCreditsForExportPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case ClaimCreditsForLostDamagedPage => userAnswers => checkClaimCreditsForLostDamagedPageNavigation(userAnswers)
    case HowManyCreditsForLostDamagedPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private val EditRouteMap: Page => UserAnswers => Call = {
    case AddASmallProducerPage => _ => routes.SmallProducerDetailsController.onSubmit(NormalMode)
    case _ => _ => sys.error("This case should never reach")
  }

  def nextPage(page: Page,
               mode: Mode,
               userAnswers: UserAnswers,
               sdilReturn: Option[SdilReturn] = None,
               subscription: Option[RetrievedSubscription] = None,
               smallProducerMissing: Option[Boolean] = None,
               changedRegistation:Option[Map[String,Boolean]] = None): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(sdilReturn)(subscription)(smallProducerMissing)(changedRegistation)
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
    if(userAnswers.get(page = ExemptionsForSmallProducersPage).contains(true) && userAnswers.smallProducerList.isEmpty) {
      routes.AddASmallProducerController.onPageLoad(NormalMode)
    } else if (userAnswers.get(page = ExemptionsForSmallProducersPage).contains(false)) {
      routes.BroughtIntoUKController.onPageLoad(NormalMode)
    }else routes.SmallProducerDetailsController.onPageLoad(NormalMode)

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

  private def smallProducerDetailsPageNavigation(userAnswers: UserAnswers, smallProducerMissing: Option[Boolean]) = {
    smallProducerMissing match {
      case Some(true) => routes.SmallProducerDetailsController.onPageLoad(NormalMode)
      case _ =>
        if (userAnswers.get(page = SmallProducerDetailsPage).contains(true)) {
          routes.AddASmallProducerController.onPageLoad(BlankMode)
        } else if (userAnswers.smallProducerList.isEmpty) {
          routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
        } else {
          routes.BroughtIntoUKController.onPageLoad(NormalMode)
        }
    }

  }

 private def removeSmallProducerConfirmPageNavigation(userAnswers: UserAnswers) = {
   if (userAnswers.get(page = RemoveSmallProducerConfirmPage).contains(true) && userAnswers.smallProducerList.isEmpty) {
     routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
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

  private def claimCreditsForLostDamagedPageNavigation(userAnswers: UserAnswers, newRegistationChecker: Option[Map[String,Boolean]] ) = {
    if (userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
      routes.HowManyCreditsForLostDamagedController.onPageLoad(NormalMode)
    } else {
      newRegistationChecker match {
        case Some(regChange) => routes.ReturnChangeRegistrationController.onPageLoad()
        case _ => routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }

  private def checkBroughtIntoUkPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = BroughtIntoUKPage).contains(true)) {
      routes.HowManyBroughtIntoUkController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def checkBroughtIntoUkfromSmallProducersPageNavigation(userAnswers: UserAnswers) = {
    if(userAnswers.get(page = BroughtIntoUkFromSmallProducersPage).contains(true)) {
      routes.HowManyBroughtIntoTheUKFromSmallProducersController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def checkClaimCreditsForExportPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = ClaimCreditsForExportsPage).contains(true)) {
      routes.HowManyCreditsForExportController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def checkClaimCreditsForLostDamagedPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
      routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }


  private def checkOwnBrandPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = OwnBrandsPage).contains(true)) {
      routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def checkPackagedContractPackerPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = PackagedContractPackerPage).contains(true)) {
      routes.HowManyAsAContractPackerController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def checkExemptionForSmallProducersPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = ExemptionsForSmallProducersPage).contains(true)) {
      routes.SmallProducerDetailsController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def checkSmallProducerDetailsPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = SmallProducerDetailsPage).contains(true)) {
      routes.AddASmallProducerController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def packAtBusinessAddressPageNavigation(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = PackAtBusinessAddressPage).contains(true)) {
      routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
    } else {
      routes.BroughtIntoUKController.onPageLoad(NormalMode)
    }
  }


}
