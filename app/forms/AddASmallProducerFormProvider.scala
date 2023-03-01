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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.{AddASmallProducer, UserAnswers}
import play.api.data.validation.{Constraint, Invalid, Valid}


class AddASmallProducerFormProvider @Inject() extends Mappings {

  def apply(userAnswers: UserAnswers  ) = {

    def checkSDILReference(): Constraint[String] = {

      val validFormatPattern = "^X[A-Z]SDIL000[0-9]{6}$"

      Constraint {
        case sdilReference if !sdilReference.matches(validFormatPattern) =>
          Invalid("addASmallProducer.error.referenceNumber.invalid")
        case sdilReference if sdilReference == userAnswers.id =>
          Invalid("addASmallProducer.error.referenceNumber.same")
        case _ =>
          Valid
      }
    }

    Form(
      mapping(
        "producerName" -> optional(text(
        ).verifying(
          maxLength(160,"addASmallProducer.error.producerName.maxLength"))),
        "referenceNumber" -> text(
          "addASmallProducer.error.referenceNumber.required"
        ).verifying(
            checkSDILReference()),
        "lowBand" -> long(
          "litres.error.lowBand.required",
          "litres.error.lowBand.negative",
          "litres.error.lowBand.nonNumeric",
          "litres.error.lowBand.wholeNumber",
          "litres.error.lowBand.outOfMaxVal")
          .verifying(maximumValueNotEqual(100000000000000L, "litres.error.lowBand.outOfMaxVal")),

        "highBand" -> long(
          "litres.error.highBand.required",
          "litres.error.highBand.negative",
          "litres.error.highBand.nonNumeric",
          "litres.error.highBand.wholeNumber",
          "litres.error.highBand.outOfMaxVal")
          .verifying(maximumValueNotEqual(100000000000000L, "litres.error.lowBand.outOfMaxVal"))
      )(AddASmallProducer.apply)(AddASmallProducer.unapply)
    )
  }
}