package services

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.Address
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupService @Inject()(addressLookupConnector: AddressLookupService) {

  val logger: Logger = Logger(this.getClass())

  def getAddress(id:String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[Address]] = addressLookupConnector.getAddress(id)

}
