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

import base.ReturnsTestData._
import base.SpecBase
import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{ReturnPeriod, UserAnswers}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository, sdilSessionCache: SDILSessionCache) extends DataRetrievalActionImpl(sessionRepository, sdilSessionCache) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        val sdilSessionCache = mock[SDILSessionCache]
        when(sessionRepository.get("id")) thenReturn Future(None)
        when(sdilSessionCache.fetchEntry[ReturnPeriod]("id", SDILSessionKeys.RETURN_PERIOD)) thenReturn Future(Some(returnPeriod))
        val action = new Harness(sessionRepository, sdilSessionCache)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", aSubscription)).futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[SessionRepository]
        val sdilSessionCache = mock[SDILSessionCache]
        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
        when(sdilSessionCache.fetchEntry[ReturnPeriod]("id", SDILSessionKeys.RETURN_PERIOD)) thenReturn Future(Some(returnPeriod))
        val action = new Harness(sessionRepository, sdilSessionCache)

        val result = action.callTransform(new IdentifierRequest(FakeRequest(), "id", aSubscription)).futureValue

        result.userAnswers mustBe defined
      }
    }
  }
}
