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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.http.{HeaderCarrier}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default,
  sdilConnector: SoftDrinksIndustryLevyConnector)
  (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with ActionHelpers {

//  private def authoriseSubscription[A](
//    sdilNumber: String,
//    identifierType: String,
//    enrolments: Enrolments,
//    block: IdentifierRequest[A] => Future[Result])
//    (implicit request: Request[A], hc: HeaderCarrier) = {
//
//    subscriptionService.authenticateSubscription(sdilNumber, identifierType) flatMap {
//      case Right(subscription) =>
//        block(IdentifierRequest(request, AgentUser(internalId, enrolments, arn)))
//      case Left(redirect: Result) => Future.successful(redirect)
//    }
//  }

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway)).retrieve(allEnrolments) { enrolments =>
      (getSdilEnrolment(enrolments), getUtr(enrolments)) match {
        case (Some(e), _) => block(IdentifierRequest(request, e.value))
        case (None, Some(utr)) =>  sdilConnector.retrieveSubscription(utr, "utr").flatMap {
          case Some(subscription) =>
            block(IdentifierRequest(request, EnrolmentIdentifier("sdil", subscription.sdilRef).value))
          case None => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
        }
        case _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
      }
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}

class SessionIdentifierAction @Inject()(
                                         val parser: BodyParsers.Default
                                       )
                                       (implicit val executionContext: ExecutionContext) extends IdentifierAction {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId match {
      case Some(session) =>
        block(IdentifierRequest(request, session.value))
      case None =>
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
