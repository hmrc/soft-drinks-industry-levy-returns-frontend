/*
 * Copyright 2022 HM Revenue & Customs
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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryHowManyCreditsForExportPage: Arbitrary[HowManyCreditsForExportPage.type] =
    Arbitrary(HowManyCreditsForExportPage)

  implicit lazy val arbitraryBoughtIntoUKPage: Arbitrary[BroughtIntoUKPage.type] =
    Arbitrary(BroughtIntoUKPage)

  implicit lazy val arbitraryClaimCreditsForLostDamagedPage: Arbitrary[ClaimCreditsForLostDamagedPage.type] =
    Arbitrary(ClaimCreditsForLostDamagedPage)

  implicit lazy val arbitraryHowManyAsAContractPackerPage: Arbitrary[HowManyAsAContractPackerPage.type] =
    Arbitrary(HowManyAsAContractPackerPage)

  implicit lazy val arbitraryHowManyBroughtIntoUkPage: Arbitrary[HowManyBoughtIntoUkPage.type] =
    Arbitrary(HowManyBoughtIntoUkPage)


  implicit lazy val arbitraryPackagedContractPackerPage: Arbitrary[PackagedContractPackerPage.type] =
    Arbitrary(PackagedContractPackerPage)

  implicit lazy val arbitraryOwnBrandsPage: Arbitrary[OwnBrandsPage.type] =
    Arbitrary(OwnBrandsPage)

  implicit lazy val arbitraryBrandsPackagedAtOwnSitesPage: Arbitrary[BrandsPackagedAtOwnSitesPage.type] =
    Arbitrary(BrandsPackagedAtOwnSitesPage)

  implicit lazy val arbitraryExemptionsForSmallProducersPage: Arbitrary[ExemptionsForSmallProducersPage.type] =
    Arbitrary(ExemptionsForSmallProducersPage)
}
