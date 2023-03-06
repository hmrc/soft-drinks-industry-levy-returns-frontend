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
import connectors.httpParsers.AddressLookupHttpParser.AddressLookupReads
import connectors.httpParsers.InitialiseAddressLookupHttpParser.InitialiseAddressLookupReads
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.customerAddress.{AddressLookupJsonBuilder, AddressLookupOnRampModel, AddressModel}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.LoggerUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupConnector @Inject()(val http: HttpClient,
                                       implicit val config: FrontendAppConfig) extends LoggerUtil {

  def initialiseJourney(addressLookupJsonBuilder: AddressLookupJsonBuilder)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[AddressLookupOnRampModel]] = {

    val url = {
      s"${config.addressLookupService}/api/v2/init"
    }

    http.POST[AddressLookupJsonBuilder, HttpResult[AddressLookupOnRampModel]](
      url, addressLookupJsonBuilder
    )(implicitly, InitialiseAddressLookupReads, hc, ec)
  }

  private[connectors] def getAddressUrl(id: String) = s"${config.addressLookupService}/api/confirmed?id=$id"

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[AddressModel]] = {
    logger.debug(s"[AddressLookupConnector][getAddress]: Calling getAddress with URL - ${getAddressUrl(id)}")
    http.GET[HttpResult[AddressModel]](getAddressUrl(id))(AddressLookupReads, hc, ec)
  }
}
