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

package views

trait ReturnDetailsExpectedResults {

  object SummaryHeadingIds {
    val ownBrands = "ownBrandsPackagedAtYourOwnSite"
    val contractPackedAtYourOwnSite = "contractPackedAtYourOwnSite"
    val contractPackedForRegisteredSmallProducers = "contractPackedForRegisteredSmallProducers"
    val broughtIntoUK = "broughtIntoUK"
    val broughtIntoTheUKFromSmallProducers = "broughtIntoTheUKFromSmallProducers"
    val exported = "exported"
    val lostOrDestroyed = "lostOrDestroyed"
    val amountToPay = "amount-to-pay-title"
    val registeredSites = "registeredUkSites"
  }

  val returnDetailsSummaryListsWithQuestionKey = Map(
    SummaryHeadingIds.ownBrands -> "reportingOwnBrandsPackagedAtYourOwnSite",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "reportingContractPackedAtYourOwnSite",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "exemptionForRegisteredSmallProducers",
    SummaryHeadingIds.broughtIntoUK -> "reportingLiableDrinksBroughtIntoTheUK",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "reportingLiableDrinksBroughtIntoTheUKFromSmallProducers",
    SummaryHeadingIds.exported -> "claimingCreditForExportedLiableDrinks",
    SummaryHeadingIds.lostOrDestroyed -> "claimingCreditForLostOrDestroyedLiableDrinks",
    SummaryHeadingIds.registeredSites -> "You have 1 packaging site"
  )

  val returnDetailsSummaryListsWithListNames = Map(
    SummaryHeadingIds.ownBrands -> "own brands",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "contract packed at your own site",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "contract packed for registered small producers",
    SummaryHeadingIds.broughtIntoUK -> "brought into the UK",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "brought into the UK from small producers",
    SummaryHeadingIds.exported -> "exported",
    SummaryHeadingIds.lostOrDestroyed -> "lost or destroyed",
    SummaryHeadingIds.amountToPay -> "amount to pay",
    SummaryHeadingIds.registeredSites -> "registered sites"
  )

  val returnDetailsSummaryListsWithActionHrefsForQuestion = Map(
    SummaryHeadingIds.ownBrands -> "/change-own-brands-packaged-at-own-sites",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "/change-packaged-as-contract-packer",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "/change-exemptions-for-small-producers",
    SummaryHeadingIds.broughtIntoUK -> "/change-brought-into-uk",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "/change-brought-into-uk-from-small-producers",
    SummaryHeadingIds.exported -> "/change-claim-credits-for-exports",
    SummaryHeadingIds.lostOrDestroyed -> "/change-claim-credits-for-lost-damaged",
    SummaryHeadingIds.registeredSites -> "/change-pack-at-business-address-in-return"
  )

  val returnDetailsSummaryListsWithActionHrefsForLitres = Map(
    SummaryHeadingIds.ownBrands -> "/change-how-many-own-brands-packaged-at-own-sites",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "/change-how-many-packaged-as-contract-packer",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "/change-small-producer-details",
    SummaryHeadingIds.broughtIntoUK -> "/change-how-many-brought-into-uk",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "/change-how-many-into-uk-small-producers",
    SummaryHeadingIds.exported -> "/change-how-many-credits-for-exports",
    SummaryHeadingIds.lostOrDestroyed -> "/change-how-many-credits-for-lost-damaged"
  )

  val returnDetailsSummaryListsWithActionIds = Map(
    SummaryHeadingIds.ownBrands -> "change-own-brands",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "change-contract-packer",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "change-exemption-small-producers",
    SummaryHeadingIds.broughtIntoUK -> "change-brought-into-uk",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "change-brought-into-uk-small-producers",
    SummaryHeadingIds.exported -> "change-exports",
    SummaryHeadingIds.lostOrDestroyed -> "change-credits-lost-damaged",
    SummaryHeadingIds.registeredSites -> "change-packaging-sites"
  )

  val returnDetailsSummaryListsWithActionHiddenKey = Map(
    SummaryHeadingIds.ownBrands -> "ownBrands",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "packagedContractPacker",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "exemptionsForSmallProducers",
    SummaryHeadingIds.broughtIntoUK -> "broughtIntoUK",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "broughtIntoUkFromSmallProducers",
    SummaryHeadingIds.exported -> "claimCreditsForExports",
    SummaryHeadingIds.lostOrDestroyed -> "claimCreditsForLostDamaged",
    SummaryHeadingIds.registeredSites -> "checkYourAnswers.sites.packing"
  )

  val returnDetailsSummaryListsWithLitresActionId = Map(
    SummaryHeadingIds.ownBrands -> "own-site",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "contract-packer",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "small-producers",
    SummaryHeadingIds.broughtIntoUK -> "brought-into-uk",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "brought-into-uk-small-producers",
    SummaryHeadingIds.exported -> "export-credits",
    SummaryHeadingIds.lostOrDestroyed -> "lost-destroyed"
  )

  val returnDetailsSummaryListsWithLitresHiddenKey = Map(
    SummaryHeadingIds.ownBrands -> "ownBrandsPackagedAtYourOwnSite",
    SummaryHeadingIds.contractPackedAtYourOwnSite -> "contractPackedAtYourOwnSite",
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> "contractPackedForRegisteredSmallProducers",
    SummaryHeadingIds.broughtIntoUK -> "broughtIntoUK",
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> "broughtIntoTheUKFromSmallProducers",
    SummaryHeadingIds.exported -> "exported",
    SummaryHeadingIds.lostOrDestroyed -> "lostOrDestroyed"
  )

  val returnDetailsSummaryListsWithArrayElement = Map(
    SummaryHeadingIds.ownBrands -> 0,
    SummaryHeadingIds.contractPackedAtYourOwnSite -> 1,
    SummaryHeadingIds.contractPackedForRegisteredSmallProducers -> 2,
    SummaryHeadingIds.broughtIntoUK -> 3,
    SummaryHeadingIds.broughtIntoTheUKFromSmallProducers -> 4,
    SummaryHeadingIds.exported -> 5,
    SummaryHeadingIds.lostOrDestroyed -> 6,
    SummaryHeadingIds.amountToPay -> 7,
    SummaryHeadingIds.registeredSites -> 8
  )

  def isNegativeLevy(summaryHeadingIds: String): Boolean =
    List(SummaryHeadingIds.exported, SummaryHeadingIds.lostOrDestroyed)
      .contains(summaryHeadingIds)

}
