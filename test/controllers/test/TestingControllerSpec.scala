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

package controllers.test

import akka.actor.TypedActor.dispatcher
import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import repositories.SessionRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class TestingControllerSpec extends SpecBase with MockitoSugar with Matchers
  with DefaultPlayMongoRepositorySupport[UserAnswers]
  {
    private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

    private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))

    private val mockAppConfig = mock[FrontendAppConfig]
    when(mockAppConfig.cacheTtl) thenReturn 1

    protected val repository = new SessionRepository(
      mongoComponent = mongoComponent,
      appConfig = mockAppConfig,
      clock = stubClock
    )

    protected val testingController = new TestingController(sessionRepository = repository, controllerComponents = mcc)

    ".resetUserAnswers" - {

      "must remove a record" in {
        insert(userAnswers).futureValue

        val result = testingController.resetUserAnswers(userAnswers.id)

        result mustEqual true
        repository.get(userAnswers.id).futureValue must not be defined
      }

      "must return true when there is no record to remove" in {
        val result = testingController.resetUserAnswers("id that does not exist")

        result mustEqual true
      }
    }

}
