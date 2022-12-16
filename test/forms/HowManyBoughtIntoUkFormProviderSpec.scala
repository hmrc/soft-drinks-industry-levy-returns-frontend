package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class HowManyBoughtIntoUkFormProviderSpec extends LongFieldBehaviour {

  val form = new HowManyBoughtIntoUkFormProvider()()

  ".lowBandLitres" - {

    val fieldName = "lowBandLitres"
    val requiredKey = "HowManyBoughtIntoUk.error.lowBandLitres.required"
    val numberKey = "HowManyBoughtIntoUk.error.lowBandLitres.nonNumeric"
    val negativeNumberKey = "HowManyBoughtIntoUk.error.lowBandLitres.negative"
    val maxValueKey = "HowManyBoughtIntoUk.error.lowBandLitres.outOfMaxVal"
    val wholeNumberKey = "HowManyBoughtIntoUk.error.lowBandLitres.wholeNumber"
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
      maxLength = maxValue,
      lengthError = FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".highBandLitres" - {

    val fieldName = "highBandLitres"
    val requiredKey = "HowManyBoughtIntoUk.error.highBandLitres.required"
    val numberKey = "HowManyBoughtIntoUk.error.highBandLitres.nonNumeric"
    val negativeNumberKey = "HowManyBoughtIntoUk.error.highBandLitres.negative"
    val maxValueKey = "HowManyBoughtIntoUk.error.highBandLitres.outOfMaxVal"
    val wholeNumberKey = "HowManyBoughtIntoUk.error.highBandLitres.wholeNumber"
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
      maxLength = maxValue,
      lengthError = FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
