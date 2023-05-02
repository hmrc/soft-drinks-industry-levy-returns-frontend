package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.Warehouse
import models.backend.UkAddress
import org.scalatest.TryValues
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class RemovingWarehouseConfirmControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues  {

  "RemovingWarehouseConfirmController" should {
    "Ask for if user wants to remove warehouses" in {
      val twoWarehouses: Map[String, Warehouse] = Map(
        "1" -> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
        "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

      setAnswers(newPackerPartialAnswers.copy( warehouseList = twoWarehouses))
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/remove-warehouse-details/1")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe OK
          res.body must include ("ABC Ltd<br>33 Rhes Priordy, East London, Line 3, Line 4, WR53 7CX")
          res.body must include ("Are you sure you want to remove this warehouse?")
        }

      }
    }

    "user selected yes to remove first warehouse" in {

      val twoWarehouses: Map[String,Warehouse] = Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
        "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))
      val removedWarehouseMap: Map[String,Warehouse] = Map("2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))
      setAnswers(newPackerPartialAnswers.copy(id = sdilNumber , warehouseList = twoWarehouses))
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

    setAnswers(newPackerPartialAnswers)
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
