package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock.{get, ok, post, put, stubFor, urlEqualTo, urlPathEqualTo, urlPathMatching}
import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import play.api.libs.json.Json

import java.time.LocalDate

case class SdilBackendStub()
                           (implicit builder: PreconditionBuilder)
{
  val aSubscription = RetrievedSubscription(
    "0000000022",
    "XKSDIL000000022",
    "Super Lemonade Plc",
    UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    RetrievedActivity(false, true, false, false, false),
    LocalDate.of(2018, 4, 19),
    List(
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
    List(),
    Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    None
  )


  def retrieveSubscription(identifierType: String, sdilNumber: String) = {
    stubFor(
      put(urlPathEqualTo(
        s"/subscription/$identifierType/$sdilNumber")
      )
        .willReturn(ok(
          Json.toJson(aSubscription).toString()
        ))
    )
    builder
  }
}

