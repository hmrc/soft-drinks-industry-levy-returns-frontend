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

import config.FrontendAppConfig
import controllers.actions._
import models.{Address, Amounts, Warehouse}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{SDILSessionCache, SDILSessionKeys}
import services.ReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.CurrencyFormatter
import utilitlies.ReturnsHelper.extractReturnPeriod
import views.html.ReturnSentView

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class ReturnsController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   config:FrontendAppConfig,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   returnService: ReturnService,
                                   requireData: DataRequiredAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: ReturnSentView,
                                   sessionCache: SDILSessionCache,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  //Warehouse
  val tradingName: String = "Soft Juice Ltd"
  val line1: String = "3 Prospect St"
  val line2: String = "Reading"
  val line3: String = "Berkshire"
  val line4: String = "United Kingdom"
  val postcode: String = "CT44 0DF"
  val warehouseList: List[Warehouse] = List(Warehouse(tradingName, Address(line1, line2, line3, line4, postcode)))
  val logger: Logger = Logger(this.getClass())

  def onPageLoad(nilReturn: Boolean): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val sdilEnrolment = request.sdilEnrolment
      val subscription = request.subscription
      val userAnswers = request.userAnswers
      val returnPeriod = extractReturnPeriod(request)


      for {
        session <- sessionCache.fetchEntry[Amounts](sdilEnrolment,SDILSessionKeys.AMOUNTS)
        pendingReturns <- returnService.getPendingReturns(subscription.utr)
      } yield {
        session match {
          case Some(amounts) =>
            if (pendingReturns.contains(returnPeriod)) {
              returnService.returnsUpdate(subscription, returnPeriod, userAnswers, nilReturn).map {
                case Some(OK) => logger.info(s"Return submitted for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
                case _ => logger.error(s"Failed to submit return for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
                  throw new RuntimeException(s"Failed to submit return $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}" )
              }
            } else {
              logger.error(s"Pending returns for $sdilEnrolment don't contain the return for year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            }

            Ok(view(returnPeriod,
              userAnswers,
              amounts,
              subscription,
              CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total),
              financialStatus = financialStatus(amounts.total)
            )(implicitly, implicitly, config))
          case _ =>
            logger.error(s"No amount found in the cache for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }


  private def financialStatus(total: BigDecimal): String = {
    total match {
      case total if total > 0 => "amountToPay"
      case total if total < 0 => "creditedPay"
      case total if total == 0 => "noPayNeeded"
    }
  }
}
