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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.*
import services.Encryption
import java.time.Instant

class DatedCacheMapMongoFormatsSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit lazy val encryption: Encryption = app.injector.instanceOf[Encryption]

  private val fixedInstant = Instant.parse("2024-01-02T03:04:05Z")

  "DatedCacheMap.MongoFormats" should {

    "round-trip (writes then reads) to the original value" in {
      val cm = DatedCacheMap(
        id = "abc-123",
        data = Map(
          "str" -> JsString("hello"),
          "num" -> JsNumber(42),
          "obj" -> Json.obj("k" -> "v")
        ),
        lastUpdated = fixedInstant
      )

      val json = Json.toJson(cm)(using DatedCacheMap.MongoFormats.writes)
      val back = json.as[DatedCacheMap](using DatedCacheMap.MongoFormats.reads)

      back shouldBe cm
    }

    "produce EncryptedValue-shaped JSON under data when writing" in {
      val cm   = DatedCacheMap("id1", Map("k" -> JsString("v")), fixedInstant)
      val json = Json.toJson(cm)(using DatedCacheMap.MongoFormats.writes)

      val dataObj = (json \ "data").as[JsObject]
      dataObj.keys should contain("k")

      val enc = (dataObj \ "k").as[JsObject]
      enc.keys should contain allOf ("value", "nonce")
    }

    "fail to read when data entries are not EncryptedValue-shaped" in {
      val good = Json
        .toJson(DatedCacheMap("id2", Map("k" -> JsString("v")), fixedInstant))(
          using DatedCacheMap.MongoFormats.writes
        )
        .as[JsObject]

      val bad = good + ("data" -> Json.obj("k" -> Json.obj("not" -> "encrypted")))

      bad.validate[DatedCacheMap](using DatedCacheMap.MongoFormats.formats).isError shouldBe true
    }
  }
}
