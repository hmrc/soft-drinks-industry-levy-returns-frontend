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

package models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class SdilCalculationJsonSpec extends AnyWordSpec with Matchers {

  "SdilCalculation JSON" should {

    "round-trip via format" in {
      val m = SdilCalculation(1.5, 3.25)
      val json = Json.toJson(m)
      json.as[SdilCalculation] shouldBe m
    }

    "write expected shape" in {
      Json.toJson(SdilCalculation(2.0, 4.0)) shouldBe
        Json.obj("lowBandLevy" -> 2.0, "highBandLevy" -> 4.0)
    }

    "fail to read when required fields are missing" in {
      Json.obj("lowBandLevy" -> 1.0).validate[SdilCalculation].isError shouldBe true
    }

    "fail to read when fields have wrong types" in {
      Json.obj("lowBandLevy" -> "x", "highBandLevy" -> true).validate[SdilCalculation].isError shouldBe true
    }

    "cover format/reads/writes and round-trip" in {
      val m = SdilCalculation(1.5, 3.25)
      val js = SdilCalculation.writes.writes(m)
      SdilCalculation.reads.reads(js).get shouldBe m
      Json.toJson(m)(SdilCalculation.format).as[SdilCalculation](SdilCalculation.format) shouldBe m
    }

  }
}
