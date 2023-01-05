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
import models.HowManyBroughtIntoUk

class HowManyBoughtIntoUkFormProvider @Inject() extends Mappings {

   def apply(): Form[HowManyBroughtIntoUk] = Form(
     mapping(
      "lowBand" -> long(
        "howManyBoughtIntoUk.error.lowBand.required",
                    "howManyBoughtIntoUk.error.lowBand.negative",
                    "howManyBoughtIntoUk.error.lowBand.nonNumeric",
                    "howManyBoughtIntoUk.error.lowBand.wholeNumber",
        "howManyBoughtIntoUk.error.lowBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyBoughtIntoUk.error.lowBand.outOfMaxVal")),
        "highBand" -> long(
        "howManyBoughtIntoUk.error.highBand.required",
                    "howManyBoughtIntoUk.error.highBand.negative",
                    "howManyBoughtIntoUk.error.highBand.nonNumeric",
                    "howManyBoughtIntoUk.error.highBand.wholeNumber",
          "howManyBoughtIntoUk.error.highBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyBoughtIntoUk.error.highBand.outOfMaxVal"))
    )(HowManyBroughtIntoUk.apply)(HowManyBroughtIntoUk.unapply)
   )
 }
