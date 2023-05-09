package controllers

import handlers.ErrorHandler
import models.requests.DataRequest
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.Page
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.{InternalServerError, Redirect}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

trait ControllerHelper extends FrontendBaseController with I18nSupport {

  val sessionRepository: SessionRepository
  val navigator: Navigator
  val errorHandler: ErrorHandler

  def updateDatabaseAndRedirect(updatedAnswers: UserAnswers, page: Page, mode: Mode)
                               (implicit ec: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    sessionRepository.set(updatedAnswers).map {
      case Right(_) => Redirect(navigator.nextPage(page, mode, updatedAnswers))
      case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }

}
