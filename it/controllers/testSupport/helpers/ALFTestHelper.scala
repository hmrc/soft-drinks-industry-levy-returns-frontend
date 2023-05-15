package controllers.testSupport.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import models.alf.init.JourneyConfig
import play.api.libs.json.{JsObject, JsString, Json}

import scala.jdk.CollectionConverters._

object ALFTestHelper {

  def requestedBodyMatchesExpected(wireMockServer: WireMockServer, bodyExpected: JourneyConfig): Boolean = {
    val requestMadeToAlf = wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/api/init")).head
    val jsonBodyOfRequest =  Json.parse(requestMadeToAlf.getBodyAsString).as[JsObject]
    val jsonBodyRequestWithNewContinueUrl = jsonBodyOfRequest.+(
      ("options" -> jsonBodyOfRequest("options").as[JsObject].+ ("continueUrl" -> JsString(""))))

    val jsonBodyOfExpectedPost = Json.toJson(bodyExpected).as[JsObject]
    val jsonBodyExpectedWithNewContinueUrl = jsonBodyOfExpectedPost.+(
      "options" -> jsonBodyOfExpectedPost("options").as[JsObject].+ ("continueUrl" -> JsString("")))

    val continueUrlSentMatches = {
      if(bodyExpected.options.continueUrl.nonEmpty) {
        requestMadeToAlf.getBodyAsString.contains(bodyExpected.options.continueUrl.substring(0, bodyExpected.options.continueUrl.indexOf("/")))
      } else { true }
    }

    jsonBodyRequestWithNewContinueUrl == jsonBodyExpectedWithNewContinueUrl && continueUrlSentMatches
  }
}
