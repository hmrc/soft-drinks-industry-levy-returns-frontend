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
import models.{AlfResponse, UserAnswers, Warehouse}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.AddressHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupService @Inject()(
                                      addressLookupConnector: AddressLookupConnector
                                    ) extends AddressHelper{

  val logger: Logger = Logger(this.getClass())

  private def addressChecker(address: AlfResponse): UkAddress = {

    val ukAddress = UkAddress(address.lines,
      address.postcode.getOrElse(""))

    if (ukAddress.lines.isEmpty && ukAddress.postCode == "" && address.organisation.isEmpty) {
      throw new RuntimeException("Not Found (Alf has returned an empty address and organisation name)")
    } else {
      ukAddress
    }

  }

  def getAddress(id:String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[AlfResponse]] = addressLookupConnector.getAddress(id)

  def addAddressUserAnswers(addressLookupState: AddressLookupState, address: AlfResponse, userAnswers: UserAnswers): UserAnswers = {

    val convertedAddress = addressChecker(address)

    addressLookupState match {
      case Packingdetails => userAnswers.copy(packagingSiteList = userAnswers.packagingSiteList ++ Map(generateId -> Site(convertedAddress,None,address.organisation,None)))
      case Warehousedetails => userAnswers.copy(warehouseList = userAnswers.warehouseList ++ Map(generateId -> Warehouse(address.organisation,convertedAddress)))
    }
  }
}
