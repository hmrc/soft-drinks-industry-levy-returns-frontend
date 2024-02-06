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

import base.ReturnsTestData.smallProducerListWithTwoProducers
import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "SdilReturn" - {

    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 1" in {
      val expectedValue: BigDecimal = 6.30
      val data = testSdilReturn(
        packSmall = List.empty,
        ownBrand = (15, 15),
        packLarge = (15, 15),
        importLarge = (15, 15),
        importSmall = (15, 15),
        export = (15, 15),
        wastage = (15, 15))

      data.total mustBe expectedValue
    }

    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 2" in {
      val expectedValue: BigDecimal = 25.20
      val data = testSdilReturn(
        packSmall = smallProducerListWithTwoProducers,
        ownBrand = (30, 30),
        packLarge = (30, 30),
        importLarge = (30, 30),
        `export` = (15, 15),
        wastage = (15, 15))

      data.total mustBe expectedValue
    }

  }
}
