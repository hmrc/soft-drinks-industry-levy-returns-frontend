/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.Configuration
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import models.backend.Subscription
import models.retrieved.RetrievedSubscription
import models._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
                                                 http: HttpClient,
                                                 val configuration: Configuration
                                               )(implicit ec: ExecutionContext)
  extends ServicesConfig(configuration) {

  lazy val sdilUrl: String = baseUrl("soft-drinks-industry-levy")
  //Returns
  def retrieveSubscription(sdilNumber: String, identifierType: String = "sdil")(
    implicit hc: HeaderCarrier): Option[RetrievedSubscription] =
        http.GET[Option[RetrievedSubscription]](s"$sdilUrl/subscription/$identifierType/$sdilNumber").flatMap {
          case Some(a) => Future(Some(a))
          case _ => Future.successful(None)
  }

}
