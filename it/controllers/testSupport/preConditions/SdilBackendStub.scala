package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod}
import play.api.libs.json.Json

import java.time.LocalDate

case class SdilBackendStub()
                           (implicit builder: PreconditionBuilder)
{
  val aReceivedSubscription: RetrievedSubscription = RetrievedSubscription(
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

  val aSubscriptionWithDeRegDate: RetrievedSubscription = aReceivedSubscription.copy(
    deregDate = Some(LocalDate.of(2022, 2, 11)))

  val returnPeriod: ReturnPeriod = ReturnPeriod(2018, 1)

  def retrieveSubscription(identifier: String, refNum: String): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(aReceivedSubscription).toString())))
    builder
  }

  def retrieveSubscriptionToModify(identifier: String, refNum: String, retrievedSubscription: RetrievedSubscription): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(retrievedSubscription).toString())))
    builder
  }

  def retrieveSubscriptionWithDeRegDate(identifier: String, refNum: String): PreconditionBuilder = {
    stubFor(
      get(
        urlEqualTo(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(aSubscriptionWithDeRegDate).toString())))
    builder
  }

  def retrieveSubscriptionNone(identifier: String, refNum: String): PreconditionBuilder = {
    stubFor(
      get(
        urlPathEqualTo(s"/subscription/$identifier/$refNum"))
        .willReturn(
          notFound()))
    builder
  }

  def pendingReturnPeriod(utr: String): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.toJson(List(returnPeriod)).toString())))
    builder
  }

  def balance(sdilRef: String, withAssessment: Boolean): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/$withAssessment"))
        .willReturn(
          ok(Json.toJson(BigDecimal(10000)).toString())))
    builder
  }

  def balanceNegative(sdilRef: String, withAssessment: Boolean): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/$withAssessment"))
        .willReturn(
          ok(Json.toJson(BigDecimal(-1000)).toString())))
    builder
  }

  def balanceNone(sdilRef: String, withAssessment: Boolean): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/$withAssessment"))
        .willReturn(
          ok(Json.toJson(BigDecimal(0)).toString())))
    builder
  }
  def submitReturns(utr: String = "0000001611", year: Int = 2018, quarter: Int = 1): PreconditionBuilder = {
    stubFor(
      post(
        urlPathMatching(s"/returns/$utr/year/$year/quarter/$quarter"))
        .willReturn(
          ok()))
    builder
  }
  def submitVariations(sdilRef: String = "XKSDIL000000022"): PreconditionBuilder = {
    stubFor(
      post(
        urlPathMatching(s"/returns/variation/sdil/$sdilRef"))
        .willReturn(
          noContent()))
    builder
  }

  def balanceHistoryNone(sdilRef: String): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/history/all/false"))
        .willReturn(
          ok(Json.toJson[Seq[FinancialLineItem]](List(
            ReturnCharge(returnPeriod, BigDecimal(0)))).toString())))
    builder
  }

  def balanceHistoryInCredit(sdilRef: String): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/history/all/false"))
        .willReturn(
          ok(Json.toJson[Seq[FinancialLineItem]](List(
            ReturnCharge(returnPeriod, BigDecimal(-1000)))).toString())))
    builder
  }


  def balanceHistory(sdilRef: String): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/history/all/false"))
        .willReturn(
          ok(Json.toJson[Seq[FinancialLineItem]](List(
            ReturnCharge(returnPeriod, BigDecimal(1000)),
            ReturnCharge(ReturnPeriod(2018, 2), BigDecimal(1000)))).toString())))
    builder

  }

}
