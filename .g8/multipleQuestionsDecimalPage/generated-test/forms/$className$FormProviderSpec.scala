package forms

import forms.behaviours.LongFieldBehaviour
import play.api.data.FormError

class $className$FormProviderSpec extends LongFieldBehaviour {

  val form = new $className$FormProvider()()

  ".$field1Name$" - {

    val fieldName = "$field1Name$"
    val requiredKey = "$className;format="decap"$.error.$field1Name$.required"
    val numberKey = "$className;format="decap"$.error.$field1Name$.nonNumeric"
    val negativeNumberKey = "$className;format="decap"$.error.$field1Name$.negative"
    val maxValueKey = "$className;format="decap"$.error.$field1Name$.outOfMaxVal"
    val wholeNumberKey = "$className;format="decap"$.error.$field1Name$.wholeNumber"
    val maxValue = $field1Maximum$
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

  ".$field2Name$" - {

    val fieldName = "$field2Name$"
    val requiredKey = "$className;format="decap"$.error.$field2Name$.required"
    val numberKey = "$className;format="decap"$.error.$field2Name$.nonNumeric"
    val negativeNumberKey = "$className;format="decap"$.error.$field2Name$.negative"
    val maxValueKey = "$className;format="decap"$.error.$field2Name$.outOfMaxVal"
    val wholeNumberKey = "$className;format="decap"$.error.$field2Name$.wholeNumber"
    val maxValue = $field2Maximum$
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