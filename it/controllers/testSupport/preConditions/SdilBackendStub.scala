package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod}
import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import play.api.libs.json.Json

import java.time.LocalDate

case class SdilBackendStub()
                           (implicit builder: PreconditionBuilder)
{
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


  val returnPeriod = ReturnPeriod(2018, 1)

  def retrieveSubscription(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(aSubscription).toString())))
    builder
  }

  def oldestPendingReturnPeriod(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.toJson(List(returnPeriod)).toString())))
    builder
  }

  def balance(sdilRef: String, withAssessment: Boolean) = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/$withAssessment"))
        .willReturn(
          ok(Json.toJson(BigDecimal(10000)).toString())))
    builder
  }

  def balanceHistory(sdilRef: String, withAssessment: Boolean) = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/history/all/$withAssessment"))
        .willReturn(
          ok(Json.toJson[Seq[FinancialLineItem]](List(
            ReturnCharge(returnPeriod, BigDecimal(1000)),
            ReturnCharge(ReturnPeriod(2018, 2), BigDecimal(1000)))).toString())))
    builder
  }
}

