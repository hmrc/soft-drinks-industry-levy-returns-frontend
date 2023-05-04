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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.alf.AlfResponse
import models.core.ErrorModel
import play.api.http.Status
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.Logger
import play.mvc.Http.HeaderNames


object AddressLookupHttpParser {

  implicit object AddressLookupGetAddressReads extends HttpReads[HttpResult[AlfResponse]] {

    override def read(method: String, url: String, response: HttpResponse): HttpResult[AlfResponse] = {

      response.status match {
        case Status.OK => {
          response.json.validate[AlfResponse](AlfResponse.format).fold(
            invalid => {
              Logger(s"[AddressLookupHttpParser][read]: Invalid Json - ${invalid.map(res => res._1)}")
              Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Invalid Json returned from Address Lookup"))
            },
            valid => Right(valid)
          )
        }
        case status =>
          Logger(s"[AddressLookupHttpParser][read]: Unexpected Response, Status $status returned")
          Left(ErrorModel(status,"Downstream error returned when retrieving CustomerAddressModel from AddressLookup"))
      }
    }
  }

  implicit object AddressLookupInitJourneyReads extends HttpReads[HttpResult[String]] {

    override def read(method: String, url: String, response: HttpResponse): HttpResult[String] = {
      response.status match {
        case Status.ACCEPTED =>
          response.header(HeaderNames.LOCATION) match {
            case Some(location) => Right(location)
            case None => Left(ErrorModel(Status.ACCEPTED, s"No ${HeaderNames.LOCATION} key in response from init response from ALF"))
          }
        case Status.BAD_REQUEST => Left(ErrorModel(Status.BAD_REQUEST, s"${response.body} returned from ALF"))
        case status => Left(ErrorModel(status, "Unexpected error occurred when init journey from ALF"))
      }
    }
  }
}