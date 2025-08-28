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

package models.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class ErrorModelSpec extends AnyWordSpec with Matchers {

  "ErrorModel JSON format" should {
    "write and read correctly" in {
      val model = ErrorModel(400, "Bad request")
      val json = Json.toJson(model)
      json shouldBe Json.obj("status" -> 400, "message" -> "Bad request")
      json.as[ErrorModel] shouldBe model
    }

    "fail when required fields missing" in {
      val badJson = Json.obj("status" -> 500)
      badJson.validate[ErrorModel].isError shouldBe true
    }
  }

  "Error ADT" should {
    "include AddressValidationError as distinct from ErrorModel" in {
      AddressValidationError.isInstanceOf[Error] shouldBe true
      ErrorModel(1, "x").isInstanceOf[Error] shouldBe true
      AddressValidationError should not be ErrorModel(1, "x")
    }
  }
}
