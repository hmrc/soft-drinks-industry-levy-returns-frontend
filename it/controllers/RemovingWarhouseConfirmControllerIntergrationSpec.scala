package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.{Address, UserAnswers, Warehouse}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class RemovingWarhouseConfirmControllerIntergrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {
"RemovingWarhouseConfirmController"should {
    "Ask for if user wants to remove warehouses" in {
      setAnswers(newPackerPartialAnswers.success.value)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/remove-warehouse-details/1")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "user selected yes to remove first warehouse" in {

      val removedWarehouseMap:Map[String,Warehouse] = Map("2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))

      setAnswers(newPackerPartialAnswers.success.value)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/remove-warehouse-details/1")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "true"))


        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/secondary-warehouse-details")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.warehouseList) mustBe Some(removedWarehouseMap)
        }
      }
    }
  }
}
