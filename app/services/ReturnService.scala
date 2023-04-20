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

package services

import connectors.SoftDrinksIndustryLevyConnector
import models.retrieved.RetrievedSubscription
import models.{ReturnPeriod, SdilReturn, UserAnswers}
import pages._
import play.api.Logger
import play.api.http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.ReturnsHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnService @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector) {

  val logger: Logger = Logger(this.getClass())

  def getPendingReturns(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] = sdilConnector.returns_pending(utr)

  def returnsUpdate(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, userAnswers: UserAnswers, nilReturn: Boolean)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    sdilConnector.returns_update(subscription.utr, returnPeriod, returnToBeSubmitted(nilReturn, userAnswers)).map {
      case Some(OK) => logger.info(s"Return submitted for ${subscription.sdilRef} year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
      case _ => logger.error(s"Failed to submit return for ${subscription.sdilRef} year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
        throw new RuntimeException(s"Failed to submit return ${subscription.sdilRef} year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
    }
  }

  private def returnToBeSubmitted(nilReturn: Boolean, userAnswers: UserAnswers): SdilReturn = {
    if (nilReturn) {
      ReturnsHelper.emptyReturn
    } else {
      SdilReturn(
        ownBrandsLitres(userAnswers),
        packLargeLitres(userAnswers),
        userAnswers.smallProducerList,
        importsLitres(userAnswers),
        importsSmallLitres(userAnswers),
        exportLitres(userAnswers),
        wastageLitres(userAnswers))
    }
  }


  private def ownBrandsLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L))
  }

  private def packLargeLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L))
  }

  private def importsLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L))
  }

  private def importsSmallLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyBroughtIntoTheUKFromSmallProducersPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyBroughtIntoTheUKFromSmallProducersPage).map(_.highBand).getOrElse(0L))
  }

  private def exportLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L))
  }

  private def wastageLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L))
  }


}
