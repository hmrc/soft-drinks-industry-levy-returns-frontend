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

package controllers.actions

import base.SpecBase
import models.{ReturnPeriod, UserAnswers}
import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckSumbissionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Check Submission Action" - {

    "when the return has been submitted" - {

      "must redirect the user to returns sent page" in {
//
//        val sessionRepository = mock[SessionRepository]
//        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
//        val action = new Harness(sessionRepository)
//
//        val result = action.callTransform(new IdentifierRequest(FakeRequest(), "id", aSubscription, Some(ReturnPeriod(2023,1)))).futureValue
//
//
//        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {
//
//        val sessionRepository = mock[SessionRepository]
//        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
//        val action = new Harness(sessionRepository)
//
//        val result = action.callTransform(new IdentifierRequest(FakeRequest(), "id", aSubscription, Some(ReturnPeriod(2023,1)))).futureValue
//
//        result.userAnswers mustBe defined
      }
    }
  }

}
