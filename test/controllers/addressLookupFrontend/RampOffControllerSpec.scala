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

package controllers.addressLookupFrontend
import base.ReturnsTestData._
import base.SpecBase
import models.alf.{AlfAddress, AlfResponse}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AddressLookupService, PackingDetails, WarehouseDetails}

import scala.concurrent.Future

class RampOffControllerSpec extends SpecBase with MockitoSugar {
  class Setup {
    val mockAddressLookupService: AddressLookupService = mock[AddressLookupService]
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
  }
  s"$WarehouseDetails off ramp" - {
    "should Redirect to the next page when ALF returns address successfully" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))
      val updatedUserAnswers: UserAnswers = emptyUserAnswers.copy(id = "foobarwizz")

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(responseFromGetAddress))
      when(mockAddressLookupService.addAddressUserAnswers(
        ArgumentMatchers.eq(WarehouseDetails),
        ArgumentMatchers.eq(responseFromGetAddress.address),
        ArgumentMatchers.eq(emptyUserAnswers),
        ArgumentMatchers.eq(sdilId),
        ArgumentMatchers.eq(alfId)))
        .thenReturn(updatedUserAnswers)
      when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
        .thenReturn(Future.successful(Right(true)))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.secondaryWareHouseDetailsOffRamp(sdilId, alfId).url)

        val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url
      }
    }
    s"should return exception to the next page when ALF doesnt return Address successfully" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("woopsie")))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.secondaryWareHouseDetailsOffRamp(sdilId, alfId).url)

        intercept[Exception](await(route(application, request).value))
      }
    }
    s"should return exception to the next page when ALF returns address, but it can't be converted" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(responseFromGetAddress))
      when(mockAddressLookupService.addAddressUserAnswers(
        ArgumentMatchers.eq(WarehouseDetails),
        ArgumentMatchers.eq(responseFromGetAddress.address),
        ArgumentMatchers.eq(emptyUserAnswers),
        ArgumentMatchers.eq(sdilId),
        ArgumentMatchers.eq(alfId)))
        .thenThrow(new RuntimeException("foo"))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.secondaryWareHouseDetailsOffRamp(sdilId, alfId).url)

        intercept[Exception](await(route(application, request).value))
      }
    }
    s"should return exception to the next page when ALF returns address, it can be converted but fails to update db" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))
      val updatedUserAnswers: UserAnswers = emptyUserAnswers.copy(id = "foobarwizz")

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(responseFromGetAddress))
      when(mockAddressLookupService.addAddressUserAnswers(
        ArgumentMatchers.eq(WarehouseDetails),
        ArgumentMatchers.eq(responseFromGetAddress.address),
        ArgumentMatchers.eq(emptyUserAnswers),
        ArgumentMatchers.eq(sdilId),
        ArgumentMatchers.eq(alfId)))
        .thenReturn(updatedUserAnswers)
      when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
        .thenReturn(Future.failed(new Exception("woopsie")))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.secondaryWareHouseDetailsOffRamp(sdilId, alfId).url)
        intercept[Exception](await(route(application, request).value))
      }
    }
  }
  s"$PackingDetails off ramp" - {
    "should Redirect to the next page when ALF returns address successfully" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))
      val updatedUserAnswers: UserAnswers = emptyUserAnswers.copy(id = "foobarwizz")

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(responseFromGetAddress))
      when(mockAddressLookupService.addAddressUserAnswers(
        ArgumentMatchers.eq(PackingDetails),
        ArgumentMatchers.eq(responseFromGetAddress.address),
        ArgumentMatchers.eq(emptyUserAnswers),
        ArgumentMatchers.eq(sdilId),
        ArgumentMatchers.eq(alfId)))
        .thenReturn(updatedUserAnswers)
      when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
        .thenReturn(Future.successful(Right(true)))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId).url)

        val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe controllers.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
      }
    }
    s"should return exception to the next page when ALF doesnt return Address successfully" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("woopsie")))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId).url)

        intercept[Exception](await(route(application, request).value))
      }
    }
    s"should return exception to the next page when ALF returns address, but it can't be converted" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(responseFromGetAddress))
      when(mockAddressLookupService.addAddressUserAnswers(
        ArgumentMatchers.eq(PackingDetails),
        ArgumentMatchers.eq(responseFromGetAddress.address),
        ArgumentMatchers.eq(emptyUserAnswers),
        ArgumentMatchers.eq(sdilId),
        ArgumentMatchers.eq(alfId)))
        .thenThrow(new RuntimeException("foo"))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId).url)

        intercept[Exception](await(route(application, request).value))
      }
    }
    s"should return exception to the next page when ALF returns address, it can be converted but fails to update db" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AddressLookupService].toInstance(mockAddressLookupService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))
      val updatedUserAnswers: UserAnswers = emptyUserAnswers.copy(id = "foobarwizz")

      when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(responseFromGetAddress))
      when(mockAddressLookupService.addAddressUserAnswers(
        ArgumentMatchers.eq(PackingDetails),
        ArgumentMatchers.eq(responseFromGetAddress.address),
        ArgumentMatchers.eq(emptyUserAnswers),
        ArgumentMatchers.eq(sdilId),
        ArgumentMatchers.eq(alfId)))
        .thenReturn(updatedUserAnswers)
      when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
        .thenReturn(Future.failed(new Exception("woopsie")))

      running(application) {
        val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId).url)
        intercept[Exception](await(route(application, request).value))
      }
    }
  }
}
