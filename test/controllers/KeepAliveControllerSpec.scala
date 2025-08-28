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

package controllers

import base.ReturnsTestData._
import base.SpecBase
import errors.SessionDatabaseInsertError
import helpers.LoggerHelper
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import util.GenericLogger

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the session alive and return OK" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.keepAlive(ArgumentMatchers.eq(completedUserAnswers.id))) thenReturn Future.successful(Right(true))

        val application =
          applicationBuilder(Some(completedUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockSessionRepository, times(1)).keepAlive(completedUserAnswers.id)
        }
      }
    }

    "when session repository keep alive fails" - {

      "must return Internal Server Error" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.keepAlive(ArgumentMatchers.eq(completedUserAnswers.id))) thenReturn Future.successful(Left(SessionDatabaseInsertError))

        val application =
          applicationBuilder(Some(completedUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          verify(mockSessionRepository, times(1)).keepAlive(completedUserAnswers.id)
        }
      }
    }

    "should log an error message when internal server error is returned" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.keepAlive(ArgumentMatchers.eq(completedUserAnswers.id))) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val app =
        applicationBuilder(Some(completedUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(app) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual ("ERROR")
              event.getMessage mustEqual ("Failed to keep the session alive due to error from mongo session repository's keepAlive")
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }

}
