package services

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.requests.DataRequest
import models.retrieved.RetrievedSubscription
import models.{ReturnPeriod, SdilReturn, SmallProducer, UserAnswers, Warehouse}
import pages._
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.ReturnsHelper
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

import javax.inject.Inject
import scala.concurrent.Future

@Singleton
class ReturnService @Inject()(config: FrontendAppConfig,
                              sdilConnector: SoftDrinksIndustryLevyConnector) {

  def getPendingReturns(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] = sdilConnector.returns_pending(utr)

  def returnsUpdate(subscription: RetrievedSubscription, returnPeriod: ReturnPeriod, userAnswers: UserAnswers, nilReturn: Boolean)(implicit hc: HeaderCarrier): Future[Option[Int]] = {
    sdilConnector.returns_update(subscription.utr, returnPeriod, returnToBeSubmitted(nilReturn, subscription, userAnswers))
  }

  def returnToBeSubmitted(nilReturn: Boolean, subscription: RetrievedSubscription, userAnswers: UserAnswers) = {
    if (nilReturn) {
      ReturnsHelper.emptyReturn
    } else {
      SdilReturn(
        ownBrandsLitres(subscription, userAnswers),
        packLargeLitres(userAnswers),
        userAnswers.smallProducerList,
        importsLitres(userAnswers),
        importsSmallLitres(userAnswers),
        exportLitres(userAnswers),
        wastageLitres(userAnswers))
    }
  }


  private def ownBrandsLitres(subscription: RetrievedSubscription, userAnswers: UserAnswers) = {
    (userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L))
  }

  private def packLargeLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L))
  }

  private def importsLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L))
  }

  private def importsSmallLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyBroughtIntoTheUKFromSmallProducersPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyBroughtIntoTheUKFromSmallProducersPage).map(_.highBand).getOrElse(0L))
  }

  private def exportLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L))
  }

  private def wastageLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L))
  }


}
