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

package models

import cats.implicits._
import models.requests.DataRequest
import pages.{BrandsPackagedAtOwnSitesPage, HowManyAsAContractPackerPage, HowManyBroughtIntoTheUKFromSmallProducersPage, HowManyBroughtIntoUkPage, HowManyCreditsForExportPage, HowManyCreditsForLostDamagedPage}

import java.time.LocalDateTime


case class SdilReturn(
                       ownBrand: (Long, Long),
                       packLarge: (Long, Long),
                       packSmall: List[SmallProducer],
                       importLarge: (Long, Long),
                       importSmall: (Long, Long),
                       export: (Long, Long),
                       wastage: (Long, Long),
                       submittedOn: Option[LocalDateTime] = None
                     ) {

  def totalPacked: (Long, Long) = packLarge |+| packSmall.total
  def totalImported: (Long, Long) = importLarge |+| importSmall

  private def sumLitres(l: List[(Long, Long)]) = l.map(x => LitreOps(x).dueLevy).sum

  def total: BigDecimal = sumLitres(List(ownBrand, packLarge, importLarge)) - sumLitres(List(export, wastage))

  type Litres = Long
  type LitreBands = (Litres, Litres)

  implicit class LitreOps(litreBands: LitreBands) {
    lazy val lowLevy: BigDecimal = litreBands._1 * BigDecimal("0.18")
    lazy val highLevy: BigDecimal = litreBands._2 * BigDecimal("0.24")
    lazy val dueLevy: BigDecimal = lowLevy + highLevy
  }
}

object SdilReturn {
  implicit class SmallProducerDetails(smallProducers: List[SmallProducer]) {
    def total: (Long, Long) = smallProducers.map(x => x.litreage).combineAll
  }

  def apply(userAnswers: UserAnswers)(implicit request: DataRequest[_]): SdilReturn = {
    val lowOwnBrand =  userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L)
    val highOwnBrand = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L)
    val lowPackLarge = userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L)
    val highPackLarge = userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L)
    val packSmall = request.userAnswers.smallProducerList
    val lowImportLarge = userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L)
    val highImportLarge = userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L)
    val lowImportSmall = userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L)
    val highImportSmall= userAnswers.get(HowManyBroughtIntoTheUKFromSmallProducersPage).map(_.highBand).getOrElse(0L)
    val lowExports = userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L)
    val highExports = userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L)
    val lowWastage = userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L)
    val highWastage = userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L)
    SdilReturn(
      ownBrand = (lowOwnBrand, highOwnBrand),
      packLarge = (lowPackLarge, highPackLarge),
      packSmall = packSmall,
      importLarge = (lowImportLarge, highImportLarge),
      importSmall = (lowImportSmall, highImportSmall),
      export = (lowExports, highExports),
      wastage = (lowWastage, highWastage)
    )

  }

}
