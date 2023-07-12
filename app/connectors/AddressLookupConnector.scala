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
import connectors.httpParsers.AddressLookupHttpParser._
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.alf.AlfResponse
import models.alf.init.JourneyConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utilitlies.GenericLogger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupConnector @Inject()(val http: HttpClient,
                                       implicit val config: FrontendAppConfig) {
  private[connectors] def getAddressUrl(id: String, addressLookupFrontendTestEnabled: Boolean): String = {
    if(addressLookupFrontendTestEnabled) {
      s"${config.returnsFrontendBaseUrl}${controllers.test.routes.AddressFrontendStubController.addresses(id).url}"
    } else {
      s"${config.addressLookupService}/api/confirmed?id=$id"
    }
  }
  private[connectors] def initJourneyUrl(addressLookupFrontendTestEnabled: Boolean): String = {
    if(addressLookupFrontendTestEnabled) {
      s"${config.returnsFrontendBaseUrl}${controllers.test.routes.AddressFrontendStubController.initialise().url}"
    } else {
      s"${config.addressLookupService}/api/init"
    }

  }

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[AlfResponse]] ={
    http.GET[HttpResult[AlfResponse]](getAddressUrl(id, config.addressLookUpFrontendTestEnabled))(AddressLookupGetAddressReads, hc, ec)
  }

  def initJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[String]] = {
    http.POST[JourneyConfig, HttpResult[String]](initJourneyUrl(config.addressLookUpFrontendTestEnabled),
      journeyConfig)(JourneyConfig.format, AddressLookupInitJourneyReads, hc, ec)
  }
}