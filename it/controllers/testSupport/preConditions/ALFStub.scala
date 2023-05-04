package controllers.testSupport.preConditions

import models.AlfResponse
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

case class ALFStub()(implicit builder: PreconditionBuilder) {
  val aAddress = AlfResponse(
    organisation = Some("soft drinks ltd"),
    List("line 1", "line 2", "line 3", "line 4"),
    postcode = Some("aa1 1aa"),
    countryCode = Some("UK")
  )

  def getAddress(id : String) ={
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id",equalTo(id))
        .willReturn(
        ok(Json.toJson(aAddress).toString())))
    builder
  }
}
