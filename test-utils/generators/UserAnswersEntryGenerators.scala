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

import models.{BrandsPackagedAtOwnSites, HowManyAsAContractPacker}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import pages.PackagedContractPackerPage
import pages.OwnBrandsPage
import pages.{BrandsPackagedAtOwnSitesPage, PackagedContractPackerPage,HowManyAsAContractPackerPage}

import play.api.libs.json.{JsValue, Json}



trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryHowManyAsAContractPackerUserAnswersEntry: Arbitrary[(HowManyAsAContractPackerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyAsAContractPackerPage.type]
        value <- arbitrary[HowManyAsAContractPacker].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryackagedContractPackerUserAnswersEntry: Arbitrary[(PackagedContractPackerPage.type, JsValue)] =
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
}
