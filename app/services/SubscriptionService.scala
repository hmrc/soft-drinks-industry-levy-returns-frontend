package services

import connectors.SoftDrinksIndustryLevyConnector
import play.api.Logging
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier


import javax.inject.Inject
import scala.concurrent.Future

class SubscriptionService @Inject()(
  softDrinksIndustryLevyConnector: SoftDrinksIndustryLevyConnector)  extends Logging {

  def authenticateSubscription[A](sdilNumber:String,identifierType:String)
   (implicit hc: HeaderCarrier): Future[Either[Result, String]] =
  {
    softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType).flatMap {
      case Some(subscription) =>
        Future.successful(Right(subscription))

      case None =>
        logger.warn(s"[AuthenticationService][authenticateUser][Session ID: ${sdilNumber}] Unable to authenticate user with sdil-number")
        Future.successful(Left(Redirect(redirectUrl)))
    }
  }
}
