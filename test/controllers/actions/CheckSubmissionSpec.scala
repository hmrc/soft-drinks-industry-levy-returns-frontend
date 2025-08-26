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
import models.ReturnPeriod
import models.requests.DataRequest
import org.scalatest.Inside.inside
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.Future
import base.ReturnsTestData._

class CheckSubmissionSpec extends SpecBase with MockitoSugar {

  class Harness() extends CheckingSubmission() {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Check Submission Action" - {

    "when the return has been submitted" - {

      "must redirect the user to returns sent page" in {

        val action = new Harness()
        val result = action.callRefine(new DataRequest(FakeRequest(), "id", aSubscription, submittedAnswers, ReturnPeriod(2023, 1))).futureValue

        result must matchPattern { case Left(_) => }

        inside(result) {
          case Left(res) =>
            res.header.status mustEqual SEE_OTHER
        }

      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {
        val action = new Harness()
        val result = action.callRefine(new DataRequest(FakeRequest(), "id", aSubscription, completedUserAnswers, ReturnPeriod(2023, 1))).futureValue

        result must matchPattern { case Right(_) => }

        inside(result) {
          case Right(res) =>
            res.userAnswers mustEqual (completedUserAnswers)
        }
      }
    }
  }

}