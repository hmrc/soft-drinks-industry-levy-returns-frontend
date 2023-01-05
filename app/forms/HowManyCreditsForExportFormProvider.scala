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
import models.HowManyCreditsForExport

class HowManyCreditsForExportFormProvider @Inject() extends Mappings {

   def apply(): Form[HowManyCreditsForExport] = Form(
     mapping(
      "lowBand" -> long(
        "howManyCreditsForExport.error.lowBand.required",
                    "howManyCreditsForExport.error.lowBand.negative",
                    "howManyCreditsForExport.error.lowBand.nonNumeric",
                    "howManyCreditsForExport.error.lowBand.wholeNumber",
        "howManyCreditsForExport.error.lowBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyCreditsForExport.error.lowBand.outOfMaxVal")),
        "highBand" -> long(
        "howManyCreditsForExport.error.highBand.required",
                    "howManyCreditsForExport.error.highBand.negative",
                    "howManyCreditsForExport.error.highBand.nonNumeric",
                    "howManyCreditsForExport.error.highBand.wholeNumber",
          "howManyCreditsForExport.error.highBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyCreditsForExport.error.highBand.outOfMaxVal"))
    )(HowManyCreditsForExport.apply)(HowManyCreditsForExport.unapply)
   )
 }
