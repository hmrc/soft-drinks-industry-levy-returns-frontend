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

package views.helpers.returnDetails

import base.ReturnsTestData._
import base.SpecBase
import models.NormalMode
import models.backend.{ Site, UkAddress }
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import views.helpers.returnDetails.PackagingSiteDetailsSummary

class PackagingSiteDetailsSummarySpec extends SpecBase {

  val addressWith3AddressLines = Site(
    UkAddress(List("The house", "The Road", "ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg"), "NW88 8II"),
    None,
    Some("Test trading name 1"),
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

  val PackagingSiteEvenLongerAddressNoTradeName = Site(
    UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val PackagingSiteEvenLongerAddressWithTradeName = Site(
    UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP"),
    Some("10"),
    Some("Test Trading Name Inc"),
    None)

  val packagingSiteListWith3 = Map(("rieajnldkaljnk13", address45Characters), ("jfkladnlr12", address47Characters), ("jgklaj;ll;e;o", address49Characters))

  "row2" - {

    "should return an empty list of summaryListRows when no packaging site list is passed in" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map.empty)

      packagingSiteSummaryRowList mustBe List()
    }

    "must not return a remove action if only 1 packaging site is passed in" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(packagingSiteListWith1)

      packagingSiteSummaryRowList.mkString mustNot include("Remove")
    }
    "must include Correct elements in list with 2 elements" in {
      val site1 = Site(
        UkAddress(List("foo2", "bar2"), "wizz2"),
        None,
        Some("trade2"),
        None)
      val site2 = Site(
        UkAddress(List("foo", "bar"), "wizz"),
        None,
        Some("trade"),
        None)
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map("ref1" -> site1, "ref2" -> site2))
      packagingSiteSummaryRowList.head.key.content.asHtml.toString() mustBe "trade2<br>foo2, bar2, wizz2"
      packagingSiteSummaryRowList.head.actions.toList.head.items.last.content.asHtml.toString() mustBe "Remove"
      packagingSiteSummaryRowList.head.actions.toList.head.items.last.href mustBe controllers.routes.RemovePackagingDetailsConfirmationController.onPageLoad(NormalMode, "ref1").url

      packagingSiteSummaryRowList.last.key.content.asHtml.toString() mustBe "trade<br>foo, bar, wizz"
      packagingSiteSummaryRowList.last.actions.toList.head.items.last.content.asHtml.toString() mustBe "Remove"
      packagingSiteSummaryRowList.last.actions.toList.head.items.last.href mustBe controllers.routes.RemovePackagingDetailsConfirmationController.onPageLoad(NormalMode, "ref2").url
    }
  }

  "address formatting within Row2" - {

    "should place a break after a trading name if a trading name is used" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map(("90831480921", addressWith3AddressLines)))
      val expectedAddressContent = HtmlContent("Test trading name 1<br>The house, The Road, ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg, NW88 8II")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }

    "should not place a break before the post code if the address line and post code length is 44 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map(("1208934391", address44Characters)))
      val expectedAddressContent = HtmlContent("29 Station Rd, The Railyard, Cambridge, CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }

    "should place a break before the post code if the address line and post code length is between 45 and 49 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(packagingSiteListWith3)
      val expectedAddressContent45 = HtmlContent("29 Station Pl., The Railyard, Cambridge, <br>CB1 2FP")
      val expectedAddressContent47 = HtmlContent("29 Station Place, The Railyard, Cambridge, <br>CB1 2FP")
      val expectedAddressContent49 = HtmlContent("29 Station PlaceDr, The Railyard, Cambridge, <br>CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent45
      packagingSiteSummaryRowList.apply(1).key.content mustBe expectedAddressContent47
      packagingSiteSummaryRowList.last.key.content mustBe expectedAddressContent49
    }

    "should not place a break before the post code if the address line and post code length is 50 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map(("3489028394r", address50Characters)))
      val expectedAddressContent = HtmlContent("29 Station Place Dr, The Railyard, Cambridge, CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }
    "should autowrap and place a break before the post code if the address line and post code length is between 98 & 103 characters" in {
      val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map(("45641351", PackagingSiteEvenLongerAddressNoTradeName)))
      val expectedAddressContent = HtmlContent("29 Station Rd, This address will auto wrap but not in postcode, it is 4 lines 103 char, Cambridge, <br>CB1 2FP")

      packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
    }

    "should place a break after a trading name AND autowrap and place a break before the post code if the address line " +
      "and post code length is between 98 & 103 characters" in {
        val packagingSiteSummaryRowList = PackagingSiteDetailsSummary.row2(Map(("56458678", PackagingSiteEvenLongerAddressWithTradeName)))
        val expectedAddressContent = HtmlContent("Test Trading Name Inc<br>29 Station Rd, This address will auto wrap but not " +
          "in postcode, it is 4 lines 103 char, Cambridge, <br>CB1 2FP")

        packagingSiteSummaryRowList.head.key.content mustBe expectedAddressContent
      }
  }
}
