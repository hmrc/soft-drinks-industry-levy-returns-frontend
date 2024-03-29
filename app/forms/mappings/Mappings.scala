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

package forms.mappings

import java.time.LocalDate
import play.api.data.{ FieldMapping, Mapping }
import play.api.data.Forms.of
import models.Enumerable
import play.api.data.validation.{ Constraint, Invalid, Valid }

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(
    requiredKey: String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey: String = "error.nonNumeric",
    args: Seq[String] = Seq.empty): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def long(
    requiredKey: String = "error.required",
    negativeNumber: String = "error.negative",
    nonNumericKey: String = "error.nonNumeric",
    wholeNumberKey: String = "error.wholeNumber",
    outOfRangeKey: String = "error.outOfMaxVal",
    args: Seq[String] = Seq.empty): FieldMapping[Long] =
    of(longFormatter(requiredKey, negativeNumber, nonNumericKey, wholeNumberKey, outOfRangeKey, args))

  protected def litres(
    band: String,
    args: Seq[String] = Seq.empty): Mapping[Long] =
    of(litresFormatter(band, args))
      .verifying(maximumValueNotEqual(100000000000000L, s"litres.error.$band.outOfMaxVal"))

  protected def sdilReference(
    requiredKey: String = "error.required",
    userAnswersId: String,
    args: Seq[String] = Seq.empty): Mapping[String] = {
    def checkSDILReference(): Constraint[String] = {

      val validFormatPattern = "^[A-Z]{6}[0-9]{9}$"
      val validSDILRefPattern = "^X[A-Z]SDIL000[0-9]{6}$"

      Constraint {
        case sdilReference if !sdilReference.matches(validFormatPattern) =>
          Invalid("addASmallProducer.error.referenceNumber.invalidFormat")
        case sdilReference if !sdilReference.matches(validSDILRefPattern) =>
          Invalid("addASmallProducer.error.referenceNumber.invalidSDILRef")
        case sdilReference if sdilReference == userAnswersId =>
          Invalid("addASmallProducer.error.referenceNumber.same")
        case _ =>
          Valid
      }
    }

    of(sdilReferenceFormatter(requiredKey, args)).verifying(checkSDILReference())
  }

  protected def boolean(
    requiredKey: String = "error.required",
    invalidKey: String = "error.boolean",
    args: Seq[String] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A](
    requiredKey: String = "error.required",
    invalidKey: String = "error.invalid",
    args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def localDate(
    invalidKey: String,
    allRequiredKey: String,
    twoRequiredKey: String,
    requiredKey: String,
    args: Seq[String] = Seq.empty): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, allRequiredKey, twoRequiredKey, requiredKey, args))
}
