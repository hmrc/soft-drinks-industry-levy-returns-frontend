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

package base

import base.ReturnsTestData.defaultReturnsPeriod
import cats.data.EitherT
import config.FrontendAppConfig
import controllers.actions._
import errors.ReturnsErrors
import models.retrieved.RetrievedSubscription
import models.{ ReturnPeriod, UserAnswers }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues, TryValues }
import play.api.i18n.{ Lang, Messages, MessagesApi, MessagesImpl }
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import play.api.{ Application, Play }
import service.ReturnResult
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }
trait SpecBase
  extends AnyFreeSpec
  with Matchers
  with TryValues
  with OptionValues
  with ScalaFutures
  with IntegrationPatience with BeforeAndAfterEach {

  lazy val application: Application = applicationBuilder(userAnswers = None).build()
  implicit lazy val messagesAPI: MessagesApi = application.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  lazy val frontendAppConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  override def afterEach(): Unit = {
    Play.stop(application)
    super.afterEach()
  }
  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None,
    returnPeriod: Option[ReturnPeriod] = Some(defaultReturnsPeriod),
    subscription: Option[RetrievedSubscription] = None): GuiceApplicationBuilder = {
    val bodyParsers = stubControllerComponents().parsers.defaultBodyParser
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(subscription, bodyParsers)),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, returnPeriod)))
  }

  def createSuccessReturnResult[T](result: T): ReturnResult[T] =
    EitherT.right[ReturnsErrors](Future.successful(result))

  def createFailureReturnResult[T](error: ReturnsErrors): ReturnResult[T] =
    EitherT.left(Future.successful(error))

}
