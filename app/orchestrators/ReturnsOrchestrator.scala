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
import errors.{NoPendingReturnForGivenPeriod, ReturnsErrors}
import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest}
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


  //ToDo remove when ATs etc route through the dashboard
  def tempSetupReturn
                     (implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): ReturnResult[Unit] = {
    val latestReturn = returnService.getPendingReturns(request.subscription.utr).map{pendingReturns =>
      pendingReturns.sortBy(_.start).headOption match {
        case Some(pendingReturn) => Right(pendingReturn)
        case _ => Left(NoPendingReturnForGivenPeriod)
      }}
    for {
      lr <- EitherT(latestReturn)
      _ <- EitherT.right[ReturnsErrors](sdilSessionCache.save[ReturnPeriod](request.sdilEnrolment, SDILSessionKeys.RETURN_PERIOD, lr))
      _ <- setupUserAnswers(request.sdilEnrolment, false)
    } yield ((): Unit)
  }
  def setupNewReturn(year: Int, quarter: Int, nilReturn: Boolean)
                    (implicit request: IdentifierRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): ReturnResult[Unit] = {
    for {
      _ <- getAndSaveValidReturnPeriod(year, quarter)
      _ <- setupUserAnswers(request.sdilEnrolment, nilReturn)
    } yield ((): Unit)
  }

  def getAndSaveValidReturnPeriod(year: Int, quarter: Int)
                 (implicit request: IdentifierRequest[AnyContent],
                  hc: HeaderCarrier,
                  ec: ExecutionContext): ReturnResult[ReturnPeriod] = EitherT{
    returnService.getPendingReturns(request.subscription.utr).flatMap { pendingReturns =>
      val returnPeriod = ReturnPeriod(year, quarter)
      if(pendingReturns.contains(returnPeriod)) {
        sdilSessionCache.save[ReturnPeriod](request.sdilEnrolment, SDILSessionKeys.RETURN_PERIOD, returnPeriod).map(
          _ => Right(returnPeriod)
        )
      } else {
        Future.successful(Left(NoPendingReturnForGivenPeriod))
      }
    }
  }

  def setupUserAnswers(sdilRef: String, nilReturn: Boolean): ReturnResult[Boolean] = EitherT {
    val defaultUserAnswers = UserAnswers(sdilRef, isNilReturn = nilReturn)
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
