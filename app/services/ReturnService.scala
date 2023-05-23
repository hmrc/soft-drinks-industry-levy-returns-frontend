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

import cats.implicits.catsSyntaxSemigroup
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.backend.Site
import models.retrieved.RetrievedSubscription
import models.{Amounts, ReturnPeriod, ReturnsVariation, SdilReturn, UserAnswers}
import pages._
import play.api.Logger
import play.api.http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.ReturnsHelper.{extractTotal, listItemsWithTotal}
import utilitlies.{ReturnsHelper, TotalForQuarter, UserTypeCheck}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnService @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                              config: FrontendAppConfig) {

  val logger: Logger = Logger(this.getClass())

  val costLower = config.lowerBandCostPerLitre
  val costHigher = config.higherBandCostPerLitre

  def getPendingReturns(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] = sdilConnector.returns_pending(utr)

  def sendReturn(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, userAnswers: UserAnswers, nilReturn: Boolean)
                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    if(nilReturn) {
      submitNilReturn(subscription, returnPeriod)
    } else {
      submitReturnAndVariation(subscription, returnPeriod, userAnswers)
    }
  }

  def submitNilReturn(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val sdilReturn = ReturnsHelper.emptyReturn
    submitReturn(subscription, returnPeriod, sdilReturn)
  }

  def submitReturnAndVariation(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, userAnswers: UserAnswers)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val sdilReturn = returnToBeSubmitted(userAnswers)
    val sdilVariation = returnVariationToBeSubmitted(subscription, sdilReturn, userAnswers)
    for {
      _ <- submitReturn(subscription, returnPeriod, sdilReturn)
      varition <- submitReturnVariation(subscription.sdilRef, sdilVariation)
    } yield varition
  }

  def calculateAmounts(sdilRef: String,
                       userAnswers: UserAnswers,
                       returnPeriod: ReturnPeriod)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Amounts] = {
    for {
      isSmallProducer <- sdilConnector.checkSmallProducerStatus(sdilRef, returnPeriod)
      balanceBroughtForward <- getBalanceBroughtForward(sdilRef)
    } yield getAmounts(userAnswers, balanceBroughtForward, isSmallProducer.getOrElse(false))

  }

  def getBalanceBroughtForward(sdilRef: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[BigDecimal] = {
    if (config.balanceAllEnabled) {
      sdilConnector.balanceHistory(sdilRef, withAssessment = false).map { financialItem =>
        extractTotal(listItemsWithTotal(financialItem))
      }
    } else {
      sdilConnector.balance(sdilRef, withAssessment = false)
    }
  }

  def getAmounts(userAnswers: UserAnswers, balanceBroughtForward: BigDecimal, isSmallProducer: Boolean): Amounts = {
    val totalForQuarter = TotalForQuarter.calculateTotal(userAnswers, isSmallProducer)(config)
    val total = totalForQuarter - balanceBroughtForward
    Amounts(totalForQuarter, balanceBroughtForward, total)
  }

  private def submitReturn(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, sdilReturn: SdilReturn)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    sdilConnector.returns_update(subscription.utr, returnPeriod, sdilReturn).map {
      case Some(OK) => logger.info(s"Return submitted for ${subscription.sdilRef} year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
      case _ => logger.error(s"Failed to submit return for ${subscription.sdilRef} year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
        throw new RuntimeException(s"Failed to submit return ${subscription.sdilRef} year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
    }
  }

  private def submitReturnVariation(sdilRef: String, variation: ReturnsVariation)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    sdilConnector.returns_variation(sdilRef, variation).map {
      case Some(OK) => logger.info(s"Return variation submitted for $sdilRef")
      case _ => logger.error(s"Failed to submit return variation for $sdilRef")
        throw new RuntimeException(s"Failed to submit return variation $sdilRef")
    }
  }

  private def returnVariationToBeSubmitted(subscription: RetrievedSubscription, sdilReturn: SdilReturn, userAnswers: UserAnswers) = {
    val isNewImporter = UserTypeCheck.isNewImporter(sdilReturn, subscription)
    val isNewPacker = UserTypeCheck.isNewPacker(sdilReturn, subscription)
    ReturnsVariation(
      orgName = subscription.orgName,
      ppobAddress = subscription.address,
      importer = (isNewImporter, ((sdilReturn.totalImported)).combineN(4)),
      packer = (isNewPacker, (sdilReturn.totalPacked).combineN(4)),
      warehouses = getWarehouseSites(userAnswers),
      packingSites = userAnswers.packagingSiteList.values.toList,
      phoneNumber = subscription.contact.phoneNumber,
      email = subscription.contact.email,
      taxEstimation = taxEstimation(sdilReturn)
    )
  }

  private def returnToBeSubmitted(userAnswers: UserAnswers): SdilReturn = {
    SdilReturn(
      ownBrandsLitres(userAnswers),
      packLargeLitres(userAnswers),
      userAnswers.smallProducerList,
      importsLitres(userAnswers),
      importsSmallLitres(userAnswers),
      exportLitres(userAnswers),
      wastageLitres(userAnswers))
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

  private def getWarehouseSites(userAnswers: UserAnswers): List[Site] = {
    userAnswers.warehouseList.map { case (id, warehouse) =>
      Site.fromWarehouse(warehouse)
    }.toList
  }

  private def taxEstimation(r: SdilReturn): BigDecimal = {
    val t = r.packLarge |+| r.importLarge |+| r.ownBrand
    (t._1 * costLower |+| t._2 * costHigher) * 4
  }
}
