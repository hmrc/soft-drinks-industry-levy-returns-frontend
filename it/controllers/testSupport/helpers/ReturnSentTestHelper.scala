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

package controllers.testSupport.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import models.{ReturnsVariation, SdilReturn}
import play.api.libs.json.Json

import scala.jdk.CollectionConverters.*

object ReturnSentTestHelper {

  def checkSdilReturnAndVariationSent(wireMockServer: WireMockServer, expectedReturn: SdilReturn, expectedVariation: ReturnsVariation): Boolean = {
    val requestToSendReturn =
      wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/returns/0000001611/year/")).head
    val returnSentRequestBody = Json.parse(requestToSendReturn.getBodyAsString)
    val expectedReturnJson    = Json.toJson(expectedReturn)

    val requestToSendVariation =
      wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/returns/variation/sdil")).head
    val variationSentRequestBody = Json.parse(requestToSendVariation.getBodyAsString)
    val expectedVariationJson    = Json.toJson(expectedVariation)

    val returnIsSent           = returnSentRequestBody == expectedReturnJson
    val returnsVariationIsSent = variationSentRequestBody == expectedVariationJson

    returnIsSent && returnsVariationIsSent
  }
}
