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

package base

import models.backend.{Site, UkAddress}
import models.{LitresInBands, SmallProducer, UserAnswers}
import play.api.libs.json.{JsBoolean, JsObject, Json}

import java.time.Instant

object UserAnswersTestData {

  val litresInBands = LitresInBands(1000, 1000)
  val litresInBandsOnlyLow = LitresInBands(1000, 0)
  val litresInBandsOnlyHigh = LitresInBands(0, 1000)
  val litresInBandsAll0 = LitresInBands(0, 0)

  val sdilRef = "XKSDIL000000022"

  val litresInBandsOptions = Seq(litresInBands, litresInBandsOnlyLow, litresInBandsOnlyHigh, litresInBandsAll0)

  val dataWithQuestionsAllTrueAndAllLitresInAllBands = Json.obj(
      ("ownBrands", JsBoolean(true)),
      ("brandsPackagedAtOwnSites", Json.toJson(litresInBands)),
      ("packagedContractPacker", JsBoolean(true)),
      ("howManyAsAContractPacker", Json.toJson(litresInBands)),
      ("exemptionsForSmallProducers", JsBoolean(true)),
      ("broughtIntoUK", JsBoolean(true)),
      ("HowManyBroughtIntoUk", Json.toJson(litresInBands)),
      ("broughtIntoUkFromSmallProducers", JsBoolean(true)),
      ("howManyBroughtIntoTheUKFromSmallProducers", Json.toJson(litresInBands)),
      ("claimCreditsForExports", JsBoolean(true)),
      ("howManyCreditsForExport", Json.toJson(litresInBands)),
      ("claimCreditsForLostDamaged", JsBoolean(true)),
      ("howManyCreditsForLostDamaged", Json.toJson(litresInBands)),
      ("packAtBusinessAddress", JsBoolean(true))
  )

  val dataWithQuestionsAllTrueAndAllLitresInLowBandsOnly = Json.obj(
    ("ownBrands", JsBoolean(true)),
    ("brandsPackagedAtOwnSites", Json.toJson(litresInBandsOnlyLow)),
    ("packagedContractPacker", JsBoolean(true)),
    ("howManyAsAContractPacker", Json.toJson(litresInBandsOnlyLow)),
    ("exemptionsForSmallProducers", JsBoolean(true)),
    ("broughtIntoUK", JsBoolean(true)),
    ("HowManyBroughtIntoUk", Json.toJson(litresInBandsOnlyLow)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(true)),
    ("howManyBroughtIntoTheUKFromSmallProducers", Json.toJson(litresInBandsOnlyLow)),
    ("claimCreditsForExports", JsBoolean(true)),
    ("howManyCreditsForExport", Json.toJson(litresInBandsOnlyLow)),
    ("claimCreditsForLostDamaged", JsBoolean(true)),
    ("howManyCreditsForLostDamaged", Json.toJson(litresInBandsOnlyLow)),
    ("packAtBusinessAddress", JsBoolean(true))
  )

  val dataWithQuestionsAllTrueAndAllLitresInHighBandsOnly = Json.obj(
    ("ownBrands", JsBoolean(true)),
    ("brandsPackagedAtOwnSites", Json.toJson(litresInBandsOnlyHigh)),
    ("packagedContractPacker", JsBoolean(true)),
    ("howManyAsAContractPacker", Json.toJson(litresInBandsOnlyHigh)),
    ("exemptionsForSmallProducers", JsBoolean(true)),
    ("broughtIntoUK", JsBoolean(true)),
    ("HowManyBroughtIntoUk", Json.toJson(litresInBandsOnlyHigh)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(true)),
    ("howManyBroughtIntoTheUKFromSmallProducers", Json.toJson(litresInBandsOnlyHigh)),
    ("claimCreditsForExports", JsBoolean(true)),
    ("howManyCreditsForExport", Json.toJson(litresInBandsOnlyHigh)),
    ("claimCreditsForLostDamaged", JsBoolean(true)),
    ("howManyCreditsForLostDamaged", Json.toJson(litresInBandsOnlyHigh)),
    ("packAtBusinessAddress", JsBoolean(true))
  )

