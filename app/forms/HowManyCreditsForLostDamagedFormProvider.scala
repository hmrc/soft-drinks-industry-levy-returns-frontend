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
import models.HowManyCreditsForLostDamaged

class HowManyCreditsForLostDamagedFormProvider @Inject() extends Mappings {

   def apply(): Form[HowManyCreditsForLostDamaged] = Form(
     mapping(
      "lowBand" -> long(
        "howManyCreditsForLostDamaged.error.lowBand.required",
                    "howManyCreditsForLostDamaged.error.lowBand.negative",
                    "howManyCreditsForLostDamaged.error.lowBand.nonNumeric",
                    "howManyCreditsForLostDamaged.error.lowBand.wholeNumber",
        "howManyCreditsForLostDamaged.error.lowBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyCreditsForLostDamaged.error.lowBand.outOfMaxVal")),
        "highBand" -> long(
        "howManyCreditsForLostDamaged.error.highBand.required",
                    "howManyCreditsForLostDamaged.error.highBand.negative",
                    "howManyCreditsForLostDamaged.error.highBand.nonNumeric",
                    "howManyCreditsForLostDamaged.error.highBand.wholeNumber",
          "howManyCreditsForLostDamaged.error.highBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyCreditsForLostDamaged.error.highBand.outOfMaxVal"))
    )(HowManyCreditsForLostDamaged.apply)(HowManyCreditsForLostDamaged.unapply)
   )
 }
