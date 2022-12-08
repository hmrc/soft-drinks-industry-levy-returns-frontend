package forms.behaviours

import play.api.data.{Form, FormError}

class DoubleFieldBehaviour extends FieldBehaviours {
  def doubleField(form: Form[_],
                  fieldName: String,
                  nonNumericError: FormError,
                  negativeNumberError: FormError): Unit = {
    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors must contain only nonNumericError
      }
    }

    "not bind negative numbers" in {

      forAll(negativeNumbers -> "negative") {
        number =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only negativeNumberError
      }
    }
  }

  def doubleFieldWithMaximum(form: Form[_],
                          fieldName: String,
                          maximum: Double,
                          expectedError: FormError): Unit = {

    s"not bind integers above $maximum" in {

      forAll(doubleAboveValue(maximum) -> "outOfMaxVal") {
        number: Double =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only expectedError
      }
    }
  }
}