  val dataWithQuestionsAllTrueAndAllLitresInAllBands0 = Json.obj(
    ("ownBrands", JsBoolean(true)),
    ("brandsPackagedAtOwnSites", Json.toJson(litresInBandsAll0)),
    ("packagedContractPacker", JsBoolean(true)),
    ("howManyAsAContractPacker", Json.toJson(litresInBandsAll0)),
    ("exemptionsForSmallProducers", JsBoolean(true)),
    ("broughtIntoUK", JsBoolean(true)),
    ("HowManyBroughtIntoUk", Json.toJson(litresInBandsAll0)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(true)),
    ("howManyBroughtIntoTheUKFromSmallProducers", Json.toJson(litresInBandsAll0)),
    ("claimCreditsForExports", JsBoolean(true)),
    ("howManyCreditsForExport", Json.toJson(litresInBandsAll0)),
    ("claimCreditsForLostDamaged", JsBoolean(true)),
    ("howManyCreditsForLostDamaged", Json.toJson(litresInBandsAll0)),
    ("packAtBusinessAddress", JsBoolean(true))
  )

  val dataWithQuestionsAllFalseAndAllLitres = Json.obj(
    ("ownBrands", JsBoolean(false)),
    ("brandsPackagedAtOwnSites", Json.toJson(litresInBands)),
    ("packagedContractPacker", JsBoolean(false)),
    ("howManyAsAContractPacker", Json.toJson(litresInBands)),
    ("exemptionsForSmallProducers", JsBoolean(false)),
    ("broughtIntoUK", JsBoolean(false)),
    ("HowManyBroughtIntoUk", Json.toJson(litresInBands)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(false)),
    ("howManyBroughtIntoTheUKFromSmallProducers", Json.toJson(litresInBands)),
    ("claimCreditsForExports", JsBoolean(false)),
    ("howManyCreditsForExport", Json.toJson(litresInBands)),
    ("claimCreditsForLostDamaged", JsBoolean(false)),
    ("howManyCreditsForLostDamaged", Json.toJson(litresInBands)),
    ("packAtBusinessAddress", JsBoolean(false))
  )

  val dataWithQuestionsAllTrueAndNoLitres = Json.obj(
    ("ownBrands", JsBoolean(true)),
    ("packagedContractPacker", JsBoolean(true)),
    ("exemptionsForSmallProducers", JsBoolean(true)),
    ("broughtIntoUK", JsBoolean(true)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(true)),
    ("claimCreditsForExports", JsBoolean(true)),
    ("claimCreditsForLostDamaged", JsBoolean(true)),
    ("packAtBusinessAddress", JsBoolean(true))
  )

  val dataWithQuestionsAllFalseAndNoLitres = Json.obj(
    ("ownBrands", JsBoolean(false)),
    ("packagedContractPacker", JsBoolean(false)),
    ("exemptionsForSmallProducers", JsBoolean(false)),
    ("broughtIntoUK", JsBoolean(false)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(false)),
    ("claimCreditsForExports", JsBoolean(false)),
    ("claimCreditsForLostDamaged", JsBoolean(false)),
    ("packAtBusinessAddress", JsBoolean(false))
  )

