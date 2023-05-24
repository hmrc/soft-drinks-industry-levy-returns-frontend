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

package controllers.actions

import models.requests.DataRequest
import models.retrieved.RetrievedSubscription
import models.{AddASmallProducer, LitresInBands, SdilReturn, UserAnswers}
import pages._
import play.api.Logger
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Result
import utilitlies.UserTypeCheck

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class RequiredUserAnswers @Inject()(implicit val executionContext: ExecutionContext) extends ActionHelpers {
  val logger = Logger(this.getClass)

  def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    page match {
      case CheckYourAnswersPage if isNilReturn(request.userAnswers) => {
        logger.info(s"${request.userAnswers.id} Nil return true on CYA")
        action
      }
      case CheckYourAnswersPage => checkYourAnswersRequiredData(action)
      case _ => action
    }
  }

  private[controllers] def isNilReturn(userAnswers: UserAnswers): Boolean = {
      userAnswers.data == Json.obj() && userAnswers.smallProducerList.isEmpty && userAnswers.warehouseList.isEmpty && userAnswers.packagingSiteList.isEmpty
  }

  private[controllers] def checkYourAnswersRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val userAnswersMissing: List[RequiredPage[_,_,_]] = returnMissingAnswers(mainRoute)
    if (userAnswersMissing.nonEmpty) {
      logger.warn(s"${request.userAnswers.id} has hit CYA and is missing $userAnswersMissing")
      Future.successful(redirectToStartOfJourney(request.subscription))
    } else {
      action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_,_,_]])(implicit request: DataRequest[_]): List[RequiredPage[_,_,_]] = {
    list.filterNot { listItem =>
      val currentPage: Option[A] = request.userAnswers.get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPage.isDefined, listItem.basedOnPreviousPage.isDefined) match {
        case (false, true) =>
            val previousPage: PreviousPage[QuestionPage[B], B] = listItem.basedOnPreviousPage.get.asInstanceOf[PreviousPage[QuestionPage[B], B]]
            val previousPageAnswer: Option[B] = request.userAnswers.get(previousPage.page)(previousPage.reads)
            !previousPageAnswer.contains(previousPage.previousPageAnswerRequired)
        case (false, _) => false
        case _ => true
        }
    }
  }
  private[controllers] def smallProducerCheck(subscription: RetrievedSubscription): List[RequiredPage[_, _,_]] = {
    if (subscription.activity.smallProducer) {
      List.empty
    } else {
      List(RequiredPage(OwnBrandsPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(BrandsPackagedAtOwnSitesPage,
          Some(PreviousPage(OwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))
    }
  }

  private[controllers] def mainRoute(implicit dataRequest: DataRequest[_]): List[RequiredPage[_, _,_]] = {
    smallProducerCheck(dataRequest.subscription) ++ restOfJourney ++ packingListReturnChange(dataRequest) ++ warehouseListReturnChange(dataRequest)
  }

  private[controllers] def restOfJourney: List[RequiredPage[_,_,_]] = {
    List(RequiredPage(PackagedContractPackerPage, None)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyAsAContractPackerPage,
        Some(PreviousPage(PackagedContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
      RequiredPage(AddASmallProducerPage,
        Some(PreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]),
      RequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyBroughtIntoUkPage,
        Some(PreviousPage(BroughtIntoUKPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyBroughtIntoTheUKFromSmallProducersPage,
        Some(PreviousPage(BroughtIntoUkFromSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyCreditsForExportPage,
        Some(PreviousPage(ClaimCreditsForExportsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyCreditsForLostDamagedPage,
        Some(PreviousPage(ClaimCreditsForLostDamagedPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]])
    )
  }

  private[controllers] val packingListReturnChange: DataRequest[_] => List[RequiredPage[_, _,_]] = { (request: DataRequest[_]) =>
    if(UserTypeCheck.isNewPacker(SdilReturn.apply(request.userAnswers), request.subscription)) {
      List(RequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]))
    } else {
      List.empty
    }
  }
  private[controllers] val warehouseListReturnChange: DataRequest[_] => List[RequiredPage[_,_,_]] = { (request: DataRequest[_]) =>
    if(UserTypeCheck.isNewImporter(SdilReturn.apply(request.userAnswers),request.subscription)) {
      List(RequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))
    } else {
      List.empty
    }
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPage: Option[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C],C](page: B, previousPageAnswerRequired: C)(val reads: Reads[C])



