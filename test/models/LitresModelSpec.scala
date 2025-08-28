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

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

class LitresModelSpec extends SpecBase with MockitoSugar {

  "Litres" - {
    "should convert a litresInBands to low and high bands" in {
      val litresValues: Litres = Litres(100, 200)
      litresValues.lower mustBe 100
      litresValues.higher mustBe 200
    }

    "should build from LitresInBands secondary constructor" in {
      val inBands = LitresInBands(lowBand = 5L, highBand = 7L)
      val litres = new Litres(inBands)
      litres.lower mustBe 5L
      litres.higher mustBe 7L
    }

    "should JSON round-trip" in {
      val m = Litres(10L, 20L)
      val js = Json.toJson(m)
      js mustBe Json.obj("lower" -> 10L, "higher" -> 20L)
      js.as[Litres] mustBe m
    }

    "should fail JSON reads on missing/wrong fields" in {
      Json.obj("lower" -> 1L).validate[Litres].isError mustBe true
      Json.obj("lower" -> "x", "higher" -> 2L).validate[Litres].isError mustBe true
    }

    "should round-trip using explicit Litres.format" in {
      val m = Litres(1L, 2L)
      val js = Json.toJson(m)(Litres.format)
      js.as[Litres](Litres.format) mustBe m
    }

  }

}