  val smallProducerJourneyDataWithQuestionsAllFalseAndNoLitres: JsObject = Json.obj(
    ("packagedContractPacker", JsBoolean(false)),
    ("exemptionsForSmallProducers", JsBoolean(false)),
    ("broughtIntoUK", JsBoolean(false)),
    ("broughtIntoUkFromSmallProducers", JsBoolean(false)),
    ("claimCreditsForExports", JsBoolean(false)),
    ("claimCreditsForLostDamaged", JsBoolean(false)),
    ("packAtBusinessAddress", JsBoolean(false))
  )

  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (2000L, 1000L))

  val smallProducerList1Producer = List(superCola)
  val smallProducerList2Producers = List(superCola, sparkyJuice)
  val smallProducerList0Litres = List(superCola.copy(litreage = (0L, 0L)))
  val smallProducerListHighBandOnlyLitres = List(superCola.copy(litreage = (0L, 2000L)))
  val smallProducerListLowBandOnlyLitres = List(superCola.copy(litreage = (1000L, 0L)))


  val packagingSite = Site(
    UkAddress(List("29 Station Rd", "example", "test", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val packagingSiteList = Map("90831480921" -> packagingSite)

  val emptyUserDetails = create(Json.obj())
  val userIsSmallProducer = createSmallProducer(smallProducerJourneyDataWithQuestionsAllFalseAndNoLitres)
  val withQuestionsAllTrueAllLitresInAllBands1SmallProducer = create(dataWithQuestionsAllTrueAndAllLitresInAllBands, smallProducerList1Producer, true)

  val withQuestionsAllTrueAllLitresInAllBands2SmallProducer = create(dataWithQuestionsAllTrueAndAllLitresInAllBands, smallProducerList2Producers, true)

  val withQuestionsAllTrueAndAllLitresInLowBandsOnly = create(dataWithQuestionsAllTrueAndAllLitresInLowBandsOnly, smallProducerListLowBandOnlyLitres, withPackagingSite = true)
  val withQuestionsAllTrueAndAllLitresInHighBandsOnly = create(dataWithQuestionsAllTrueAndAllLitresInHighBandsOnly, smallProducerListHighBandOnlyLitres, withPackagingSite = true)
  val withQuestionsAllTrueAndAllLitresInAllBands0 = create(dataWithQuestionsAllTrueAndAllLitresInAllBands0, smallProducerList0Litres, withPackagingSite =  true)
  val withQuestionsAllFalseAndAllLitres = create(dataWithQuestionsAllFalseAndAllLitres, smallProducerList1Producer)
  val withQuestionsAllTrueAndNoLitres = create(dataWithQuestionsAllTrueAndNoLitres, withPackagingSite = true)
  val withQuestionsAllFalseAndNoLitres = create(dataWithQuestionsAllFalseAndNoLitres)

  def questionFieldsAllTrue(key: String): Boolean = key.contains("all boolean fields true")
  def questionFieldsAllFalse(key: String): Boolean = key.contains("all boolean fields false")
  def has1SmallProducer(key: String): Boolean = key.contains("1 small producer")
  def has2SmallProducer(key: String): Boolean = key.contains("2 small producer")
  def includesLitresInAllBands(key: String): Boolean = key.contains("litres in all bands")
  def includesLitresInLowBandOnly(key: String): Boolean = key.contains("litres in low band only")
  def includesLitresInHighBandOnly(key: String): Boolean = key.contains("litres in high band only")
  def includesLitresInBothBands0(key: String): Boolean = key.contains("litres in both bands 0")
  def includesNoLitres(key: String): Boolean = key.contains("no litres") || key.contains("user answer has no data")

  def litresDefaultToZero(key: String, band: String): Boolean = {
    band match {
      case "lowband" => includesNoLitres(key) || includesLitresInBothBands0(key) || includesLitresInHighBandOnly(key)
      case _ => includesNoLitres(key) || includesLitresInBothBands0(key) || includesLitresInLowBandOnly(key)
    }
  }

  val userAnswersModels = Map(
    "user answer has no data" -> emptyUserDetails,
    "user answers has all boolean fields true, litres in all bands and 1 small producer" -> withQuestionsAllTrueAllLitresInAllBands1SmallProducer,
    "user answers has all boolean fields true, litres in all bands and 2 small producer" -> withQuestionsAllTrueAllLitresInAllBands2SmallProducer,
    "user answers has all boolean fields true and litres in low band only" -> withQuestionsAllTrueAndAllLitresInLowBandsOnly,
    "user answers has all boolean fields true and litres in high band only" -> withQuestionsAllTrueAndAllLitresInHighBandsOnly,
    "user answers has all boolean fields true and litres in both bands 0" -> withQuestionsAllTrueAndAllLitresInAllBands0,
    "user answers has all boolean fields false and litres in all bands" -> withQuestionsAllFalseAndAllLitres,
    "user answers has all boolean fields true and no litres" -> withQuestionsAllTrueAndNoLitres,
    "user answers has all boolean fields false and no litres" -> withQuestionsAllFalseAndNoLitres
  )

  private def create(data: JsObject, smallProducerList: List[SmallProducer] = List.empty, withPackagingSite: Boolean = false): UserAnswers = {
    UserAnswers(
      id = sdilRef,
      data = data,
      smallProducerList = smallProducerList,
      packagingSiteList = if (withPackagingSite) {
        packagingSiteList
      } else {
        Map.empty
      },
      lastUpdated = Instant.now()
    )
  }

    private def createSmallProducer(data: JsObject, smallProducerList: List[SmallProducer] = List.empty, withPackagingSite: Boolean = false): UserAnswers = {
      UserAnswers(
        id = "XGSDIL000001611",
        data = data,
        smallProducerList = smallProducerList,
        packagingSiteList = if (withPackagingSite) {
          packagingSiteList
        } else {
          Map.empty
        },
        lastUpdated = Instant.now()
      )
  }
}
