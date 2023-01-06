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

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case AddASmallProducerPage => _ => routes.SmallProducerDetailsController.onPageLoad(NormalMode)
    case BroughtIntoUkFromSmallProducersPage => userAnswers => broughtIntoUkfromSmallProducersPageNavigation(userAnswers)
    case HowManyBroughtIntoUkPage => _ => routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    case BroughtIntoUKPage => userAnswers => broughtIntoUkPageNavigation(userAnswers)
    case HowManyAsAContractPackerPage => _ => routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    case ExemptionsForSmallProducersPage => userAnswers => exemptionForSmallProducersPageNavigation(userAnswers)
    case BrandsPackagedAtOwnSitesPage => _ => routes.PackagedContractPackerController.onPageLoad(NormalMode)
    case PackagedContractPackerPage => userAnswers => packagedContractPackerPageNavigation(userAnswers)
    case OwnBrandsPage => userAnswers => ownBrandPageNavigation(userAnswers)
    case _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)
      case CheckMode => checkRouteMap(page)(userAnswers)
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



}
