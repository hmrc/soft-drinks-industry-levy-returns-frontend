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

import base.ReturnsTestData.litreage
import config.FrontendAppConfig

trait DataHelper {

  def testSdilReturn(
    ownBrand: (Long, Long) = litreage,
    packLarge: (Long, Long) = litreage,
    packSmall: List[SmallProducer],
    importLarge: (Long, Long) = litreage,
    importSmall: (Long, Long) = litreage,
    export: (Long, Long) = litreage,
    wastage: (Long, Long) = litreage): SdilReturn = {
    SdilReturn(
      ownBrand = (ownBrand._1, ownBrand._2),
      packLarge = (packLarge._1, packLarge._2),
      packSmall = packSmall,
      importLarge = (importLarge._1, importLarge._2),
      importSmall = (importSmall._1, importSmall._2),
      export = (export._1, export._2),
      wastage = (wastage._1, wastage._2))
  }

  def testSmallProducer(
    alias: String,
    sdilRef: String,
    litreage: (Long, Long)): SmallProducer = SmallProducer(
    alias = alias,
    sdilRef = sdilRef,
    litreage = litreage)

}
