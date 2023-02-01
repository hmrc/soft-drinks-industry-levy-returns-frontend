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

package forms.behaviours

import play.api.data.{Form, FormError}

trait SDILReferenceFieldBehaviours extends FieldBehaviours {

  def invalidRefNumber(form: Form[_],
                       fieldName: String,
                       requiredError: FormError): Unit = {

    "not bind when SDIL reference has invalid format" in {



      val result = form.bind(Map(fieldName -> "000000381XHSDIL")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

  }

  def sameRefNumber(form: Form[_],
                    fieldName: String,
                    requiredError: FormError): Unit = {

    "not bind when key is not present at all 3" in {

      val result = form.bind(Map("value" -> "XKSDIL000000022")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind SameRef values 1" in {

      val result = form.bind(Map(fieldName -> "XKSDIL000000022")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def existingRefNumber(form: Form[_],
                        fieldName: String,
                        requiredError: FormError): Unit = {

    "not bind when key is not present at all 4" in {

      val result = form.bind(Map("value" -> "XHSDIL000000381")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind SameRef values 2" in {

      val result = form.bind(Map(fieldName -> "XHSDIL000000381")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def largeRefNumber(form: Form[_],
                     fieldName: String,
                     requiredError: FormError): Unit = {

    "not bind when key is not present at all 5" in {

      val result = form.bind(Map("value" -> "XGSDIL000000437")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind LargeRef values" in {

      val result = form.bind(Map(fieldName -> "XGSDIL000000437")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def userRefNumber(form: Form[_],
                    fieldName: String,
                    requiredError: FormError): Unit = {

    "not bind when key is not present at all 6" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind User Reference Number values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

}
