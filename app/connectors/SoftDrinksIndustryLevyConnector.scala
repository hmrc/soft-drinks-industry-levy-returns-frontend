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

package connectors

import config.FrontendAppConfig
import models.retrieved.{ OptRetrievedSubscription, OptSmallProducer, RetrievedSubscription }
import models.{ FinancialLineItem, ReturnPeriod, ReturnsVariation, SdilReturn }
import play.api.libs.json.Json
import repositories.{ SDILSessionCache, SDILSessionKeys }
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, StringContextOps }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SoftDrinksIndustryLevyConnector @Inject() (
  val http: HttpClientV2,
  frontendAppConfig: FrontendAppConfig,
  sdilSessionCache: SDILSessionCache)(implicit ec: ExecutionContext) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private def getSubscriptionUrl(sdilNumber: String, identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription(identifierValue: String, identifierType: String)(implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] = {
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some(optSubscription) => Future.successful(optSubscription.optRetrievedSubscription)
      case None =>
        http.get(url"${getSubscriptionUrl(identifierValue: String, identifierType)}")
          .execute[Option[RetrievedSubscription]].flatMap {
            optRetrievedSubscription =>
              sdilSessionCache.save[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
                .map { _ => optRetrievedSubscription }
          }
    }
  }

  private def smallProducerUrl(sdilRef: String, period: ReturnPeriod): String = s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sdilSessionCache.fetchEntry[OptSmallProducer](sdilRef, SDILSessionKeys.smallProducerForPeriod(period)).flatMap {
      case Some(optSP) => Future.successful(optSP.optSmallProducer)
      case None =>
        http.get(url"${smallProducerUrl(sdilRef, period)}")
          .execute[Option[Boolean]].flatMap {
            optSP =>
              sdilSessionCache.save(sdilRef, SDILSessionKeys.smallProducerForPeriod(period), OptSmallProducer(optSP))
                .map { _ => optSP }
          }
    }

  def getPendingReturnPeriods(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] = {
    val pendingUrl = s"$sdilUrl/returns/$utr/pending"
    http.get(url"$pendingUrl")
      .execute[List[ReturnPeriod]]
  }

  def balance(
    sdilRef: String,
    withAssessment: Boolean)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    sdilSessionCache.fetchEntry[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment)).flatMap {
      case Some(b) => Future.successful(b)
      case None =>
        val balanceUrl = s"$sdilUrl/balance/$sdilRef/$withAssessment"
        http.get(url"$balanceUrl")
          .execute[BigDecimal]
          .flatMap { b =>
            sdilSessionCache.save[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment), b)
              .map(_ => b)
          }
    }
  }

  def balanceHistory(
    sdilRef: String,
    withAssessment: Boolean)(implicit hc: HeaderCarrier): Future[List[FinancialLineItem]] = {
    import FinancialLineItem.formatter
    sdilSessionCache.fetchEntry[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment)).flatMap {
      case Some(fli) => Future.successful(fli)
      case None =>
        val balanceHistoryUrl = s"$sdilUrl/balance/$sdilRef/history/all/$withAssessment"
        http.get(url"$balanceHistoryUrl")
          .execute[List[FinancialLineItem]]
          .flatMap { fli =>
            sdilSessionCache.save[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment), fli)
              .map(_ => fli)
          }
    }
  }

  def returns_update(utr: String, period: ReturnPeriod, sdilReturn: SdilReturn)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    val returnUpdateUrl = s"$sdilUrl/returns/$utr/year/${period.year}/quarter/${period.quarter}"
    http.post(url"$returnUpdateUrl")
      .withBody(Json.toJson(sdilReturn))
      .execute[HttpResponse]
      .map {
        response => Some(response.status)
      }
  }

  def returns_variation(sdilRef: String, variation: ReturnsVariation)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    val variationUpdateUrl = s"$sdilUrl/returns/variation/sdil/$sdilRef"
    http.post(url"$variationUpdateUrl")
      .withBody(Json.toJson(variation))
      .execute[HttpResponse]
      .map {
        response => Some(response.status)
      }
  }

}
