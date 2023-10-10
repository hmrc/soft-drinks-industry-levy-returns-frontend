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

package orchestrators

import cats.data.EitherT
import cats.implicits._
import com.google.inject.{Inject, Singleton}
import errors.NoPendingReturnForGivenPeriod
import models.requests.{DataRequest, OptionalDataRequest}
import models.retrieved.RetrievedSubscription
import models.{Amounts, ReturnPeriod, UserAnswers}
import play.api.mvc.AnyContent
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import service.ReturnResult
import services.ReturnService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsOrchestrator @Inject()(returnService: ReturnService,
                                    sdilSessionCache: SDILSessionCache,
                                    sessionRepository: SessionRepository) {

  def handleReturnRequest(year: Int, quarter: Int, nilReturn: Boolean)
                         (implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): ReturnResult[Unit] = EitherT{

    val requestReturnPeriod = ReturnPeriod(year, quarter)
    val userAnswersForReturnPeriod = {
      request.userAnswers.fold[Option[UserAnswers]](None) {userAnswers =>
        if(userAnswers.returnPeriod == requestReturnPeriod) {
          Some(userAnswers)
        } else {
          None
        }
      }
    }
    userAnswersForReturnPeriod match {
      case Some(userAnswers) if userAnswers.submitted => Future.successful(Left(NoPendingReturnForGivenPeriod))
      case Some(userAnswers) if userAnswers.isNilReturn == nilReturn =>
        Future.successful(Right((): Unit))
      case _ => setupNewReturn(requestReturnPeriod, nilReturn).value
    }
  }



  def setupNewReturn(returnPeriod: ReturnPeriod, nilReturn: Boolean)
                    (implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): ReturnResult[Unit] = {
    for {
      _ <- checkIfValidReturnPeriod(returnPeriod)
      _ <- setupUserAnswers(request.subscription, returnPeriod, nilReturn)
    } yield ((): Unit)
  }

  def checkIfValidReturnPeriod(returnPeriod: ReturnPeriod)
                              (implicit request: OptionalDataRequest[AnyContent],
                  hc: HeaderCarrier,
                  ec: ExecutionContext): ReturnResult[ReturnPeriod] = EitherT {

    returnService.getPendingReturns(request.subscription.utr).map { pendingReturns =>
      if(pendingReturns.contains(returnPeriod)) {
         Right(returnPeriod)
      } else {
        Left(NoPendingReturnForGivenPeriod)
      }
    }
  }

  def setupUserAnswers(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, nilReturn: Boolean): ReturnResult[Boolean] = EitherT {
    lazy val defaultUserAnswers = new UserAnswers(subscription, returnPeriod, nilReturn)
    sessionRepository.set(defaultUserAnswers)
  }

  def calculateAmounts(sdilRef: String,
                       userAnswers: UserAnswers,
                       returnPeriod: ReturnPeriod)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Amounts] = {
    returnService.calculateAmounts(sdilRef, userAnswers, returnPeriod)
  }

  def completeReturnAndUpdateUserAnswers()
                                        (implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val subscription = request.subscription
    val returnPeriod = request.returnPeriod
    val userAnswers = request.userAnswers
    for {
      amounts <- returnService.calculateAmounts(subscription.sdilRef, userAnswers, returnPeriod)
      _ <- sdilSessionCache.save[Amounts](subscription.sdilRef, SDILSessionKeys.AMOUNTS, amounts)
      sr <- returnService.sendReturn(subscription, returnPeriod, userAnswers, amounts.totalForQuarter == 0)
      _ <- sessionRepository.set(request.userAnswers.copy(submitted = true))
    } yield sr
  }


  def getCalculatedAmountsForReturnSent(sdilRef: String,
                           userAnswers: UserAnswers,
                           returnPeriod: ReturnPeriod)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Amounts] = {
    sdilSessionCache.fetchEntry[Amounts](sdilRef, SDILSessionKeys.AMOUNTS).flatMap {
      case Some(amounts) => Future.successful(amounts)
      case None => calculateAmounts(sdilRef, userAnswers, returnPeriod)
    }
  }

}
