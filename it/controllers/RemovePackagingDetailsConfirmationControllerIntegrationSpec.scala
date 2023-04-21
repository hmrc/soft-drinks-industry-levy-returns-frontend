package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.backend.{Site, UkAddress}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class RemovePackagingDetailsConfirmationControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData {

  "GET" should {
    "return view" in {
      val ref: String = "foo"
      val packagingSite: Map[String, Site] = Map(ref -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        Some("trading"),
        None))
      val updatedUserAnswers = emptyUserAnswers.copy(packagingSiteList = packagingSite)
      setAnswers(updatedUserAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/remove-packaging-site-details/$ref")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }
      }
    }
  }
  "POST" should {
    "remove packaging site, but not update user answer for page when TRUE" in {
      val ref: String = "foo"
      val ref2: String = "foo2"
      val packagingSites: Map[String, Site] = Map(
        ref -> Site(
          UkAddress(List("a", "b"), "c"),
          None,
          Some("trading"),
          None),
        ref2 -> Site(
          UkAddress(List("d", "e"), "f"),
          None,
          Some("foobar"),
          None))

      val updatedUserAnswers = emptyUserAnswers.copy(packagingSiteList = packagingSites)
      setAnswers(updatedUserAnswers)
      given
        .commonPrecondition
      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/remove-packaging-site-details/$ref")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> updatedUserAnswers.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "true"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/soft-drinks-industry-levy-returns-frontend/packaging-site-details")
          val answersAfterSubmission = getAnswers(updatedUserAnswers.id).get
          answersAfterSubmission.packagingSiteList mustBe packagingSites.tail
          answersAfterSubmission.data mustBe Json.obj()
        }
      }
    }
    "NOT remove packaging site, and not update user answer for page when FALSE" in {
      val ref: String = "foo"
      val ref2: String = "foo2"
      val packagingSites: Map[String, Site] = Map(
        ref -> Site(
          UkAddress(List("a", "b"), "c"),
          None,
          Some("trading"),
          None),
        ref2 -> Site(
          UkAddress(List("d", "e"), "f"),
          None,
          Some("foobar"),
          None))

      val updatedUserAnswers = emptyUserAnswers.copy(packagingSiteList = packagingSites)
      setAnswers(updatedUserAnswers)
      given
        .commonPrecondition
      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/remove-packaging-site-details/$ref")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> updatedUserAnswers.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "false"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/soft-drinks-industry-levy-returns-frontend/packaging-site-details")
          val answersAfterSubmission = getAnswers(updatedUserAnswers.id).get
          answersAfterSubmission.packagingSiteList mustBe packagingSites
          answersAfterSubmission.data mustBe Json.obj()
        }
      }
    }

  }
}
