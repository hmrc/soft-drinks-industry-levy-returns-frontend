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
import models.backend.UkAddress
import play.twirl.api.Html

class AddressFormattingHelperSpec extends SpecBase {
  "AddressFormattingHelper" - {
    "address formatting" - {
      "should place a break after a trading name if a trading name is used" in {
        val addressWith3AddressLines = UkAddress(List("The house", "The Road", "ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg"), "NW88 8II")
        val tradingName = Some("Test trading name 1")

        val result = AddressFormattingHelper.addressFormatting(addressWith3AddressLines, tradingName)
        val expectedAddressContent = Html("Test trading name 1<br>The house, The Road, ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg, NW88 8II")

        result mustBe expectedAddressContent
      }

      "should not place a break before the post code if the address line and post code length is 44 characters" in {
        val address44Characters = UkAddress(List("29 Station Rd", "The Railyard", "Cambridge"), "CB1 2FP")
        val result = AddressFormattingHelper.addressFormatting(address44Characters, None)
        val expectedAddressContent = Html("29 Station Rd, The Railyard, Cambridge, CB1 2FP")

        result mustBe expectedAddressContent
      }

      "should place a break before the post code if the address line and post code length is between 45 and 49 characters" in {
        val address45Characters = UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP")
        val result = AddressFormattingHelper.addressFormatting(address45Characters, None)
        val expectedAddressContent45 = Html("29 Station Pl., The Railyard, Cambridge, <br>CB1 2FP")

        result mustBe expectedAddressContent45
      }

      "should not place a break before the post code if the address line and post code length is 50 characters" in {
        val address50Characters = UkAddress(List("29 Station Place Dr", "The Railyard", "Cambridge"), "CB1 2FP")
        val result = AddressFormattingHelper.addressFormatting(address50Characters, None)
        val expectedAddressContent = Html("29 Station Place Dr, The Railyard, Cambridge, CB1 2FP")

        result mustBe expectedAddressContent
      }
      "should autowrap and place a break before the post code if the address line and post code length is between 98 & 103 characters no Trading name" in {
        val addressGreaterThan98 = UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP")
        val result = AddressFormattingHelper.addressFormatting(addressGreaterThan98, None)
        val expectedAddressContent = Html("29 Station Rd, This address will auto wrap but not in postcode, it is 4 lines 103 char, Cambridge, <br>CB1 2FP")

        result mustBe expectedAddressContent
      }

      "should place a break after a trading name AND autowrap and place a break before the post code if the address line " +
        "and post code length is between 98 & 103 characters" in {
        val addressGreaterThan98 =  UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP")
        val result = AddressFormattingHelper.addressFormatting(addressGreaterThan98, Some("Test Trading Name Inc"))
        val expectedAddressContent = Html("Test Trading Name Inc<br>29 Station Rd, This address will auto wrap but not " +
          "in postcode, it is 4 lines 103 char, Cambridge, <br>CB1 2FP")

        result mustBe expectedAddressContent
      }
    }
  }

