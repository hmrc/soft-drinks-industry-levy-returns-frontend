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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.BrandsPackagedAtOwnSites

class BrandsPackagedAtOwnSitesFormProvider @Inject() extends Mappings {

   def apply(): Form[BrandsPackagedAtOwnSites] = Form(
     mapping(
      "lowBand" -> long(
        "error.lowBand.required",
                    "error.lowBand.negative",
                    "error.lowBand.nonNumeric",
                    "error.lowBand.wholeNumber",
        "error.lowBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "error.lowBand.outOfMaxVal")),
        "highBand" -> long(
        "error.highBand.required",
                    "error.highBand.negative",
                    "error.highBand.nonNumeric",
                    "error.highBand.wholeNumber",
          "error.highBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "error.highBand.outOfMaxVal"))
    )(BrandsPackagedAtOwnSites.apply)(BrandsPackagedAtOwnSites.unapply)
   )
 }
