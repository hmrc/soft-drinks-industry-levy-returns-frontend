package controllers.testSupport

import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{AddASmallProducer, DefaultUserAnswersData, LitresInBands, UserAnswers}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.duration.DurationInt
import scala.util.Failure

trait ITCoreTestData extends TryValues {
  val lowBand = 1000L
  val highBand = 1000L
  val sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")
  val refNumber = "XZSDIL000000234"


  val aSubscription = RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(sdilNumber, Json.obj())
  def defaultNilReturnUserAnswers = UserAnswers(sdilNumber, Json.toJsObject(new DefaultUserAnswersData(aSubscription)), isNilReturn = true)
  def submittedAnswers = UserAnswers(sdilNumber, Json.obj(), submitted = true)

  def failedUserAnswers = Failure(new Exception(""))

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

  def smallProducerDetaisNoProducerAnswers = addASmallProducerPartialAnswers.success.value

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
    .set(ClaimCreditsForExportsPage, false).success.value

  def newPackerPartialNewImporterAnswers = emptyUserAnswers
    .set(OwnBrandsPage, false).success.value
    .set(PackagedContractPackerPage, true).success.value
    .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
    .set(ExemptionsForSmallProducersPage, false).success.value
    .set(BroughtIntoUKPage, false).success.value
    .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false).success.value
    .set(ClaimCreditsForExportsPage, false).success.value

  def newImporterAnswers = emptyUserAnswers
    .set(OwnBrandsPage, false).success.value
    .set(PackagedContractPackerPage, false).success.value
    .set(ExemptionsForSmallProducersPage, false).success.value
    .set(BroughtIntoUKPage, true).success.value
    .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
    .set(BroughtIntoUkFromSmallProducersPage, false).success.value
    .set(ClaimCreditsForExportsPage, false).success.value

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

def claimCreditsForLostDamagedPageWithLitresFullAnswers = newPackerPartialAnswers
  .set(ClaimCreditsForExportsPage, true).success.value
  .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand))

  def returnSentAnswersFullAnswers = submittedAnswers
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
