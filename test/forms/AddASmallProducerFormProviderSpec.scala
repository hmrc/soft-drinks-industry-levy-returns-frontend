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

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import forms.behaviours.{LongFieldBehaviour, SDILReferenceFieldBehaviours, StringFieldBehaviours}
import models.{NormalMode, UserAnswers}
import models.requests.DataRequest
import org.scalatestplus.mockito.MockitoSugar
import pages.{AddASmallProducerPage, RemoveSmallProducerConfirmPage}
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import repositories.SessionRepository
import views.html.AddASmallProducerView


class AddASmallProducerFormProviderSpec extends LongFieldBehaviour with StringFieldBehaviours with SDILReferenceFieldBehaviours with SpecBase with MockitoSugar {

//  val mockSessionRepository = mock[SessionRepository]
//  val application = applicationBuilder(userAnswers = None).build()
//  val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]

  // do we need to implement the datarequest
//  implicit val request: DataRequest[_]



  val form = new AddASmallProducerFormProvider()("what?")
  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url

  ".producerName" - {

    val fieldName = "producerName"
    val lengthKey = "addASmallProducer.error.producerName.maxLength"
    val maxLength = 160

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

  }

  ".referenceNumber" - {
    val fieldName = "referenceNumber"
    val requiredKey = "addASmallProducer.error.referenceNumber.required"
    val invalidSDILRefNumber = "addASmallProducer.error.referenceNumber.invalid"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like invalidRefNumber(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidSDILRefNumber)
    )

    //    Property tests would not suit the following constrains on the form, hence unit tests will be used instead
    //    val alreadyExistsKey = "addASmallProducer.error.referenceNumber.Exist"
    //    val large = "addASmallProducer.error.referenceNumber.Large"
    //    val same = "addASmallProducer.error.referenceNumber.same"




//    "Small producer reference number must be different to reference currently submitting the returns" in {
//      val formData = Json.obj(
//        "producerName" -> "Super Cola Ltd",
//        "referenceNumber" -> sdilNumber,
//        "lowBand" -> "12",
//        "highBand" -> "12"
//      )
//      val userAnswers = UserAnswers(sdilNumber, Json.obj()).set(AddASmallProducerPage,"hello").success.value
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//
//        val request = FakeRequest(POST, addASmallProducerRoute)
//          .withFormUrlEncodedBody(
//            ("producerName", "Super Cola Ltd"),
//            ("referenceNumber", sdilNumber),
//            ("lowBand", "12"),
//            ("highBand", "12")
//          )
//
//        val boundForm = form.bind(formData, 102400)
//        val view = application.injector.instanceOf[AddASmallProducerView]
//        val result = route(application, request).value
//
//
//        status(result) mustEqual BAD_REQUEST
////        contentAsString(result) must include ("addASmallProducer.error.referenceNumber.same")
//        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
//      }
//
//    }

  }

  ".lowBand" - {

    val fieldName = "lowBand"
    val requiredKey = "addASmallProducer.error.lowBand.required"
    val numberKey = "addASmallProducer.error.lowBand.nonNumeric"
    val negativeNumberKey = "addASmallProducer.error.lowBand.negative"
    val maxValueKey = "addASmallProducer.error.lowBand.outOfMaxVal"
    val wholeNumberKey = "addASmallProducer.error.lowBand.wholeNumber"
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
      maxValue,
       FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".highBand" - {

    val fieldName = "highBand"
    val requiredKey = "addASmallProducer.error.highBand.required"
    val numberKey = "addASmallProducer.error.highBand.nonNumeric"
    val negativeNumberKey = "addASmallProducer.error.highBand.negative"
    val maxValueKey = "addASmallProducer.error.highBand.outOfMaxVal"
    val wholeNumberKey = "addASmallProducer.error.highBand.wholeNumber"
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
