package viewmodels

import base.SpecBase
import models.backend.{Site, UkAddress}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.PackagingSiteDetailsSummary

object packagingSiteDetailsSummarySpec extends SpecBase {

  val packagingSiteSummaryList: List[SummaryListRow] =
    PackagingSiteDetailsSummary.row2(List())(messages(application))

  val list: SummaryList = SummaryListViewModel(
    rows = packagingSiteSummaryList
  )

  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  val PackagingSite2 = Site(
    UkAddress(List("29 Station Place", "Cambridge`"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val packagingSiteListWith2 = List(PackagingSite1, PackagingSite2)
  val packagingSiteListWith1 = List(PackagingSite1)
  val packagingSiteSummaryAliasList1: List[SummaryListRow] = PackagingSiteDetailsSummary.row2(packagingSiteListWith1)
  val aliasList1: SummaryList = SummaryListViewModel(
    rows = packagingSiteSummaryAliasList1
  )
  val packagingSiteSummaryAliasList2: List[SummaryListRow] = PackagingSiteDetailsSummary.row2(packagingSiteListWith2)
  val aliasList2: SummaryList = SummaryListViewModel(
    rows = packagingSiteSummaryAliasList2
  )
}

case class SummaryListViewModel(rows: List[SummaryListRow])
