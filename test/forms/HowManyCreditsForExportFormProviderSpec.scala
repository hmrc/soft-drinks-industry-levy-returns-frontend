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

import forms.behaviours.{LongFieldBehaviour}
import play.api.data.FormError

class HowManyCreditsForExportFormProviderSpec extends LongFieldBehaviour {

  val form = new HowManyCreditsForExportFormProvider()()

  ".lowBand" - {

    val fieldName = "lowBand"
    val requiredKey = "howManyCreditsForExport.error.lowBand.required"
    val numberKey = "howManyCreditsForExport.error.lowBand.nonNumeric"
    val negativeNumberKey = "howManyCreditsForExport.error.lowBand.negative"
    val maxValueKey = "howManyCreditsForExport.error.lowBand.outOfMaxVal"
    val wholeNumberKey = "howManyCreditsForExport.error.lowBand.wholeNumber"
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
    val requiredKey = "howManyCreditsForExport.error.highBand.required"
    val numberKey = "howManyCreditsForExport.error.highBand.nonNumeric"
    val negativeNumberKey = "howManyCreditsForExport.error.highBand.negative"
    val maxValueKey = "howManyCreditsForExport.error.highBand.outOfMaxVal"
    val wholeNumberKey = "howManyCreditsForExport.error.highBand.wholeNumber"
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
