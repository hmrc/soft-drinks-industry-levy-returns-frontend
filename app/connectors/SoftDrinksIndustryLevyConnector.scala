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

import models.retrieved.RetrievedSubscription
import models.{FinancialLineItem, ReturnPeriod, SdilReturn}
import play.api.Configuration
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class SoftDrinksIndustryLevyConnector @Inject()(val http: HttpClient,
                                                val configuration: Configuration,
                                                sdilSessionCache: SDILSessionCache)(implicit ec: ExecutionContext) extends ServicesConfig(configuration) {

  lazy val sdilUrl: String = baseUrl("soft-drinks-industry-levy")

  private def getSubscriptionUrl(sdilNumber: String,identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription(sdilNumber: String, identifierType: String)(implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] = {
    sdilSessionCache.fetchEntry[RetrievedSubscription](sdilNumber, SDILSessionKeys.SUBSCRIPTION).flatMap{
      case Some(subscription) => Future.successful(Some(subscription))
      case None =>
        http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(sdilNumber: String, identifierType)).flatMap {
          case Some(a) =>
            sdilSessionCache.save(a.sdilRef, SDILSessionKeys.SUBSCRIPTION, a)
              .map{_ => Some(a)}
          case _ => Future.successful(None)
        }
    }
  }

  private def smallProducerUrl(sdilRef:String, period:ReturnPeriod):String = s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    http.GET[Option[Boolean]](smallProducerUrl(sdilRef,period)).map {
      case Some(a) => Some(a)
      case _ => None
  }

  def oldestPendingReturnPeriod(utr: String)(implicit hc: HeaderCarrier): Future[Option[ReturnPeriod]] = {
    val returnPeriods = http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending")
    returnPeriods.map(_.sortBy(_.year).sortBy(_.quarter).headOption)
  }

  def balance(sdilRef: String, withAssessment: Boolean)(implicit hc: HeaderCarrier): Future[BigDecimal] =
    http.GET[BigDecimal](s"$sdilUrl/balance/$sdilRef/$withAssessment")

  def balanceHistory(sdilRef: String, withAssessment: Boolean)(implicit hc: HeaderCarrier): Future[List[FinancialLineItem]] = {
    import models.FinancialLineItem.formatter
    http.GET[List[FinancialLineItem]](s"$sdilUrl/balance/$sdilRef/history/all/$withAssessment")
  }

  def returns_pending(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] =
    http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending")

  def returns_update(utr: String, period: ReturnPeriod, sdilReturn: SdilReturn)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    val uri = s"$sdilUrl/returns/$utr/year/${period.year}/quarter/${period.quarter}"
    http.POST[SdilReturn, HttpResponse](uri, sdilReturn) map {
      response => Some(response.status)
    }
  }

}
