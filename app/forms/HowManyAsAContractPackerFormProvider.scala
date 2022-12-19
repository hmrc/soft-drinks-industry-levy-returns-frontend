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
import models.HowManyAsAContractPacker

class HowManyAsAContractPackerFormProvider @Inject() extends Mappings {

   def apply(): Form[HowManyAsAContractPacker] = Form(
     mapping(
      "lowBand" -> long(
        "howManyAsAContractPacker.error.lowBand.required",
                    "howManyAsAContractPacker.error.lowBand.negative",
                    "howManyAsAContractPacker.error.lowBand.nonNumeric",
                    "howManyAsAContractPacker.error.lowBand.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyAsAContractPacker.error.lowBand.outOfMaxVal")),
        "highBand" -> long(
        "howManyAsAContractPacker.error.highBand.required",
                    "howManyAsAContractPacker.error.highBand.negative",
                    "howManyAsAContractPacker.error.highBand.nonNumeric",
                    "howManyAsAContractPacker.error.highBand.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "howManyAsAContractPacker.error.highBand.outOfMaxVal"))
    )(HowManyAsAContractPacker.apply)(HowManyAsAContractPacker.unapply)
   )
 }
