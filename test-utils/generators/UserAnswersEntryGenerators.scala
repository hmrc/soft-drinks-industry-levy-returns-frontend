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

package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {
  implicit lazy val arbitraryproductionSiteDetailsUserAnswersEntry: Arbitrary[(PackagingSiteDetailsPage.type, JsValue)] =
  Arbitrary {
    for {
      page <- arbitrary[PackagingSiteDetailsPage.type]
      value <- arbitrary[Boolean].map(Json.toJson(_))
    } yield (page, value)
  }
  implicit lazy val arbitraryPackAtBusinessAddressUserAnswersEntry: Arbitrary[(PackAtBusinessAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[PackAtBusinessAddressPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondaryWarehouseDetailsUserAnswersEntry: Arbitrary[(SecondaryWarehouseDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondaryWarehouseDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRemoveSmallProducerConfirmUserAnswersEntry: Arbitrary[(RemoveSmallProducerConfirmPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RemoveSmallProducerConfirmPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySmallProducerDetailsUserAnswersEntry: Arbitrary[(SmallProducerDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SmallProducerDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddASmallProducerUserAnswersEntry: Arbitrary[(AddASmallProducerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddASmallProducerPage.type]
        value <- arbitrary[AddASmallProducer].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimCreditsForExportsUserAnswersEntry: Arbitrary[(ClaimCreditsForExportsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimCreditsForExportsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBroughtIntoUkFromSmallProducersUserAnswersEntry: Arbitrary[(BroughtIntoUkFromSmallProducersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BroughtIntoUkFromSmallProducersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAskSecondaryWarehouseInReturnUserAnswersEntry: Arbitrary[(AskSecondaryWarehouseInReturnPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AskSecondaryWarehouseInReturnPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyCreditsForLostDamagedUserAnswersEntry: Arbitrary[(HowManyCreditsForLostDamagedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyCreditsForLostDamagedPage.type]
        value <- arbitrary[LitresInBands].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyCreditsForExportUserAnswersEntry: Arbitrary[(HowManyCreditsForExportPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyCreditsForExportPage.type]
        value <- arbitrary[HowManyCreditsForExport].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBoughtIntoUKUserAnswersEntry: Arbitrary[(BroughtIntoUKPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BroughtIntoUKPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimCreditsForLostDamagedUserAnswersEntry: Arbitrary[(ClaimCreditsForLostDamagedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimCreditsForLostDamagedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyBroughtIntoTheUKFromSmallProducersUserAnswersEntry:
    Arbitrary[(HowManyBroughtIntoTheUKFromSmallProducersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyBroughtIntoTheUKFromSmallProducersPage.type]
        value <- arbitrary[HowManyBroughtIntoTheUKFromSmallProducers].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyAsAContractPackerUserAnswersEntry: Arbitrary[(HowManyAsAContractPackerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyAsAContractPackerPage.type]
        value <- arbitrary[LitresInBands].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyBoughtIntoUkUserAnswersEntry: Arbitrary[(HowManyBroughtIntoUkPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyBroughtIntoUkPage.type]
        value <- arbitrary[HowManyBroughtIntoUk].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryPackagedContractPackerUserAnswersEntry: Arbitrary[(PackagedContractPackerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PackagedContractPackerPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryOwnBrandsUserAnswersEntry: Arbitrary[(OwnBrandsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OwnBrandsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBrandsPackagedAtOwnSitesUserAnswersEntry: Arbitrary[(BrandsPackagedAtOwnSitesPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BrandsPackagedAtOwnSitesPage.type]
        value <- arbitrary[BrandsPackagedAtOwnSites].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryExemptionsForSmallProducersUserAnswersEntry: Arbitrary[(ExemptionsForSmallProducersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ExemptionsForSmallProducersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }
}
