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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.BrandsPackagedAtOwnSites

class BrandsPackagedAtOwnSitesFormProvider @Inject() extends Mappings {

   def apply(): Form[BrandsPackagedAtOwnSites] = Form(
     mapping(
      "lowBandLitres" -> long1(
        "brandsPackagedAtOwnSites.error.lowBandLitres.required",
                    "brandsPackagedAtOwnSites.error.lowBandLitres.negative",
                    "brandsPackagedAtOwnSites.error.lowBandLitres.nonNumeric",
                    "brandsPackagedAtOwnSites.error.lowBandLitres.wholeNumber",
        "brandsPackagedAtOwnSites.error.lowBandLitres.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "brandsPackagedAtOwnSites.error.lowBandLitres.outOfMaxVal")),
        "highBandLitres" -> long(
        "brandsPackagedAtOwnSites.error.highBandLitres.required",
                    "brandsPackagedAtOwnSites.error.highBandLitres.negative",
                    "brandsPackagedAtOwnSites.error.highBandLitres.nonNumeric",
                    "brandsPackagedAtOwnSites.error.highBandLitres.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "brandsPackagedAtOwnSites.error.highBandLitres.outOfMaxVal"))
    )(BrandsPackagedAtOwnSites.apply)(BrandsPackagedAtOwnSites.unapply)
   )
 }
