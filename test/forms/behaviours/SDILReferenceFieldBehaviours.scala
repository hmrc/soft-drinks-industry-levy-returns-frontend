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

      forAll(badSdilReferences -> "sdilRef") {
        sdilRef =>
          val result = form.bind(Map(fieldName -> sdilRef)).apply(fieldName)
          result.errors mustEqual Seq(requiredError)
      }
    }

  }


}
