package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends DoubleFieldBehaviour {

  val form = new $className$FormProvider()()

  ".$field1Name$" - {

    val fieldName = "$field1Name$"
    val requiredKey = "$className;format="decap"$.error.$field1Name$.required"
    val numberKey = "$className;format="decap"$.error.$field1Name$.nonNumeric"
    val negativeNumberKey = "$className;format="decap"$.error.$field1Name$.negative"
    val maxValueKey = "$className;format="decap"$.error.$field1Name$.outOfMaxVal"
    val maxValue = $field1Maximum$
    val validDataGenerator = doubleInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like doubleField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey)
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

  ".$field2Name$" - {

    val fieldName = "$field2Name$"
    val requiredKey = "$className;format="decap"$.error.$field2Name$.required"
    val numberKey = "$className;format="decap"$.error.$field2Name$.nonNumeric"
    val negativeNumberKey = "$className;format="decap"$.error.$field2Name$.negative"
    val maxValueKey = "$className;format="decap"$.error.$field2Name$.outOfMaxVal"
    val maxValue = $field2Maximum$
    val validDataGenerator = doubleInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like doubleField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey)
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
