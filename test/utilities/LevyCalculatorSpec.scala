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

package utilities

import base.ReturnsTestData.emptyUserAnswers
import base.SpecBase
import config.FrontendAppConfig
import models.{ SdilCalculation, SmallProducer, UserAnswers }
import org.mockito.scalatest.MockitoSugar
import pages._
import play.api.libs.json.{ JsObject, Json }
import utilitlies.LevyCalculator

class LevyCalculatorSpec extends SpecBase with MockitoSugar {

  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val userAnswersData: JsObject = Json.obj(
    "ownBrands" -> true,
    "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
    "inside user answers" -> true,
    "packagedContractPacker" -> true,
    "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
    "exemptionsForSmallProducers" -> true,
    "broughtIntoUK" -> true,
    "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
    "broughtIntoUkFromSmallProducers" -> true,
    "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
    "claimCreditsForExports" -> true,
    "howManyCreditsForExport" -> Json.obj("lowBand" -> 100, "highBand" -> 200),
    "claimCreditsForLostDamaged" -> true,
    "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 100, "highBand" -> 200))
  val superCola: SmallProducer = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
  val sparkyJuice: SmallProducer = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (1000L, 2000L))
  val userAnswers: UserAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola))

  "Levy Calculator " - {
    "should calculate a levy based on low band and high band for each page" in {
      val levyCalculator = new LevyCalculator(mockConfig) {
        override val lowBandRate = 0.18
        override val highBandRate = 0.24
      }

      val result = levyCalculator.calculateLevyForAnswers(userAnswers)
      val expectedResults = Map(
        BrandsPackagedAtOwnSitesPage.toString -> SdilCalculation(180.0, 480.0),
        HowManyAsAContractPackerPage.toString -> SdilCalculation(180.0, 480.0),
        ExemptionsForSmallProducersPage.toString -> SdilCalculation(360.0, 960.0),
        HowManyBroughtIntoUkPage.toString -> SdilCalculation(180.0, 480.0),
        HowManyBroughtIntoTheUKFromSmallProducersPage.toString -> SdilCalculation(180.0, 480.0),
        HowManyCreditsForExportPage.toString -> SdilCalculation(-18.0, -48.0),
        HowManyCreditsForLostDamagedPage.toString -> SdilCalculation(-18.0, -48.0))

      result mustBe expectedResults
    }
  }

}
