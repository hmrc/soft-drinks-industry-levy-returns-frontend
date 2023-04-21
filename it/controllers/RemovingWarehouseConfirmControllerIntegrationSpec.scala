package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.{Address, UserAnswers, Warehouse}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.TryValues
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.contentAsString
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class RemovingWarehouseConfirmControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues  {

  "RemovingWarehouseConfirmController" should {
    "Ask for if user wants to remove warehouses" in {
      def doc(result: String): Document = Jsoup.parse(result)
      val twoWarhouses:Map[String,Warehouse] = Map("1"-> Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
        "2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))
      setAnswers(newPackerPartialAnswers.success.value.copy( warehouseList = twoWarhouses))
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/remove-warehouse-details/1")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe OK
          res.body must include ("ABC Ltd, 33 Rhes Priordy, East London, Line 3, Line 4, WR53 7CX")
          res.body must include ("Are you sure you want to remove this warehouse?")
          println(res.body)
        }

      }
    }

    "user selected yes to remove first warehouse" in {


      val twoWarhouses:Map[String,Warehouse] = Map("1"-> Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
        "2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))
      val removedWarehouseMap: Map[String,Warehouse] = Map("2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))
      setAnswers(newPackerPartialAnswers.success.value.copy(id = sdilNumber , warehouseList = twoWarhouses))
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
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/secondary-warehouse-details")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.warehouseList) mustBe Some(removedWarehouseMap)
        }
      }
    }

  "user inputs remove on index that doesn't exist" in {

    setAnswers(newPackerPartialAnswers.success.value)
    given
      .commonPrecondition

    WsTestClient.withClient { client =>
      val result = client.url(s"$baseUrl/remove-warehouse-details/3")
        .withFollowRedirects(false)
        .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
        .get()

      whenReady(result) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("/soft-drinks-industry-levy-returns-frontend/secondary-warehouse-details")
      }
    }
  }
  }
}
