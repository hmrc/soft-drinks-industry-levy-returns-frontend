/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock.*
import models.alf.{AlfAddress, AlfResponse}
import play.api.http.Status
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames

case class ALFStub()(implicit builder: PreconditionBuilder) {
  val aAddress = AlfResponse(address =
    AlfAddress(
      organisation = Some("soft drinks ltd"),
      List("line 1", "line 2", "line 3", "line 4"),
      postcode = Some("aa1 1aa"),
      countryCode = Some("UK")
    )
  )

  val BadAddress =
    "Failed Address"

  def getAddress(id: String) = {
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id", equalTo(id))
        .willReturn(ok(Json.toJson(aAddress).toString()))
    )
    builder
  }

  def getBadAddress(id: String) = {
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id", equalTo(id))
        .willReturn(ok(Json.toJson(BadAddress).toString()))
    )
    builder
  }

  def getBadResponse(id: String) = {
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id", equalTo(id))
        .willReturn(notFound())
    )
    builder
  }

  def getSuccessResponseFromALFInit(locationHeaderReturned: String) =
    stubFor(
      post(
        urlPathMatching("/api/init")
      )
        .willReturn(
          status(Status.ACCEPTED)
            .withHeader(HeaderNames.LOCATION, locationHeaderReturned)
        )
    )
  def getFailResponseFromALFInit(statusReturned: Int) =
    stubFor(
      post(
        urlPathMatching("/api/init")
      ).willReturn(
        status(statusReturned)
      )
    )

}
