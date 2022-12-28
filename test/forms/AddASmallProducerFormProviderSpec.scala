package forms

import forms.behaviours.LongFieldBehaviour
import play.api.data.FormError

class AddASmallProducerFormProviderSpec extends LongFieldBehaviour {

  val form = new AddASmallProducerFormProvider()()

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
