package controllers

import models.SmallProducer
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class AddASmallProducerControllerIntegrationSpec extends ControllerITTestHelper with TryValues {

  val sdilRefSparkyJuice = "XZSDIL000000234"
  val aliasSparkyJuice = "Sparky Juice"

  val sdilRefSuperCola = "XZSDIL000000235"
  val aliasSuperCola = "Super Cola"

  val litreMax: Long = 100000000000000L
  val litre: Long = litreMax - 1

  "AddASmallProducerController" should {
    "Ask user to input a registered small producer's details" in {
      val userAnswers = addASmallProducerPartialAnswers.success.value
      setUpData(userAnswers)
      given.commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/add-small-producer")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the new form data and navigate to small producer details page " in {

      val expectedResult: Some[List[SmallProducer]] = Some(List(SmallProducer(alias = "Super Cola Ltd" ,
        sdilRef = "XZSDIL000000234", litreage = (1000L,1000L))))

      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = addASmallProducerFullAnswers.success.value
      setUpData(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/add-small-producer")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("producerName" -> "Super Cola Ltd", "referenceNumber" -> "XZSDIL000000234", "lowBand" -> "1000", "highBand" -> "1000"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/small-producer-details")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe expectedResult
        }

      }

    }

    "Load small producer details when producer details in edit mode " in {

      val userAnswers = addASmallProducerPartialAnswers.success.value
      val updatedUserAnswers = userAnswers.copy(smallProducerList = List(
        SmallProducer(s"$aliasSuperCola", s"$sdilRefSuperCola", (litre, litre)))
      )

      setUpData(updatedUserAnswers)

      given
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/add-small-producer-edit?sdilReference=$sdilRefSuperCola")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }
      }
    }

    "Post the updated details of small producer and check it is updated" in {

      val amendedProducerAlias = "Jackson's Breeze"
      val amendedLowBand = "1000"
      val amendedHighBand = "5000"

      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = addASmallProducerFullAnswers.success.value
      val updatedUserAnswers = userAnswers.copy(smallProducerList = List(
        SmallProducer(s"$aliasSuperCola", s"$sdilRefSuperCola", (litre, litre)))
      )

      setUpData(updatedUserAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/add-small-producer-edit?sdilReference=$sdilRefSuperCola")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj(
              "producerName" -> amendedProducerAlias,
              "referenceNumber" -> sdilRefSuperCola,
              "lowBand" -> amendedLowBand,
              "highBand" -> amendedHighBand
            ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/change-small-producer-details")
          val smallProducer = getAnswers("XKSDIL000000022").get.smallProducerList.head
          smallProducer.alias mustEqual amendedProducerAlias
          smallProducer.litreage._1.toString mustEqual amendedLowBand
          smallProducer.litreage._2.toString mustEqual amendedHighBand
        }

      }

    }

    "Post and update the existing small producer details when SDIL ref is changed and not add an additional producer" in {

      val amendedProducerAlias = "Jackson's Breeze"
      val amendedSDILReference = "XCSDIL000000066"
      val amendedLowBand = "1000"
      val amendedHighBand = "5000"

      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = addASmallProducerFullAnswers.success.value
      val updatedUserAnswers = userAnswers.copy(smallProducerList = List(
        SmallProducer(s"$aliasSuperCola", s"$sdilRefSuperCola", (litre, litre)),
        SmallProducer(s"$aliasSparkyJuice", s"$sdilRefSparkyJuice", (litre, litre)))
      )

      setUpData(updatedUserAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/add-small-producer-edit?sdilReference=$sdilRefSuperCola")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj(
              "producerName" -> amendedProducerAlias,
              "referenceNumber" -> amendedSDILReference,
              "lowBand" -> amendedLowBand,
              "highBand" -> amendedHighBand
            ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/change-small-producer-details")
          val smallProducers = getAnswers("XKSDIL000000022").get.smallProducerList
          assert(smallProducers.filter(_.sdilRef == sdilRefSuperCola).isEmpty)
          assert(smallProducers.size == 2)
        }

      }

    }
    testUnauthorisedUser(baseUrl + "/add-small-producer")
  }
}

