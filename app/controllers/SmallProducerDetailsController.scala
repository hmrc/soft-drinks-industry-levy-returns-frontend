package controllers

import controllers.actions._
import forms.SmallProducerDetailsFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.SmallProducerDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SmallProducerDetailsView

import scala.concurrent.{ExecutionContext, Future}

class SmallProducerDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: SmallProducerDetailsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: SmallProducerDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.flatMap(_.get(SmallProducerDetailsPage)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
    val answers = request.userAnswers.getOrElse(UserAnswers(id = request.sdilEnrolment))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(answers.set(SmallProducerDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SmallProducerDetailsPage, mode, updatedAnswers))
      )
  }
}
