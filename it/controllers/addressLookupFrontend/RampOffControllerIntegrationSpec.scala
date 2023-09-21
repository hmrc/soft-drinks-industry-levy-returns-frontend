package controllers.addressLookupFrontend

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.{CheckMode, NormalMode}
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.{PackingDetails, WarehouseDetails}

class RampOffControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {

  val modes = List(NormalMode, CheckMode)

  val siteId: String = "foo"
  val alfId: String = "bar"

  modes.foreach { case mode =>
    s"ramp off $WarehouseDetails when in $mode" should {
      val path = if(mode == NormalMode) {
        s"$baseUrl/off-ramp/secondary-warehouses/$siteId?id=$alfId"
      } else {
        s"$baseUrl/off-ramp/change-secondary-warehouses/$siteId?id=$alfId"
      }
      "redirect to next page when request is valid and address is returned from ALF" when {
        "no address exists in DB currently for SDILID provided" in {
          setUpData(emptyUserAnswers)
          given
            .commonPreconditionChangeSubscription(aSubscription)
            .alf.getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client.url(path)
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
              updatedUserAnswers.warehouseList mustBe Map(siteId -> Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), tradingName = Some("soft drinks ltd")))

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url)
            }

          }
        }
        "an address already exists in DB currently for SDILID provided" in {
          val userAnswersBefore = emptyUserAnswers.copy(warehouseList = Map(siteId -> Site(UkAddress(List.empty, "foo", Some("wizz")), tradingName = None)))
          setUpData(userAnswersBefore)
          given
            .commonPreconditionChangeSubscription(aSubscription)
            .alf.getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client.url(path)
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
              updatedUserAnswers.warehouseList mustBe Map(siteId -> Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), tradingName = Some("soft drinks ltd")))

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }
      s"return $INTERNAL_SERVER_ERROR" when {
        "alf returns error" in {
          setUpData(emptyUserAnswers)
          given
            .commonPreconditionChangeSubscription(aSubscription)
            .alf.getBadAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client.url(path)
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
    s"ramp off $PackingDetails when in $mode" should {
      val path = if (mode == NormalMode) {
        s"$baseUrl/off-ramp/packing-site-details/$siteId?id=$alfId"
      } else {
        s"$baseUrl/off-ramp/change-packing-site-details/$siteId?id=$alfId"
      }
      "redirect to next page when request is valid and address is returned from ALF" when {
        "no address exists in DB currently for SDILID provided" in {
          setUpData(emptyUserAnswers)
          given
            .commonPreconditionChangeSubscription(aSubscription)
            .alf.getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client.url(path)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe Map(siteId ->
                Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))
              updatedUserAnswers.submitted mustBe emptyUserAnswers.submitted
              updatedUserAnswers.smallProducerList mustBe emptyUserAnswers.smallProducerList
              updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(mode).url)
            }

          }
        }
        "an address already exists in DB currently for SDILID provided" in {
          val userAnswersBefore = emptyUserAnswers.copy(packagingSiteList = Map(siteId -> Site(UkAddress(List.empty, "foo", Some("wizz")), None, None, None)))
          setUpData(userAnswersBefore)
          given
            .commonPreconditionChangeSubscription(aSubscription)
            .alf.getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client.url(path)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe Map(siteId ->
                Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))
              updatedUserAnswers.submitted mustBe emptyUserAnswers.submitted
              updatedUserAnswers.smallProducerList mustBe emptyUserAnswers.smallProducerList
              updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }
      s"return $INTERNAL_SERVER_ERROR" when {
        "alf returns error" in {
          setUpData(emptyUserAnswers)
          given
            .commonPreconditionChangeSubscription(aSubscription)
            .alf.getBadAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client.url(path)
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
}
