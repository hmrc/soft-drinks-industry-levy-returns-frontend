package controllers.testSupport

import models.{AddASmallProducer, BrandsPackagedAtOwnSites, HowManyAsAContractPacker, HowManyBroughtIntoUk, UserAnswers}
import org.scalatest.TryValues
import pages.{AddASmallProducerPage, BrandsPackagedAtOwnSitesPage, BroughtIntoUKPage, BroughtIntoUkFromSmallProducersPage, ClaimCreditsForExportsPage, ExemptionsForSmallProducersPage, HowManyAsAContractPackerPage, HowManyBroughtIntoUkPage, OwnBrandsPage, PackagedContractPackerPage}
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

  def broughtIntoUkFromSmallProducersFullAnswers = broughtIntoUkFullAnswers.success.value
    .set(BroughtIntoUKPage, false).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false)

  def creditsForLostDamagedPartialAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value
    .set(ClaimCreditsForExportsPage, false)

  def howManyBroughtIntoUkFullAnswers = broughtIntoUkFullAnswers
    .success.value
    .set(HowManyBroughtIntoUkPage, HowManyBroughtIntoUk(1000L, 1000L))

  def addASmallProducerPartialAnswers = exemptionsForSmallProducersFullAnswers.success.value
    .set(ExemptionsForSmallProducersPage, true)

  def addASmallProducerFullAnswers = addASmallProducerPartialAnswers.success.value
      .set(AddASmallProducerPage, AddASmallProducer(Some("Super Cola Ltd"),"XZSDIL000000234",1000L, 1000L))


}
