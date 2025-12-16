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
import models.alf.init.JourneyConfig
import play.api.libs.json.{JsObject, JsString, Json}

import scala.jdk.CollectionConverters.*

object ALFTestHelper {

  def requestedBodyMatchesExpected(wireMockServer: WireMockServer, bodyExpected: JourneyConfig): Boolean = {
    val requestMadeToAlf  = wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/api/init")).head
    val jsonBodyOfRequest = Json.parse(requestMadeToAlf.getBodyAsString).as[JsObject]
    val jsonBodyRequestWithNewContinueUrl =
      jsonBodyOfRequest.+("options" -> jsonBodyOfRequest("options").as[JsObject].+("continueUrl" -> JsString("")))

    val jsonBodyOfExpectedPost             = Json.toJson(bodyExpected).as[JsObject]
    val jsonBodyExpectedWithNewContinueUrl =
      jsonBodyOfExpectedPost.+("options" -> jsonBodyOfExpectedPost("options").as[JsObject].+("continueUrl" -> JsString("")))

    val continueUrlSentMatches =
      if bodyExpected.options.continueUrl.nonEmpty then {
        requestMadeToAlf.getBodyAsString.contains(bodyExpected.options.continueUrl.substring(0, bodyExpected.options.continueUrl.indexOf("/")))
      } else { true }

    jsonBodyRequestWithNewContinueUrl == jsonBodyExpectedWithNewContinueUrl && continueUrlSentMatches
  }
}
