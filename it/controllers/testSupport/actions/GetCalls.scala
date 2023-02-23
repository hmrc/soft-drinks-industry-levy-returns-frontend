package controllers.testSupport.actions

import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future

class GetCalls(baseUrl: String) {

  def contactPersonPage(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/form/contactPerson")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" -> "some-id")
      .get()
  }

}

