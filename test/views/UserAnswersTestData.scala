package views

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

  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))

  val smallProducerList1Producer = List(superCola)
  val smallProducerList2Producers = List(superCola, sparkyJuice)

  val emptyUserDetails = create(Json.obj())

  val withQuestionsAllTrueAllLitresInAllBands1SmallProducer = create(dataWithQuestionsAllTrueAndAllLitresInAllBands, smallProducerList1Producer)

  val withQuestionsAllTrueAllLitresInAllBands2SmallProducer = create(dataWithQuestionsAllTrueAndAllLitresInAllBands, smallProducerList2Producers)

  val withQuestionsAllTrueAndAllLitresInLowBandsOnly = create(dataWithQuestionsAllTrueAndAllLitresInLowBandsOnly)
  val withQuestionsAllTrueAndAllLitresInHighBandsOnly = create(dataWithQuestionsAllTrueAndAllLitresInHighBandsOnly)
  val withQuestionsAllTrueAndAllLitresInAllBands0 = create(dataWithQuestionsAllTrueAndAllLitresInAllBands0)
  val withQuestionsAllFalseAndAllLitres = create(dataWithQuestionsAllFalseAndAllLitres)
  val withQuestionsAllTrueAndNoLitres = create(dataWithQuestionsAllTrueAndNoLitres)
  val withQuestionsAllFalseAndNoLitres = create(dataWithQuestionsAllFalseAndNoLitres)

  def questionFieldsAllTrue(key: String): Boolean = key.contains("all boolean fields true")
  def questionFieldsAllFalse(key: String): Boolean = key.contains("all boolean fields false")
  def has1SmallProducer(key: String): Boolean = key.contains("1 small producer")
  def has2SmallProducer(key: String): Boolean = key.contains("2 small producer")
  def includesLitresInAllBands(key: String): Boolean = key.contains("litres in all bands")
  def includesLitresInLowBandOnly(key: String): Boolean = key.contains("litres in low band only")
  def includesLitresInHighBandOnly(key: String): Boolean = key.contains("litres in high band only")
  def includesLitresInBothBands0(key: String): Boolean = key.contains("litres in both bands 0")




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

  private def create(data: JsObject, smallProducerList: List[SmallProducer] = List.empty): UserAnswers = {
    UserAnswers(
      id = sdilRef,
      data = data,
      smallProducerList = smallProducerList,
      lastUpdated = Instant.now()
    )
  }
}
