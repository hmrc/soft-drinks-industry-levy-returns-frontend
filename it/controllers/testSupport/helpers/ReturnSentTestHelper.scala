package controllers.testSupport.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import models.{ReturnsVariation, SdilReturn}
import models.alf.init.JourneyConfig
import play.api.libs.json.{JsObject, JsString, Json}

import scala.jdk.CollectionConverters._

object ReturnSentTestHelper {

  def checkSdilReturnAndVariationSent(wireMockServer: WireMockServer,
                                      expectedReturn: SdilReturn,
                                      expectedVariation: ReturnsVariation): Boolean = {
    val requestToSendReturn = wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/returns/0000001611/year/")).head
    val returnSentRequestBody =  Json.parse(requestToSendReturn.getBodyAsString)
    val expectedReturnJson = Json.toJson(expectedReturn)

    val requestToSendVariation = wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/returns/variation/sdil")).head
    val variationSentRequestBody = Json.parse(requestToSendVariation.getBodyAsString)
    val expectedVariationJson = Json.toJson(expectedVariation)

    val returnIsSent = returnSentRequestBody == expectedReturnJson
    val returnsVariationIsSent = variationSentRequestBody == expectedVariationJson

    returnIsSent && returnsVariationIsSent
  }
}
