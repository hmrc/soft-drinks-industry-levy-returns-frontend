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

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.requests.DataRequest
import models.retrieved.RetrievedSubscription
import models.{ReturnPeriod, SdilReturn, SmallProducer, UserAnswers, Warehouse}
import pages._
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.ReturnsHelper
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

import javax.inject.Inject
import scala.concurrent.Future

@Singleton
class ReturnService @Inject()(config: FrontendAppConfig,
                              sdilConnector: SoftDrinksIndustryLevyConnector) {

  def getPendingReturns(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] = sdilConnector.returns_pending(utr)

  def returnsUpdate(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, userAnswers: UserAnswers, nilReturn: Boolean)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    sdilConnector.returns_update(subscription.utr, returnPeriod, returnToBeSubmitted(nilReturn, subscription, userAnswers))
  }

  def returnToBeSubmitted(nilReturn: Boolean, subscription: RetrievedSubscription, userAnswers: UserAnswers) = {
    if (nilReturn) {
      ReturnsHelper.emptyReturn
    } else {
      SdilReturn(
        ownBrandsLitres(subscription, userAnswers),
        packLargeLitres(userAnswers),
        userAnswers.smallProducerList,
        importsLitres(userAnswers),
        importsSmallLitres(userAnswers),
        exportLitres(userAnswers),
        wastageLitres(userAnswers))
    }
  }


  private def ownBrandsLitres(subscription: RetrievedSubscription, userAnswers: UserAnswers) = {
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
