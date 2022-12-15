package controllers.testSupport.actions

import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future

class PostCalls(baseUrl: String) {

  def declaration(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/submit")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" → "some-id",
        "Csrf-Token" -> "nocheck",
        "Content-Type" → "application/json")
      .post(declarationData)
  }

  private def declarationData = {
    """{
      |"fullName": "John",
      |"jobTitle": "Director",
      |"isUseGgEmail": true,
      |"ggEmail": "user@test.com"
      |}
    """.stripMargin
  }

}

