package controllers

import models.SmallProducer
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class RemoveSmallProducerConfirmControllerIntegrationSpec extends ControllerITTestHelper with TryValues {

  val sdilRefPartyDrinks = "XZSDIL000000234"
  val sdilRefSuperCola = "XZSDIL000000235"
  val aliasPartyDrinks = "Party Drinks Group"
  val aliasSuperCola = "Super Cola"
  val litreMax: Long = 100000000000000L
  val smallLitre: Long = litreMax - 1
  val largeLitre: Long = litreMax - 1

  "RemoveSmallProducerConfirmController" should {
    "Ask for if user wants to remove this small producer" in {

      val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
      val updatedUserAnswers = userAnswers.copy(smallProducerList = List(SmallProducer(s"$aliasPartyDrinks",s"$sdilRefPartyDrinks",(smallLitre,largeLitre))))
        setUpData(updatedUserAnswers)
      given
      .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/remove-small-producer-confirm/$sdilRefPartyDrinks")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the removal for selected small producer " when {

      "user selected yes " in {

        given
          .commonPreconditionChangeSubscription(aSubscription)

        val smallProducerToRemove = SmallProducer(s"$aliasPartyDrinks",s"$sdilRefPartyDrinks",(smallLitre,largeLitre))
        val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
        val updatedUserAnswers = userAnswers.copy(smallProducerList = List(
          smallProducerToRemove,
          SmallProducer(s"$aliasSuperCola",s"$sdilRefSuperCola",(smallLitre,largeLitre)))
        )

        setUpData(updatedUserAnswers)


        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/remove-small-producer-confirm/$sdilRefPartyDrinks")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/small-producer-details")
            getAnswers("XKSDIL000000022").fold(List.empty[SmallProducer])(_.smallProducerList) mustNot contain(smallProducerToRemove)
          }

        }
      }

      "user selected yes when last small producer is being removed" in {

        given
          .commonPreconditionChangeSubscription(aSubscription)

        val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
        val updatedUserAnswers = userAnswers.copy(smallProducerList = List(
          SmallProducer(s"$aliasPartyDrinks", s"$sdilRefPartyDrinks", (smallLitre, largeLitre)))
        )

        setUpData(updatedUserAnswers)


        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/remove-small-producer-confirm/$sdilRefPartyDrinks")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/exemptions-for-small-producers")
          }

        }
      }

      "user selected no " in {

        val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
        val updatedUserAnswers = userAnswers.copy(smallProducerList = List(SmallProducer(s"$aliasPartyDrinks",s"$sdilRefPartyDrinks",(smallLitre,largeLitre))))
        setUpData(updatedUserAnswers)

        given
          .commonPreconditionChangeSubscription(aSubscription)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/remove-small-producer-confirm/$sdilRefPartyDrinks")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/small-producer-details")
          }

        }
      }
    }


  }

}

