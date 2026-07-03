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

package forms.mappings

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError

class LitresFormatterSpec extends AnyFreeSpec with Matchers {

  private val formatter = new Formatters {}.litresFormatter("lowBand")

  "litresFormatter" - {

    "must bind numbers suffixed with 'l'" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "10l"))
      result mustBe Right(10L)
    }

    "must bind numbers suffixed with 'litres'" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "10 litres"))
      result mustBe Right(10L)
    }

    "must bind numbers suffixed with ' l' (space before suffix)" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "10 l"))
      result mustBe Right(10L)
    }

    "must bind numbers suffixed with ' litres' (space before suffix)" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "10   litres"))
      result mustBe Right(10L)
    }

    "must reject when suffix appears at the start" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "litres 10"))
      result mustBe Left(Seq(FormError("lowBand", "litres.error.lowBand.nonNumeric")))
    }

    "must reject when suffix appears in the middle" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "l 10 litres"))
      result mustBe Left(Seq(FormError("lowBand", "litres.error.lowBand.nonNumeric")))
    }

    "must reject input containing multiple numbers even if suffix is present at the end" in {
      val result = formatter.bind("lowBand", Map("lowBand" -> "10 litres 5"))
      result mustBe Left(Seq(FormError("lowBand", "litres.error.lowBand.nonNumeric")))
    }
  }
}
