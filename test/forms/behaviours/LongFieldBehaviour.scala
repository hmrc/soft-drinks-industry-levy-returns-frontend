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

package forms.behaviours

import play.api.data.{Form, FormError}

class LongFieldBehaviour extends FieldBehaviours {
  def longField(form: Form[_],
                fieldName: String,
                nonNumericError: FormError,
                negativeNumberError: FormError,
                wholeNumberError: FormError): Unit = {
    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors must contain only nonNumericError
      }
    }


    "not bind negative numbers" in {

          val result = form.bind(Map(fieldName -> (-35878697979L).toString)).apply(fieldName)
          result.errors must contain only negativeNumberError

    }

    "not bind decimals" in {

      forAll(decimalsOps -> "decimal") {
        decimal =>
          val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
          result.errors must contain only wholeNumberError
      }
    }
  }

  def doubleFieldWithMaximum(form: Form[_],
                          fieldName: String,
                          maximum: Long,
                          expectedError: FormError): Unit = {

    s"not bind long above $maximum" in {

      forAll(longAboveValue(maximum) -> "outOfMaxVal") {
        number: Long =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only expectedError
      }
    }
  }
}