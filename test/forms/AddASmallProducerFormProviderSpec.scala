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

import base.SpecBase
import controllers.routes
import forms.behaviours.{LongFieldBehaviour, SDILReferenceFieldBehaviours, StringFieldBehaviours}
import models.NormalMode

import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError

class AddASmallProducerFormProviderSpec extends LongFieldBehaviour with StringFieldBehaviours with SDILReferenceFieldBehaviours with SpecBase with MockitoSugar {

  val form = new AddASmallProducerFormProvider()("what?",List())
  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url

  ".producerName" - {

    val fieldName = "producerName"
    val lengthKey = "addASmallProducer.error.producerName.maxLength"
    val maxLength = 160

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

  }

  ".referenceNumber" - {
    val fieldName = "referenceNumber"
    val requiredKey = "addASmallProducer.error.referenceNumber.required"
    val invalidSDILRefNumber = "addASmallProducer.error.referenceNumber.invalid"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like invalidRefNumber(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidSDILRefNumber)
    )

  }

  ".lowBand" - {

    val fieldName = "lowBand"
    val requiredKey = "addASmallProducer.error.lowBand.required"
    val numberKey = "addASmallProducer.error.lowBand.nonNumeric"
    val negativeNumberKey = "addASmallProducer.error.lowBand.negative"
    val maxValueKey = "addASmallProducer.error.lowBand.outOfMaxVal"
    val wholeNumberKey = "addASmallProducer.error.lowBand.wholeNumber"
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
    val requiredKey = "addASmallProducer.error.highBand.required"
    val numberKey = "addASmallProducer.error.highBand.nonNumeric"
    val negativeNumberKey = "addASmallProducer.error.highBand.negative"
    val maxValueKey = "addASmallProducer.error.highBand.outOfMaxVal"
    val wholeNumberKey = "addASmallProducer.error.highBand.wholeNumber"
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
