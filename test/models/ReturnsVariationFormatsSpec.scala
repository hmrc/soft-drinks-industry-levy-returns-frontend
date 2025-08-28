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
import ReturnsVariation._

class ReturnsVariationFormatsSpec extends AnyWordSpec with Matchers {

  "longTupleFormatter [(Long, Long)]" should {
    "write as { lower, higher }" in {
      Json.toJson[(Long, Long)](1L -> 2L) shouldBe Json.obj("lower" -> 1L, "higher" -> 2L)
    }
    "read from { lower, higher }" in {
      Json.obj("lower" -> 5L, "higher" -> 9L).as[(Long, Long)] shouldBe (5L -> 9L)
    }
  }

  "bllFormat [(Boolean, (Long, Long))]" should {
    "write as { _1: Boolean, _2: { lower, higher } }" in {
      Json.toJson[(Boolean, (Long, Long))](true -> (3L -> 7L)) shouldBe
        Json.obj("_1" -> true, "_2" -> Json.obj("lower" -> 3L, "higher" -> 7L))
    }
    "round-trip correctly" in {
      val v: (Boolean, (Long, Long)) = (false, (0L, 0L))
      Json.toJson(v).as[(Boolean, (Long, Long))] shouldBe v
    }
  }
}
