/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import play.api.libs.json.Json

class LevyCalculationJsonSpec extends SpecBase {

  "LevyCalculationRequest" - {

    "should serialise to the expected JSON" in {
      val request = LevyCalculationRequest(1000L, 500L, ReturnPeriod(2025, 1))

      Json.toJson(request) mustBe Json.obj(
        "lowLitres"    -> 1000,
        "highLitres"   -> 500,
        "returnPeriod" -> Json.obj("year" -> 2025, "quarter" -> 1)
      )
    }

    "should round-trip through JSON" in {
      val request = LevyCalculationRequest(2000L, 3000L, ReturnPeriod(2024, 0))
      val json    = Json.toJson(request)
      json.as[LevyCalculationRequest] mustBe request
    }
  }

  "LevyCalculation" - {

    "should deserialise from backend response JSON" in {
      val json = Json.obj(
        "lowBandLevy"      -> 180.00,
        "highBandLevy"     -> 240.00,
        "totalLevy"        -> 420.00,
        "totalRoundedDown" -> 420.00
      )

      json.as[LevyCalculation] mustBe LevyCalculation(BigDecimal("180.0"), BigDecimal("240.0"), BigDecimal("420.0"), BigDecimal("420.0"))
    }

    "should round-trip through JSON" in {
      val calculation = LevyCalculation(BigDecimal("180"), BigDecimal("240"), BigDecimal("420"), BigDecimal("420"))
      val json        = Json.toJson(calculation)
      json.as[LevyCalculation] mustBe calculation
    }

    "should round display values to 2dp" in {
      val calculation = LevyCalculation(BigDecimal("180.456"), BigDecimal("240.123"), BigDecimal("420.579"), BigDecimal("420.57"))

      calculation.lowLevy mustBe BigDecimal("180.46")
      calculation.highLevy mustBe BigDecimal("240.12")
      calculation.total mustBe BigDecimal("420.58")
    }

    "zero should be all zeroes" in {
      val zero = LevyCalculation.zero
      zero.lowBandLevy mustBe BigDecimal(0)
      zero.highBandLevy mustBe BigDecimal(0)
      zero.totalLevy mustBe BigDecimal(0)
      zero.totalRoundedDown mustBe BigDecimal(0)
    }
  }
}
