package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.retrieved.RetrievedSubscription
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod}
import play.api.libs.json.Json

case class SdilBackendStub()
                           (implicit builder: PreconditionBuilder)
{

  val returnPeriod = ReturnPeriod(2018, 1)

  def retrieveSubscription(identifier: String, refNum: String, retrievedSubscription: RetrievedSubscription) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(retrievedSubscription).toString())))
    builder
  }

  def pendingReturnPeriod(utr: String) = {
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
  def submitReturns(utr: String = "0000001611", year: Int = 2018, quarter: Int = 1) = {
    stubFor(
      post(
        urlPathMatching(s"/returns/$utr/year/$year/quarter/$quarter"))
        .willReturn(
          ok()))
    builder
  }
  def submitVariations(sdilRef: String = "XKSDIL000000022") = {
    stubFor(
      post(
        urlPathMatching(s"/returns/variation/sdil/$sdilRef"))
        .willReturn(
          noContent()))
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

