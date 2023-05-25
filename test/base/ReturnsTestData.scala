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

import models.alf.{AlfAddress, AlfResponse}
import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{Amounts, ReturnCharge, ReturnPeriod, ReturnsVariation, SdilReturn, SmallProducer, UserAnswers}
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

object ReturnsTestData {

  val defaultReturnsPeriod = ReturnPeriod(2018, 2)
  val organisation = "soft drinks ltd"
  val addressLine1 = "line 1"
  val veryLongAddressLine1 = "liiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiine 1"
  val foreignCharsLine1 = "1 Falsche Straße"
  val addressLine2 = "line 2"
  val addressLine3 = "line 3"
  val addressLine4 = "line 4"
  val postcode = "aa1 1aa"
  val countryName = "United Kingdom"
  val countryCode = "UK"
  val litreage = (10L, 20L)
  val id = "id"
  val superColaProducerAlias = "Super Cola Ltd"
  val superJuiceProducerAlias = "Super Juice Ltd"
  val referenceNumber1 = "XZSDIL000000234"
  val referenceNumber2 = "XZSDIL000000235"

  val producerName = "Super Cola Plc"
  val sdilReference = "XCSDIL000000069"
  val producerNameParty = "Soft Juice"
  val sdilReferenceParty = "XMSDIL000000113"
  val bandMax: Long = 100000000000000L
  val litres: Long = bandMax - 1
  val smallProducerList: List[SmallProducer] = List(SmallProducer(producerNameParty, sdilReferenceParty, (litres, litres)))
  val smallProducerListWithTwoProducers: List[SmallProducer] = List(
    SmallProducer(producerNameParty, sdilReferenceParty, (litres, litres)),
    SmallProducer(producerName, sdilReference, (litres, litres))
  )
  val smallProducerListOnlySuperCola: List[SmallProducer] = List(SmallProducer(producerName, sdilReference, (litres, litres)))

  val customerAddressJsonError: JsObject = Json.obj(
    "address" -> Json.obj(
      "lines" -> 4
    )
  )
  val customerAddressMax: AlfResponse = AlfResponse(
    AlfAddress(
      Some(organisation),
      List(addressLine1, addressLine2, addressLine3, addressLine4),
      Some(postcode),
      Some(countryCode)
    ))

  val customerAddressMaxJson = Json.toJson(AlfResponse(
    AlfAddress(
      Some(organisation),
      List(addressLine1, addressLine2, addressLine3, addressLine4),
      Some(postcode),
      Some(countryCode)
    )))

  val returnPeriod = ReturnPeriod(2022, 1)
  val returnPeriods = List(ReturnPeriod(2018, 1), ReturnPeriod(2019, 1))
  val genericSmallProducerAlias = "Generic Producer LTD"
  val baseUrl = "/soft-drinks-industry-levy-returns-frontend"
  val baseAlias = "Jackson's Drinks"
  val baseLitreage = 100L
  val sdilNumber: String = "XKSDIL000000022"
  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1L, 1L))
  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (100L, 100L))
  val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(-100))
  val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(-200))
  val financialItemList = List(financialItem1, financialItem2)

  val baseSessionData =
    Json.obj(
      "producerName" -> baseAlias,
      "referenceNumber" -> sdilNumber,
      "lowBand" -> baseLitreage,
      "highBand" -> baseLitreage
    )

  val aSubscription = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  lazy val subscriptionWithCopacker = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = false,
      voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  lazy val emptyUserAnswers = UserAnswers(sdilNumber, Json.obj())
  lazy val submittedAnswers = UserAnswers(sdilNumber, Json.obj(), submitted = true)
  lazy val completedUserAnswers = UserAnswers(sdilNumber, Json.obj("ownBrands" -> false, "packagedContractPacker" ->
    true, "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
    "exemptionsForSmallProducers" -> false, "broughtIntoUK" -> true, "HowManyBroughtIntoUk" -> Json.obj(
      "lowBand" -> 259, "highBand" -> 923), "broughtIntoUkFromSmallProducers" -> false, "claimCreditsForExports"
      -> false, "claimCreditsForLostDamaged" -> false), List.empty, Map.empty)

  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  lazy val packagingSiteListWith1 = Map(("78941132", PackagingSite1))

  val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))

  val returnVariationForNilReturn =
    ReturnsVariation(
      orgName = aSubscription.orgName,
      ppobAddress = aSubscription.address,
      importer = (false, (0,0)),
      packer = (false, (0,0)),
      warehouses = List.empty,
      packingSites = List.empty,
      phoneNumber = aSubscription.contact.phoneNumber,
      email = aSubscription.contact.email,
      taxEstimation = 0
    )
  val zero = BigDecimal(0.00)
  val amountsZero = Amounts(zero, zero, zero)
  val amounts = Amounts(1000, 3000, 2000)
}
