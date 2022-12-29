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

package forms

import connectors.SoftDrinksIndustryLevyConnector

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.{AddASmallProducer, ReturnPeriod}
import models.requests.OptionalDataRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class AddASmallProducerFormProvider @Inject() extends Mappings {



   def apply(sessionRepository: SessionRepository,
             sdilConnector: SoftDrinksIndustryLevyConnector
            )(implicit request: OptionalDataRequest[_]): Form[AddASmallProducer] = {

     implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

     def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod): Future[Option[Boolean]] =
       sdilConnector.checkSmallProducerStatus(sdilRef, period)

     Form(
       mapping(
         "producerName" -> optional(text(

         )
           .verifying(maxLength(160,"addASmallProducer.error.producerName.maxLength"))),

         "referenceNumber" -> text(
           "addASmallProducer.error.referenceNumber.required"
         )
           .verifying(
             referenceNumberFormat("^X[A-Z]SDIL000[0-9]{6}$",
               "addASmallProducer.error.referenceNumber.invalid"),
             referenceNumberSame(request.sdilEnrolment,
               "addASmallProducer.error.referenceNumber.same"),
             referenceNumberExists(List("XHSDIL000000381","XLSDIL000000539"),
               "addASmallProducer.error.referenceNumber.Exist"),
             referenceNumberLarge(Await.result(checkSmallProducerStatus(sp.sdilRef, period), 20.seconds).getOrElse(true),"addASmallProducer.error.referenceNumber.Large")),

         "lowBand" -> long(
           "addASmallProducer.error.lowBand.required",
           "addASmallProducer.error.lowBand.negative",
           "addASmallProducer.error.lowBand.nonNumeric",
           "addASmallProducer.error.lowBand.wholeNumber",
           "addASmallProducer.error.lowBand.outOfMaxVal")
           .verifying(maximumValueNotEqual(100000000000000L, "addASmallProducer.error.lowBand.outOfMaxVal")),

         "highBand" -> long(
           "addASmallProducer.error.highBand.required",
           "addASmallProducer.error.highBand.negative",
           "addASmallProducer.error.highBand.nonNumeric",
           "addASmallProducer.error.highBand.wholeNumber",
           "addASmallProducer.error.highBand.outOfMaxVal")
           .verifying(maximumValueNotEqual(100000000000000L, "addASmallProducer.error.highBand.outOfMaxVal"))
       )(AddASmallProducer.apply)(AddASmallProducer.unapply)
     )
   }
 }
