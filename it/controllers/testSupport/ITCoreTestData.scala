package controllers.testSupport

import models.{BrandsPackagedAtOwnSites, UserAnswers}
import org.scalatest.TryValues
import pages.{BrandsPackagedAtOwnSitesPage, OwnBrandsPage}
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt

trait ITCoreTestData extends TryValues {
  val sdilNumber = "XKSDIL000000022"
  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(sdilNumber, Json.obj())

  def ownBrandPageAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true)

  def ownBrandPageFalseAnswers = emptyUserAnswers
    .set(OwnBrandsPage, false)

  def brandPackagedOwnSiteAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, BrandsPackagedAtOwnSites(1000L, 1000L))

}
