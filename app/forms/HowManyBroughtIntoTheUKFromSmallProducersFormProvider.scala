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
import models.HowManyBroughtIntoTheUKFromSmallProducers

class HowManyBroughtIntoTheUKFromSmallProducersFormProvider @Inject() extends Mappings {

   def apply(): Form[HowManyBroughtIntoTheUKFromSmallProducers] = Form(
     mapping(
      "lowBand" -> long(
        "howManyBroughtIntoTheUKFromSmallProducers.error.lowBand.required",
                    "howManyBroughtIntoTheUKFromSmallProducers.error.lowBand.negative",
                    "howManyBroughtIntoTheUKFromSmallProducers.error.lowBand.nonNumeric",
                    "howManyBroughtIntoTheUKFromSmallProducers.error.lowBand.wholeNumber",
        "howManyBroughtIntoTheUKFromSmallProducers.error.lowBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyBroughtIntoTheUKFromSmallProducers.error.lowBand.outOfMaxVal")),
        "highBand" -> long(
        "howManyBroughtIntoTheUKFromSmallProducers.error.highBand.required",
                    "howManyBroughtIntoTheUKFromSmallProducers.error.highBand.negative",
                    "howManyBroughtIntoTheUKFromSmallProducers.error.highBand.nonNumeric",
                    "howManyBroughtIntoTheUKFromSmallProducers.error.highBand.wholeNumber",
          "howManyBroughtIntoTheUKFromSmallProducers.error.highBand.outOfMaxVal")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyBroughtIntoTheUKFromSmallProducers.error.highBand.outOfMaxVal"))
    )(HowManyBroughtIntoTheUKFromSmallProducers.apply)(HowManyBroughtIntoTheUKFromSmallProducers.unapply)
   )
 }
