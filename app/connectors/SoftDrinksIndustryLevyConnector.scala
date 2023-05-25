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
import models.retrieved.{OptRetrievedSubscription, OptSmallProducer, RetrievedSubscription}
import models.{FinancialLineItem, ReturnPeriod, ReturnsVariation, SdilReturn}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
    val http: HttpClient,
    frontendAppConfig: FrontendAppConfig,
    sdilSessionCache: SDILSessionCache
  )(implicit ec: ExecutionContext) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private def getSubscriptionUrl(sdilNumber: String,identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription(identifierValue: String, identifierType: String)
                          (implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] = {
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some(optSubscription) => Future.successful(optSubscription.optRetrievedSubscription)
      case None =>
        http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(identifierValue: String, identifierType)).flatMap {
          optRetrievedSubscription =>
            sdilSessionCache.save[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map { _ => optRetrievedSubscription }
        }
    }
  }

  private def smallProducerUrl(sdilRef:String, period:ReturnPeriod):String = s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sdilSessionCache.fetchEntry[OptSmallProducer](sdilRef, SDILSessionKeys.smallProducerForPeriod(period)).flatMap {
      case Some(optSP) => Future.successful(optSP.optSmallProducer)
      case None =>
        http.GET[Option[Boolean]](smallProducerUrl(sdilRef, period)).flatMap {
          optSP =>
            sdilSessionCache.save(sdilRef, SDILSessionKeys.smallProducerForPeriod(period), OptSmallProducer(optSP))
              .map { _ => optSP }
        }
    }

  def getPendingReturnPeriods(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] = {
   http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending")
  }

  def balance(
               sdilRef: String,
               withAssessment: Boolean
             )(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    sdilSessionCache.fetchEntry[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment)).flatMap {
      case Some(b) => Future.successful(b)
      case None =>
        http.GET[BigDecimal](s"$sdilUrl/balance/$sdilRef/$withAssessment")
          .flatMap { b =>
            sdilSessionCache.save[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment), b)
              .map(_ => b)
          }
    }
  }

  def balanceHistory(
                      sdilRef: String,
                      withAssessment: Boolean
                    )(implicit hc: HeaderCarrier): Future[List[FinancialLineItem]] = {
    import FinancialLineItem.formatter
    sdilSessionCache.fetchEntry[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment)).flatMap {
      case Some(fli) => Future.successful(fli)
      case None =>
        http.GET[List[FinancialLineItem]](s"$sdilUrl/balance/$sdilRef/history/all/$withAssessment")
          .flatMap{ fli => sdilSessionCache.save[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment), fli)
            .map(_ => fli)
      }
    }
  }

  def returns_update(utr: String, period: ReturnPeriod, sdilReturn: SdilReturn)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    val uri = s"$sdilUrl/returns/$utr/year/${period.year}/quarter/${period.quarter}"
    http.POST[SdilReturn, HttpResponse](uri, sdilReturn) map {
      response => Some(response.status)
    }
  }

  def returns_variation(sdilRef: String, variation: ReturnsVariation)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    http.POST[ReturnsVariation, HttpResponse](s"$sdilUrl/returns/variation/sdil/$sdilRef", variation) map {
      response => Some(response.status)
    }
  }

}
