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

import forms.behaviours.LongFieldBehaviour
import play.api.data.FormError

class HowManyAsAContractPackerFormProviderSpec extends LongFieldBehaviour {

  val form = new HowManyAsAContractPackerFormProvider()()

  ".lowBand" - {

    val fieldName = "lowBand"
    val requiredKey = "howManyAsAContractPacker.error.lowBand.required"
    val numberKey = "howManyAsAContractPacker.error.lowBand.nonNumeric"
    val negativeNumberKey = "howManyAsAContractPacker.error.lowBand.negative"
    val maxValueKey = "howManyAsAContractPacker.error.lowBand.outOfMaxVal"
    val wholeNumberKey = "howManyAsAContractPacker.error.lowBand.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like longField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey)
    )

    behave like longFieldWithMaximum(
      form,
      fieldName,
      maxValue,
      FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".highBand" - {

    val fieldName = "highBand"
    val requiredKey = "howManyAsAContractPacker.error.highBand.required"
    val numberKey = "howManyAsAContractPacker.error.highBand.nonNumeric"
    val negativeNumberKey = "howManyAsAContractPacker.error.highBand.negative"
    val maxValueKey = "howManyAsAContractPacker.error.highBand.outOfMaxVal"
    val wholeNumberKey = "howManyAsAContractPacker.error.highBand.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like longField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey)
    )

    behave like longFieldWithMaximum(
      form,
      fieldName,
      maxValue,
      FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}