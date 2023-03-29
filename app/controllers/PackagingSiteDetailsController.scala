package controllers

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import forms.PackagingSiteDetailsFormProvider

import javax.inject.Inject
import models.{Mode, SdilReturn}
import navigation.Navigator
import pages.PackagingSiteDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PackagingSiteDetailsView
import models.backend.Site
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.PackagingSiteDetailsSummary
import viewmodels.govuk.summarylist._

import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                sdilConnector: SoftDrinksIndustryLevyConnector,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: PackagingSiteDetailsFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: PackagingSiteDetailsView
                                              )(implicit ec: ExecutionContext, config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val packagingSiteList: List[Site] = request.userAnswers.packagingSiteList
      val preparedForm = request.userAnswers.get(PackagingSiteDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val packagingSiteSummaryAliasList: List[SummaryListRow] = PackagingSiteDetailsSummary.row2(packagingSiteList)
      val aliasList: SummaryList = SummaryListViewModel(
        rows = packagingSiteSummaryAliasList
      )

      Ok(view(preparedForm, mode, aliasList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val packagingSiteList: List[Site] = request.userAnswers.packagingSiteList
      val packagingSiteSummaryList: List[SummaryListRow] = PackagingSiteDetailsSummary.row2(packagingSiteList)
      val siteList: SummaryList = SummaryListViewModel(
        rows = packagingSiteSummaryList
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackagingSiteDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            sdilReturn = SdilReturn.apply(updatedAnswers)
            retrievedSubs <- sdilConnector.retrieveSubscription(request.sdilEnrolment, "sdil")
          } yield Redirect(navigator.nextPage(PackagingSiteDetailsPage, mode, updatedAnswers, Some(sdilReturn), retrievedSubs))
      )
  }
}
