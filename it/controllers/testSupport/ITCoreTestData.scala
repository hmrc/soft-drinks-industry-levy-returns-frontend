package controllers.testSupport

import models.{BrandsPackagedAtOwnSites, HowManyAsAContractPacker, UserAnswers}
import org.scalatest.TryValues
import pages.{BrandsPackagedAtOwnSitesPage, BroughtIntoUKPage, ExemptionsForSmallProducersPage, HowManyAsAContractPackerPage, OwnBrandsPage, PackagedContractPackerPage}
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

  def howManyAsContractPackerFullAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, BrandsPackagedAtOwnSites(1000L, 1000L)).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, HowManyAsAContractPacker(1000L, 1000L))

  def howManyAsContractPackerPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, BrandsPackagedAtOwnSites(1000L, 1000L)).success.value
    .set(PackagedContractPackerPage, true)

  def exemptionsForSmallProducersPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, BrandsPackagedAtOwnSites(1000L, 1000L)).success.value
    .set(PackagedContractPackerPage, false)

  def exemptionsForSmallProducersFullAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, BrandsPackagedAtOwnSites(1000L, 1000L)).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, HowManyAsAContractPacker(1000L, 1000L)).success.value
    .set(ExemptionsForSmallProducersPage, true)

  def broughtIntoUkPartialAnswers = exemptionsForSmallProducersFullAnswers.success.value
    .set(ExemptionsForSmallProducersPage, false)

  def broughtIntoUkFullAnswers = broughtIntoUkPartialAnswers.success.value
    .set(BroughtIntoUKPage, true)




}
