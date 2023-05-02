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

import connectors.AddressLookupConnector
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.backend.{Site, UkAddress}
import models.{Address, UserAnswers, Warehouse}
import play.api.Logger
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.AddressHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupService @Inject()(
                                      sessionRepository: SessionRepository,
                                      addressLookupConnector: AddressLookupConnector
                                    ) extends AddressHelper{

  val logger: Logger = Logger(this.getClass())

  def getAddress(id:String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[Address]] = addressLookupConnector.getAddress(id)

  def addAddressUserAnswers(addressLookupState: AddressLookupState, address: Address, userAnswers: UserAnswers): Future[UserAnswers] = {
    val ukAddress = UkAddress(List(address.line1.get,address.line2.get, address.line3.get, address.line4.get), address.postcode.get)
    val newWarehouse = userAnswers.warehouseList ++ Map(generateId -> Warehouse(address.organisation,ukAddress))
    val newPackigSite = userAnswers.packagingSiteList ++ Map(generateId -> Site(ukAddress,None,address.organisation,None))
    addressLookupState match {
      case Packingdetails => sessionRepository.set(userAnswers.copy(warehouseList  = newWarehouse))
      Future.successful(userAnswers.copy(warehouseList = newWarehouse))

      case Warehousedetails => sessionRepository.set(userAnswers.copy(packagingSiteList  = newPackigSite))
        Future.successful(userAnswers.copy(packagingSiteList = newPackigSite))
    }
  }
}
