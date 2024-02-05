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

package views.detailsPages

import base.ReturnsTestData._
import base.SpecBase
import models.backend.{Site, UkAddress}

class SecondaryWarehouseDetailsSummarySpec extends SpecBase {

  val warehouseAddressMapWith3AddressLines: Map[String, Site] = Map(("2", Site(
    address = UkAddress(List("The house", "The Road", "ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg"), "NW88 8II"),
    tradingName = Some("Warehouse Group"))))

  val warehouseMap: Map[String, Site] = Map(("1", Site(
    tradingName = Some("Warehouse Group"),
    address = UkAddress(List("The house", "The Road", "ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg"), "NW88 8II"))), ("24", WarehouseSite1))

  "warehouse detail row " - {

    "should return an empty list of summaryListRows when no warehouse list is passed in" in {
      val warehouseSummaryRowList = SecondaryWarehouseDetailsSummary.warehouseDetailRow(Map.empty)

      warehouseSummaryRowList mustBe List()
    }

    "must return a remove action when at least 1 site is passed in" in {
      val warehouseSummaryRowList = SecondaryWarehouseDetailsSummary.warehouseDetailRow(warehouseAddressMapWith3AddressLines)

      warehouseSummaryRowList.mkString must include("Remove")
    }

    "must include Correct elements in list with 2 elements" in {

      val warehouseSummaryRowList = SecondaryWarehouseDetailsSummary.warehouseDetailRow(warehouseMap)
      warehouseSummaryRowList.head.key.content.asHtml.toString() mustBe "Warehouse Group<br>The house, The Road, ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg, NW88 8II"
      warehouseSummaryRowList.head.actions.toList.head.items.last.content.asHtml.toString() mustBe "Remove"
      warehouseSummaryRowList.head.actions.toList.head.items.last.href mustBe controllers.routes.RemoveWarehouseConfirmController.onPageLoad("1").url

      warehouseSummaryRowList.last.key.content.asHtml.toString() mustBe "Wild Lemonade Group<br>33 Rhes Priordy, East London, E73 2RP"
      warehouseSummaryRowList.last.actions.toList.head.items.last.content.asHtml.toString() mustBe "Remove"
      warehouseSummaryRowList.last.actions.toList.head.items.last.href mustBe controllers.routes.RemoveWarehouseConfirmController.onPageLoad("24").url
    }
  }

}
