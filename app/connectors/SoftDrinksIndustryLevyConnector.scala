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


import models.{ReturnPeriod, SdilReturn}
import play.api.Configuration
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import models.retrieved.RetrievedSubscription

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
    val http: HttpClient,
    val configuration: Configuration
  )(implicit ec: ExecutionContext)
  extends ServicesConfig(configuration) {

  lazy val sdilUrl: String = baseUrl("soft-drinks-industry-levy")
  lazy val sdilFrontendUrl:String = baseUrl("soft-drinks-industry-levy-frontend")

  private def getSubscriptionUrl(sdilNumber: String,identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription(
    sdilNumber: String,
    identifierType: String
  )(implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] =
  http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(sdilNumber: String,identifierType)).map {
    case Some(a) => Some(a)
    case _ => None
  }

  private def smallProducerUrl(sdilRef:String,period:ReturnPeriod):String = s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(
                                sdilRef: String,
                                period: ReturnPeriod
                              )(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
        http.GET[Option[Boolean]](smallProducerUrl(sdilRef,period)).map {
        case Some(a) => Some(a)
        case _ => None
  }

  def oldestPendingReturnPeriod(utr: String)(implicit hc: HeaderCarrier): Future[Option[ReturnPeriod]] = {
    val  m = http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending")
    m.map(_.sortBy(_.year).sortBy(_.quarter).headOption)
  }

  def submitReturn(sdilReturn: SdilReturn)(implicit hc: HeaderCarrier): Future[Boolean] = {
    http.POST[SdilReturn, Boolean](s"$sdilFrontendUrl/submit-return",sdilReturn)
  }

}
