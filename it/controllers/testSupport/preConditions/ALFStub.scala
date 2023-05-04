package controllers.testSupport.preConditions

import models.Address
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

case class ALFStub()(implicit builder: PreconditionBuilder) {
  val aAddress = Address(
    organisation = Some("soft drinks ltd"),
    line1 = Some("line 1"),
    line2 = Some("line 2"),
    line3 = Some("line 3"),
    line4 = Some("line 4"),
    postcode = Some("aa1 1aa"),
    countryCode = Some("UK")
  )

  def getAddress(id : String) ={
    stubFor(
      get(
        urlPathMatching(s"address-lookup-frontend/api/confirmed?id=$id")
      ).willReturn(
        ok(Json.toJson(aAddress).toString())))
    builder
  }
}
