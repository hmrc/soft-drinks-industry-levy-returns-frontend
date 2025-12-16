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

package controllers.testSupport.actions

import play.api.libs.ws.{WSClient, WSResponse}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String

import scala.concurrent.Future

class PostCalls(baseUrl: String) {

  def declaration(implicit client: WSClient): Future[WSResponse] =
    client
      .url(s"$baseUrl/submit")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" -> "some-id", "Csrf-Token" -> "nocheck", "Content-Type" -> "application/json")
      .post(declarationData)

  private def declarationData =
    """{
      |"fullName": "John",
      |"jobTitle": "Director",
      |"isUseGgEmail": true,
      |"ggEmail": "user@test.com"
      |}
    """.stripMargin

}
