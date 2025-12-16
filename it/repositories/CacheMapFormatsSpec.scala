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

package repositories

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*

class CacheMapFormatsSpec extends AnyWordSpec with Matchers {

  "CacheMap JSON format" should {

    "write and read (round-trip) correctly" in {
      val cm = CacheMap(
        id = "abc-123",
        data = Map(
          "str"  -> JsString("x"),
          "num"  -> JsNumber(42),
          "bool" -> JsBoolean(true),
          "obj"  -> Json.obj("k" -> "v"),
          "arr"  -> Json.arr(1, 2, 3),
          "nil"  -> JsNull
        )
      )

      val json = Json.toJson(cm) // uses implicit OFormat
      json shouldBe Json.obj(
        "id"   -> "abc-123",
        "data" -> Json.obj(
          "str"  -> "x",
          "num"  -> 42,
          "bool" -> true,
          "obj"  -> Json.obj("k" -> "v"),
          "arr"  -> Json.arr(1, 2, 3),
          "nil"  -> JsNull
        )
      )

      json.as[CacheMap] shouldBe cm
    }

    "ignore unknown top-level fields when reading" in {
      val json = Json.obj(
        "id"    -> "X",
        "data"  -> Json.obj("a" -> 1),
        "extra" -> "ignored"
      )
      json.as[CacheMap] shouldBe CacheMap("X", Map("a" -> JsNumber(1)))
    }

    "fail to read when required fields are missing" in {
      Json.obj("id" -> "only").validate[CacheMap].isError       shouldBe true
      Json.obj("data" -> Json.obj()).validate[CacheMap].isError shouldBe true
    }

    "fail to read when fields have wrong types" in {
      val bad1 = Json.obj("id" -> 123, "data" -> Json.obj())
      val bad2 = Json.obj("id" -> "ok", "data" -> "not-an-object")
      bad1.validate[CacheMap].isError shouldBe true
      bad2.validate[CacheMap].isError shouldBe true
    }
  }
}
