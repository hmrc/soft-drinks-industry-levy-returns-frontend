package controllers.addressLookupFrontend

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.backend.UkAddress
import models.{NormalMode, Warehouse}
import org.scalatest.TryValues
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.WarehouseDetails

class RampOffControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {

  s"ramp off $WarehouseDetails" should {
    "redirect to next page when request is valid and address is returned from ALF" when {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        setAnswers(emptyUserAnswers)
        given
          .commonPrecondition
          .alf.getAddress(alfId)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/off-ramp/secondary-warehouses/$sdilId?id=$alfId")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswers.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswers.smallProducerList
            updatedUserAnswers.warehouseList mustBe Map(sdilId -> Warehouse(Some("soft drinks ltd"), UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))))

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
          }

        }
      }
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswers.copy(warehouseList = Map(sdilId -> Warehouse(None, UkAddress(List.empty, "foo", Some("wizz")))))
        setAnswers(userAnswersBefore)
        given
          .commonPrecondition
          .alf.getAddress(alfId)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/off-ramp/secondary-warehouses/$sdilId?id=$alfId")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswers.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswers.smallProducerList
            updatedUserAnswers.warehouseList mustBe Map(sdilId -> Warehouse(Some("soft drinks ltd"), UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))))

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    s"return $INTERNAL_SERVER_ERROR" when {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        setAnswers(emptyUserAnswers)
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/off-ramp/secondary-warehouses/$sdilId?id=$alfId")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswers.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswers.smallProducerList
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

          }
        }
      }
    }
  }
}