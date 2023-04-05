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

package viewmodels

import base.SpecBase
import models.backend.{Site, UkAddress}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import viewmodels.checkAnswers.PackagingSiteDetailsSummary

class packagingSiteDetailsSummarySpec extends SpecBase {

  val addressWith3AddressLines = Site(
      UkAddress(List("The house", "The Road", "ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg"), "NW88 8II"),
      None,
      Some("Test trading name 1"),
      None)

  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  val address44Characters = Site(
    UkAddress(List("29 Station Rd", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val address45Characters = Site(
    UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    None,
    None)

  val address47Characters = Site(
    UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val address49Characters = Site(
    UkAddress(List("29 Station PlaceDr", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    None,
    None)

  val address50Characters = Site(
    UkAddress(List("29 Station Place Dr", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val PackagingSiteEvenLongerAddress = Site(
    UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val packagingSiteListWith2 = List(PackagingSite1, addressWith3AddressLines)
  val packagingSiteListWith1 = List(PackagingSite1)

  "row2" - {

    "should return an empty list of summaryListRows when no packaging site list is passed in" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(List())

      packagingSiteSummaryRowList mustBe List()
    }
    "should return an edit action when 1 packaging site is passed in" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(packagingSiteListWith1)
      packagingSiteSummaryRowList.mkString must include("Edit")
    }

    "must not return a remove action if only 1 packaging site is passed in" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(packagingSiteListWith1)

      packagingSiteSummaryRowList.mkString mustNot include("Remove")
    }
  }

  "address formatting within Row2" - {

    "should place a break after a trading name if a trading name is used" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(List(addressWith3AddressLines))
      val expectedAddressContent = HtmlContent("Test trading name 1<br>The house, The Road, ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg, NW88 8II")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }

    "should not place a break before the post code if the address line and post code length is 44 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(List(address44Characters))
      val expectedAddressContent = HtmlContent("29 Station Rd, The Railyard, Cambridge, CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }

    "should place a break before the post code if the address line and post code length is between 45 and 49 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(List(address45Characters, address47Characters, address49Characters ))
      val expectedAddressContent45 = HtmlContent("29 Station Pl., The Railyard, Cambridge, <br>CB1 2FP")
      val expectedAddressContent47 = HtmlContent("29 Station Place, The Railyard, Cambridge, <br>CB1 2FP")
      val expectedAddressContent49 = HtmlContent("29 Station PlaceDr, The Railyard, Cambridge, <br>CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent45
      packagingSiteSummaryRowList.apply(1).key.content mustBe expectedAddressContent47
      packagingSiteSummaryRowList.last.key.content mustBe expectedAddressContent49
    }

    "should not place a break before the post code if the address line and post code length is 50 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(List(address50Characters))
      val expectedAddressContent = HtmlContent("29 Station Place Dr, The Railyard, Cambridge, CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }
    "should autowrap and place a break before the post code if the address line and post code length is between 98 & 103 characters" in {
    val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(List(PackagingSiteEvenLongerAddress))
    val expectedAddressContent = HtmlContent("29 Station Rd, This address will auto wrap but not in postcode, it is 4 lines 103 char, Cambridge, <br>CB1 2FP")

    packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }

  }

}
