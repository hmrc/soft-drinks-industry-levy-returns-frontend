package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class OwnBrandsControllerIntegrationSpec extends Specifications with TestConfiguration {
  "OwnBrandsController" should {

    "Ask if user is reporting liable drinks they have packaged as a third party or contract packer at UK sites user operates" in {

      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/own-brands-packaged-at-own-sites")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 200
        }

      }
    }

    "Post the Own brand packaged at own sites " in {

      val declarationData = {
        """{
          |
          |"ownBrands" : false
          |
          |}
        """.stripMargin
      }
      given
        .commonPrecondition


      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/own-brands-packaged-at-own-sites")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "some-id",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(declarationData)

        whenReady(result) { res =>
          println(" res is :;"+res)
          res.status mustBe 303
          println(" header :;"+res.header(HeaderNames.LOCATION))
          res.header(HeaderNames.LOCATION) mustBe Some(s"/how-many-own-brands-packaged-at-own-sites")
        }

      }

    }
  }

}