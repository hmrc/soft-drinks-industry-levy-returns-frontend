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


trait ModelGenerators {

  implicit lazy val arbitraryAddASmallProducer: Arbitrary[AddASmallProducer] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield AddASmallProducer(lowBand, highBand)
    }

  implicit lazy val arbitraryHowManyCreditsForLostDamaged: Arbitrary[HowManyCreditsForLostDamaged] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield HowManyCreditsForLostDamaged(lowBand, highBand)
    }

  implicit lazy val arbitraryHowManyBoughtIntoUk: Arbitrary[HowManyBroughtIntoUk] =

    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield HowManyBroughtIntoUk(lowBand, highBand)
    }

  implicit lazy val arbitraryHowManyCreditsForExport: Arbitrary[HowManyCreditsForExport] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield HowManyCreditsForExport(lowBand, highBand)
    }

  implicit lazy val arbitraryHowManyBroughtIntoTheUKFromSmallProducers: Arbitrary[HowManyBroughtIntoTheUKFromSmallProducers] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield HowManyBroughtIntoTheUKFromSmallProducers(lowBand, highBand)
    }

  implicit lazy val arbitraryBrandsPackagedAtOwnSites: Arbitrary[BrandsPackagedAtOwnSites] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield BrandsPackagedAtOwnSites(lowBand, highBand)
    }

  implicit lazy val arbitraryHowManyAsAContractPacker: Arbitrary[HowManyAsAContractPacker] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield HowManyAsAContractPacker(lowBand, highBand)
    }
}
