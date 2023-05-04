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

import base.SpecBase
import connectors.httpParsers.AddressLookupHttpParser.AddressLookupGetAddressReads
import models.core.ErrorModel
import play.api.http.Status
import sttp.model.HeaderNames
import uk.gov.hmrc.http.HttpResponse

class AddressLookupHttpParserSpec extends SpecBase{

  val errorModel: ErrorModel = ErrorModel(Status.BAD_REQUEST, "Error Message")

  "The AddressLookupGetAddressReads" - {

    "the http response status is OK with valid Json" - {

      "return a AddressLookupModel" in {
        AddressLookupGetAddressReads.read("", "",
          HttpResponse(Status.OK, customerAddressMaxJson, Map.empty[String, Seq[String]])) mustBe Right(customerAddressMax)
      }
    }

    "the http response status is OK with invalid Json" - {

      "return an ErrorModel" in {
        AddressLookupGetAddressReads.read("", "",
          HttpResponse(Status.OK, customerAddressJsonError, Map.empty[String, Seq[String]])) mustBe
          Left(ErrorModel(Status.INTERNAL_SERVER_ERROR,"Invalid Json returned from Address Lookup"))
      }
    }

    "the http response status is BAD_REQUEST" - {

      "return an ErrorModel" in {
        AddressLookupGetAddressReads.read("", "",
          HttpResponse(Status.BAD_REQUEST, "")) mustBe
          Left(ErrorModel(Status.BAD_REQUEST,"Downstream error returned when retrieving CustomerAddressModel from AddressLookup"))
      }
    }

    "the http response status unexpected" - {

      "return an ErrorModel" in {
        AddressLookupGetAddressReads.read("", "",
          HttpResponse(Status.SEE_OTHER, "")) mustBe
          Left(ErrorModel(Status.SEE_OTHER,"Downstream error returned when retrieving CustomerAddressModel from AddressLookup"))
      }
    }
  }
  "The AddressLookupInitJourneyReads" - {
    s"should return url if ${Status.ACCEPTED} returned and ${HeaderNames.Location} exists" in {

    }
    s"return Left if ${Status.ACCEPTED} but no header exists" in {

    }
    s"return Left if status is ${Status.BAD_REQUEST}" in {

    }
    "return Left if status not accepted statuses from API" in {

    }
  }
}
