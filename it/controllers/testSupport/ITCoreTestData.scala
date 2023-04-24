package controllers.testSupport

import models.{AddASmallProducer, LitresInBands, UserAnswers, Warehouse}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt

trait ITCoreTestData extends TryValues {
  val lowBand = 1000L
  val highBand = 1000L
  val sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")
  val refNumber = "XZSDIL000000234"

  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(sdilNumber, Json.obj())

  def ownBrandPageAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true)

  def ownBrandPageFalseAnswers = emptyUserAnswers
    .set(OwnBrandsPage, false)

  def brandPackagedOwnSiteAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand))

  def howManyAsContractPackerFullAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand))

  def howManyAsContractPackerPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
    .set(PackagedContractPackerPage, true)

  def exemptionsForSmallProducersPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
    .set(PackagedContractPackerPage, false)

  def exemptionsForSmallProducersFullAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
    .set(ExemptionsForSmallProducersPage, true)

  def broughtIntoUkPartialAnswers = exemptionsForSmallProducersFullAnswers.success.value
    .set(ExemptionsForSmallProducersPage, false)

  def broughtIntoUkFullAnswers = broughtIntoUkPartialAnswers.success.value
    .set(BroughtIntoUKPage, true)

  def broughtIntoUkFromSmallProducersFullAnswers = broughtIntoUkFullAnswers.success.value
    .set(BroughtIntoUKPage, false).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false)

  def creditsForLostDamagedPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
    .set(ExemptionsForSmallProducersPage, false).success.value
    .set(BroughtIntoUKPage, true).success.value
    .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false).success.value
    .set(ClaimCreditsForExportsPage, false)

  def creditsForCopackerDamagedPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, false).success.value
    .set(PackagedContractPackerPage, false).success.value
    .set(ExemptionsForSmallProducersPage, false).success.value
    .set(BroughtIntoUKPage, false).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false).success.value
    .set(ClaimCreditsForExportsPage, false)

  def howManyBroughtIntoUkFullAnswers = broughtIntoUkFullAnswers
    .success.value
    .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand))

  def addASmallProducerPartialAnswers = exemptionsForSmallProducersFullAnswers.success.value
    .set(ExemptionsForSmallProducersPage, true)

  def addASmallProducerFullAnswers = addASmallProducerPartialAnswers.success.value
      .set(AddASmallProducerPage, AddASmallProducer(producerName, refNumber, lowBand, highBand))

  def smallProducerDetaisPartialAnswers = addASmallProducerFullAnswers.success.value
    .set(AddASmallProducerPage, AddASmallProducer(producerName, refNumber, lowBand, highBand))

  def smallProducerDetaisFullAnswers = addASmallProducerPartialAnswers.success.value
    .set(SmallProducerDetailsPage, true)

  def removeSmallProducerConfirmPartialAnswers = smallProducerDetaisFullAnswers.success.value
    .set(SmallProducerDetailsPage, false)

  def removeSmallProducerConfirmFullAnswers = addASmallProducerPartialAnswers.success.value
    .set(RemoveSmallProducerConfirmPage, true)

  def newPackerPartialAnswers = emptyUserAnswers
    .set(OwnBrandsPage, false).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
    .set(ExemptionsForSmallProducersPage, false).success.value
    .set(BroughtIntoUKPage, false).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false).success.value
    .set(ClaimCreditsForExportsPage, false)

  def checkYourAnswersFullAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true).success.value
    .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
    .set(ExemptionsForSmallProducersPage, false).success.value
    .set(BroughtIntoUKPage, true).success.value
    .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false).success.value
    .set(ClaimCreditsForExportsPage, true).success.value
    .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
    .set(ClaimCreditsForLostDamagedPage, true).success.value
    .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
}
