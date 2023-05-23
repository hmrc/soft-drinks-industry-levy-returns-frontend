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

import models.ReturnPeriod

import javax.inject.Inject
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(
                                         val sessionRepository: SessionRepository,
                                         val sdilSessionCache: SDILSessionCache
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    for {
      userAnsOps <- sessionRepository.get(request.sdilEnrolment)
      optReturnPeriod <- sdilSessionCache.fetchEntry[ReturnPeriod](request.sdilEnrolment, SDILSessionKeys.RETURN_PERIOD)
    } yield OptionalDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnsOps, optReturnPeriod)
  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
