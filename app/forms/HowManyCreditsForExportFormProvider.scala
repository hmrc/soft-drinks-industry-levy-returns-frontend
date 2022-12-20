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
import models.HowManyCreditsForExport

class HowManyCreditsForExportFormProvider @Inject() extends Mappings {

   def apply(): Form[HowManyCreditsForExport] = Form(
     mapping(
      "lowBandLitres" -> long(
        "howManyCreditsForExport.error.lowBandLitres.required",
                    "howManyCreditsForExport.error.lowBandLitres.negative",
                    "howManyCreditsForExport.error.lowBandLitres.nonNumeric",
                    "howManyCreditsForExport.error.lowBandLitres.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyCreditsForExport.error.lowBandLitres.outOfMaxVal")),
        "highBandLitres" -> long(
        "howManyCreditsForExport.error.highBandLitres.required",
                    "howManyCreditsForExport.error.highBandLitres.negative",
                    "howManyCreditsForExport.error.highBandLitres.nonNumeric",
                    "howManyCreditsForExport.error.highBandLitres.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyCreditsForExport.error.highBandLitres.outOfMaxVal"))
    )(HowManyCreditsForExport.apply)(HowManyCreditsForExport.unapply)
   )
 }