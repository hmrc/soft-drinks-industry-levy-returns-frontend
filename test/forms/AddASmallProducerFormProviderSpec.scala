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

import base.ReturnsTestData._
import base.SpecBase
import controllers.routes
import forms.behaviours.{ LongFieldBehaviour, SDILReferenceFieldBehaviours, StringFieldBehaviours }
import models.NormalMode

import org.mockito.MockitoSugar
import play.api.data.FormError

class AddASmallProducerFormProviderSpec extends LongFieldBehaviour with StringFieldBehaviours with SDILReferenceFieldBehaviours with SpecBase with MockitoSugar {

  val form = new AddASmallProducerFormProvider()(emptyUserAnswers)
  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url

  ".producerName" - {

    val fieldName = "producerName"
    val lengthKey = "addASmallProducer.error.producerName.maxLength"
    val maxLength = 160

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)))

  }

  ".referenceNumber" - {
    val fieldName = "referenceNumber"
    val requiredKey = "addASmallProducer.error.referenceNumber.required"
    val invalidSDILFormatKey = "addASmallProducer.error.referenceNumber.invalidFormat"
    val invalidSDILRefKey = "addASmallProducer.error.referenceNumber.invalidSDILRef"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey))

    behave like invalidSDILFormat(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidSDILFormatKey))

    behave like invalidSDILRef(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidSDILRefKey))

  }

  ".lowBand" - {

    val fieldName = "lowBand"
    val requiredKey = "litres.error.lowBand.required"
    val numberKey = "litres.error.lowBand.nonNumeric"
    val negativeNumberKey = "litres.error.lowBand.negative"
    val maxValueKey = "litres.error.lowBand.outOfMaxVal"
    val wholeNumberKey = "litres.error.lowBand.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator)

    behave like longField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey))

    behave like longFieldWithMaximum(
      form,
      fieldName,
      maxValue,
      FormError(fieldName, maxValueKey, Seq(maxValue)))

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey))
  }

  ".highBand" - {

    val fieldName = "highBand"
    val requiredKey = "litres.error.highBand.required"
    val numberKey = "litres.error.highBand.nonNumeric"
    val negativeNumberKey = "litres.error.highBand.negative"
    val maxValueKey = "litres.error.highBand.outOfMaxVal"
    val wholeNumberKey = "litres.error.highBand.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator)

    behave like longField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey))

    behave like longFieldWithMaximum(
      form,
      fieldName,
      maxValue,
      FormError(fieldName, maxValueKey, Seq(maxValue)))

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey))
  }
}
