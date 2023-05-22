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

package controllers.test

import com.google.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

@Singleton
class AddressFrontendStubController @Inject()
(val controllerComponents: MessagesControllerComponents) extends FrontendBaseController with I18nSupport {

  val exampleId: String = "foobarwizzbang"
  lazy val address = Json.obj(
    ("organisation", JsString("Some Trading Name")),
    ("lines", Json.toJson(List(
      "10 Other Place",
      "Some District",
      "Anytown"))
    ),
    ("postcode", JsString("ZZ1 1ZZ")),
    ("country", Json.obj(
      ("code", JsString("GB")),
      ("name", JsString("United Kingdom"))
    ))
  )
  lazy val addressResponse =
    Json.obj(
      ("auditRef", JsString("bed4bd24-72da-42a7-9338-f43431b7ed72")),
      ("id", JsString("GB990091234524")),
      ("address", address)
    )

  def initialise(): Action[JsValue] = Action(parse.json) { request =>
    val continueUrl: String = (request.body.as[JsObject] \ "options" \ "continueUrl").get.as[String]
    Accepted.withHeaders(LOCATION -> s"$continueUrl?id=$exampleId")
  }

  def addresses(id: String): Action[AnyContent] = Action { _ =>
  Ok(addressResponse)
  }
}
