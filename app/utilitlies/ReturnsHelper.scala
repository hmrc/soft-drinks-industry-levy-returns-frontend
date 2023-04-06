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

package utilitlies

import models.requests.DataRequest
import models.{FinancialLineItem, SdilReturn, SmallProducer, UserAnswers}
import play.api.libs.json.Json
import play.api.mvc.AnyContent

object ReturnsHelper {

  def noActivityUserAnswers(sdilEnrolment: String) =
    UserAnswers(sdilEnrolment,
      Json.obj(
        "ownBrands" -> false,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "packagedContractPacker" -> false,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "exemptionsForSmallProducers" -> false,
        "addASmallProducer" -> Json.obj("referenceNumber" -> s"$sdilEnrolment", "lowBand" -> 0, "highBand" -> 0),
        "smallProducerDetails" -> false,
        "broughtIntoUK" -> false,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "broughtIntoUkFromSmallProducers" -> false,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForExports" -> false,
        "howManyCreditsForExport" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
        "claimCreditsForLostDamaged" -> false,
        "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
      ))

  def emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))

  def extractReturnPeriod(request: DataRequest[AnyContent]) = {
    request.returnPeriod match {
      case Some(period) => period
      case _ => throw new RuntimeException(s"Request does not contain return period for ${request.returnPeriod}")
    }
  }

  def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
    items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
      (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
    }

  def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal = l.headOption.fold(BigDecimal(0))(_._2)

}
