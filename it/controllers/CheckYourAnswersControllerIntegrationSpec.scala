package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class CheckYourAnswersControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {

  override def configParams: Map[String, Any] = Map(
    "balanceAll.enabled" -> false
  )

  "CheckYourAnswersController" should {

    "Load when valid user answers present" in {

      setAnswers(checkYourAnswersFullAnswers)

      given.commonPrecondition
      given.sdilBackend.balance("XKSDIL000000022", false)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/check-your-answers")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }
      }
    }

    "Load when valid user answers with cached row calculations" in {

      setAnswers(checkYourAnswersFullAnswers)

      given.commonPrecondition
      given.sdilBackend.balance("XKSDIL000000022", false)

      val expectedRowCalculations =
        s"""{
           |"howManyCreditsForLostDamaged":{"lowBandLevy":-180,"highBandLevy":-240},
           |"HowManyBroughtIntoUk":{"lowBandLevy":180,"highBandLevy":240},
           |"howManyAsAContractPacker":{"lowBandLevy":180,"highBandLevy":240},
           |"brandsPackagedAtOwnSites":{"lowBandLevy":180,"highBandLevy":240},
           |"howManyBroughtIntoTheUKFromSmallProducers":{"lowBandLevy":0,"highBandLevy":0},
           |"howManyCreditsForExport":{"lowBandLevy":-180,"highBandLevy":-240},
           |"exemptionsForSmallProducers":{"lowBandLevy":0,"highBandLevy":0}}
           |""".stripMargin

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/check-your-answers")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        val calculations = sdilSessionCacheRepo.get("XKSDIL000000022").map {
          cacheResults => cacheResults.get.data.get("ROW-CALCULATIONS")
        }

        whenReady(result1) { res =>
          res.status mustBe 200
        }

        whenReady(calculations) { calculations =>
          calculations.get mustEqual(Json.parse(expectedRowCalculations))
        }
      }
    }
  }
}