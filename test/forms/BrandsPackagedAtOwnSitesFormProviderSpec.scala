package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class BrandsPackagedAtOwnSitesFormProviderSpec extends DoubleFieldBehaviour {

  val form = new BrandsPackagedAtOwnSitesFormProvider()()

  ".lowBandLitres" - {

    val fieldName = "lowBandLitres"
    val requiredKey = "brandsPackagedAtOwnSites.error.lowBandLitres.required"
    val numberKey = "brandsPackagedAtOwnSites.error.lowBandLitres.nonNumeric"
    val negativeNumberKey = "brandsPackagedAtOwnSites.error.lowBandLitres.negative"
    val maxValueKey = "brandsPackagedAtOwnSites.error.lowBandLitres.outOfMaxVal"
    val wholeNumberKey = "brandsPackagedAtOwnSites.error.lowBandLitres.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like doubleField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey)
    )

    behave like doubleFieldWithMaximum(
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
    val requiredKey = "brandsPackagedAtOwnSites.error.highBandLitres.required"
    val numberKey = "brandsPackagedAtOwnSites.error.highBandLitres.nonNumeric"
    val negativeNumberKey = "brandsPackagedAtOwnSites.error.highBandLitres.negative"
    val maxValueKey = "brandsPackagedAtOwnSites.error.highBandLitres.outOfMaxVal"
    val wholeNumberKey = "brandsPackagedAtOwnSites.error.highBandLitres.wholeNumber"
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

    behave like doubleFieldWithMaximum(
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
