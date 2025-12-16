/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  val alfId:  String = "bar"

  modes.foreach { case mode =>
    s"ramp off $WarehouseDetails when in $mode" should {
      val path = if mode == NormalMode then {
        s"$baseUrl/off-ramp/secondary-warehouses/$siteId?id=$alfId"
      } else {
        s"$baseUrl/off-ramp/change-secondary-warehouses/$siteId?id=$alfId"
      }
      "redirect to next page when request is valid and address is returned from ALF" when {
        "no address exists in DB currently for SDILID provided" in {
          setUpData(emptyUserAnswers)
          build
            .commonPreconditionChangeSubscription(aSubscription)
            .alf
            .getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client
              .url(path)
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
              updatedUserAnswers.warehouseList mustBe Map(
                siteId -> Site(
                  UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)),
                  tradingName = Some("soft drinks ltd")
                )
              )

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url)
            }

          }
        }
        "an address already exists in DB currently for SDILID provided" in {
          val userAnswersBefore =
            emptyUserAnswers.copy(warehouseList = Map(siteId -> Site(UkAddress(List.empty, "foo", Some("wizz")), tradingName = None)))
          setUpData(userAnswersBefore)
          build
            .commonPreconditionChangeSubscription(aSubscription)
            .alf
            .getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client
              .url(path)
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
              updatedUserAnswers.warehouseList mustBe Map(
                siteId -> Site(
                  UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)),
                  tradingName = Some("soft drinks ltd")
                )
              )

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }
      s"return $INTERNAL_SERVER_ERROR" when {
        "alf returns error" in {
          setUpData(emptyUserAnswers)
          build
            .commonPreconditionChangeSubscription(aSubscription)
            .alf
            .getBadAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client
              .url(path)
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
      val path = if mode == NormalMode then {
        s"$baseUrl/off-ramp/packing-site-details/$siteId?id=$alfId"
      } else {
        s"$baseUrl/off-ramp/change-packing-site-details/$siteId?id=$alfId"
      }
      "redirect to next page when request is valid and address is returned from ALF" when {
        "no address exists in DB currently for SDILID provided" in {
          setUpData(emptyUserAnswers)
          build
            .commonPreconditionChangeSubscription(aSubscription)
            .alf
            .getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client
              .url(path)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe Map(
                siteId ->
                  Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None)
              )
              updatedUserAnswers.submitted mustBe emptyUserAnswers.submitted
              updatedUserAnswers.smallProducerList mustBe emptyUserAnswers.smallProducerList
              updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(mode).url)
            }

          }
        }
        "an address already exists in DB currently for SDILID provided" in {
          val userAnswersBefore =
            emptyUserAnswers.copy(packagingSiteList = Map(siteId -> Site(UkAddress(List.empty, "foo", Some("wizz")), None, None, None)))
          setUpData(userAnswersBefore)
          build
            .commonPreconditionChangeSubscription(aSubscription)
            .alf
            .getAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client
              .url(path)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe Map(
                siteId ->
                  Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None)
              )
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
          build
            .commonPreconditionChangeSubscription(aSubscription)
            .alf
            .getBadAddress(alfId)

          WsTestClient.withClient { client =>
            val result = client
              .url(path)
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
