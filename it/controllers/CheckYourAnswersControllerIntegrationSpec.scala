package controllers

import controllers.testSupport.helpers.ReturnSentTestHelper
import models.retrieved.{OptSmallProducer, RetrievedActivity}
import models.{AddASmallProducer, LitresInBands, NormalMode}
import org.jsoup.Jsoup
import pages._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.await
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import repositories.SDILSessionKeys
import play.api.libs.ws.DefaultBodyWritables.*
import play.api.libs.ws.JsonBodyWritables.*

class CheckYourAnswersControllerIntegrationSpec extends ControllerITTestHelper with CheckYourAnswersPageValidationHelper {

  override def configParams: Map[String, Any] = Map(
    "balanceAll.enabled" -> false
  )

  "CheckYourAnswersController" when {
    "GET" should {
      "Load when NIL return" in {
        setUpData(defaultNilReturnUserAnswers)

        build.commonPreconditionChangeSubscription(aSubscription)
        build.sdilBackend.balance(emptyUserAnswers.id, false)
        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }

      "Load when valid user answers present where all activities are set to true" in {
        val userAnswersStandardFlow = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value

        setUpData(userAnswersStandardFlow)

        build.commonPreconditionChangeSubscription(aSubscription.copy(activity = RetrievedActivity(true, true, true, true, true)))
        build.sdilBackend.balance(userAnswersStandardFlow.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }

      "Load when valid user answers present where all activities are set to false and user must see all pages in service" in {
        val userAnswersAllPages = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(AskSecondaryWarehouseInReturnPage, true).success.value

        setUpData(userAnswersAllPages)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(false, false, false, false, false),
          warehouseSites = List.empty))
        build.sdilBackend.balance(userAnswersAllPages.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }

      "Load when valid user answers present where all activities are set to false (apart from small producer = true) " +
        "and user must see all pages in service apart from own brands" in {
        val userAnswersAllPages = emptyUserAnswers
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(AskSecondaryWarehouseInReturnPage, true).success.value

        setUpData(userAnswersAllPages)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(smallProducer = true, false, false, false, false),
          warehouseSites = List.empty))
        build.sdilBackend.balance(userAnswersAllPages.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }

      "Load when minimum amount of pages answered, all activities true" in {
        val userAnswersAllPages = emptyUserAnswers
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value
          .set(ClaimCreditsForLostDamagedPage, false).success.value

        setUpData(userAnswersAllPages)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(smallProducer = true, true, true, true, true),
          warehouseSites = List.empty))
        build.sdilBackend.balance(userAnswersAllPages.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }

      "Redirect to first missing page when missing answers and small producer = true" in {
        val userAnswerMissing = emptyUserAnswers
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value

        setUpData(userAnswerMissing)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(smallProducer = true, largeProducer = false, true, true, true),
          warehouseSites = List.empty))
        build.sdilBackend.balance(userAnswerMissing.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode).url
          }
        }
      }

      "Redirect to start page when missing answers and small producer = false" in {
        val userAnswerMissing = emptyUserAnswers
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value

        setUpData(userAnswerMissing)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(smallProducer = false, true, true, true, true),
          warehouseSites = List.empty))

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.OwnBrandsController.onPageLoad(NormalMode).url
          }
        }
      }

      "Contain packaging sites if changed answers mean user is a new packer" in {
        val userAnswersStandardFlow = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value

        val userAnswers = userAnswersStandardFlow.copy(packagingSiteList = Map("x" -> PackagingSite1))

        setUpData(userAnswers)

        build.commonPreconditionChangeSubscription(aSubscription.copy(activity = RetrievedActivity(true, true, false, true, true)))
        build.sdilBackend.balance(userAnswers.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val doc = Jsoup.parse(res.body)
            getAnswers(sdilNumber).map(userAnswers => userAnswers.packagingSiteList).get mustBe Map("x" -> PackagingSite1)
            doc.getElementsByTag("dt").text() must include(Messages("You have 1 packaging site"))
          }
        }
      }

      "Clear packaging sites if changed answers mean user is no longer a new packer" in {
        val userAnswersStandardFlow = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value

        val userAnswers = userAnswersStandardFlow.copy(packagingSiteList = Map("x" -> PackagingSite1))

        setUpData(userAnswers)

        build.commonPreconditionChangeSubscription(aSubscription.copy(activity = RetrievedActivity(true, true, false, true, true)))
        build.sdilBackend.balance(userAnswers.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val doc = Jsoup.parse(res.body)
            getAnswers(sdilNumber).map(userAnswers => userAnswers.packagingSiteList).get mustBe Map.empty
            doc.getElementsByTag("dt").text() mustNot include(Messages("You have 1 packaging site"))
          }
        }
      }

      "Contain warehouses if user has entered a new warehouse and user is a new importer" in {
        val userAnswersStandardFlow = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
          .set(AskSecondaryWarehouseInReturnPage, true).success.value

        val userAnswers = userAnswersStandardFlow.copy(warehouseList = Map("x" -> warehouse))

        setUpData(userAnswers)

        build.commonPreconditionChangeSubscription(aSubscription.copy(activity = RetrievedActivity(true, true, true, false, true)))
        build.sdilBackend.balance(userAnswers.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val doc = Jsoup.parse(res.body)
            doc.getElementsByTag("dt").text() must include(Messages("You have 1 warehouse"))
          }
        }
      }

      "Clear warehouses if changed answers mean user is no longer a new importer" in {
        val userAnswersStandardFlow = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
          .set(AskSecondaryWarehouseInReturnPage, true).success.value

        val userAnswers = userAnswersStandardFlow.copy(warehouseList = Map("x" -> warehouse))

        setUpData(userAnswers)

        build.commonPreconditionChangeSubscription(aSubscription.copy(activity = RetrievedActivity(true, true, true, false, true)))
        build.sdilBackend.balance(userAnswers.id, false)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val doc = Jsoup.parse(res.body)
            getAnswers(sdilNumber).map(userAnswers => userAnswers.warehouseList).get mustBe Map.empty
            doc.getElementsByTag("dt").text() mustNot include(Messages("You have 1 warehouse"))
          }
        }
      }
    }



    "POST" should {
      "Redirect to return sent when NIL return" in {
        setUpData(defaultNilReturnUserAnswers)

        setUpDataWithBackendCallsForAmountsCached(defaultNilReturnUserAnswers)
        build
          .commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns(aSubscription.utr)
          .sdilBackend.submitVariations(defaultNilReturnUserAnswers.id)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> defaultNilReturnUserAnswers.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj())

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.ReturnSentController.onPageLoad.url
          }
        }
      }

      "Redirect to return sent when all answers are complete and retrieved activities are all false" in {
        val userAnswersAllPages = emptyUserAnswers
          .set(OwnBrandsPage, true).success.value
          .set(BrandsPackagedAtOwnSitesPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(AskSecondaryWarehouseInReturnPage, true).success.value

        setUpData(userAnswersAllPages)
        setUpDataWithBackendCallsForAmountsCached(userAnswersAllPages)
        build
          .commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(false, false, false, false, false),
          warehouseSites = List.empty))
          .sdilBackend.submitReturns(aSubscription.utr)
          .sdilBackend.submitVariations(userAnswersAllPages.id)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> userAnswersAllPages.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj())

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.ReturnSentController.onPageLoad.url
          }
        }
      }

      "Return OK when all answers are minimally complete and retrieved activities are all true" in {
        val userAnswersAllPages = emptyUserAnswers
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value
          .set(ClaimCreditsForLostDamagedPage, false).success.value

        setUpData(userAnswersAllPages)
        setUpDataWithBackendCallsForAmountsCached(userAnswersAllPages)
        build
          .commonPreconditionChangeSubscription(aSubscription.copy(
            activity = RetrievedActivity(true, true, true, true, true),
            warehouseSites = List.empty))
          .sdilBackend.submitReturns(aSubscription.utr)
          .sdilBackend.submitVariations(userAnswersAllPages.id)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> userAnswersAllPages.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj())

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.ReturnSentController.onPageLoad.url
          }
        }
      }

      "Return OK when all answers are complete and retrieved activities are all false (apart from small producer = true)" in {
        val userAnswersAllPages = emptyUserAnswers
          .set(PackagedContractPackerPage, true).success.value
          .set(HowManyAsAContractPackerPage, LitresInBands(lowBand, highBand)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(AddASmallProducerPage, AddASmallProducer(None, "", 0, 0)).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUkPage, LitresInBands(lowBand, highBand)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoTheUKFromSmallProducersPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyCreditsForExportPage, LitresInBands(lowBand, highBand)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowBand, highBand)).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(AskSecondaryWarehouseInReturnPage, true).success.value

        setUpData(userAnswersAllPages)
        setUpDataWithBackendCallsForAmountsCached(userAnswersAllPages)
        build
          .commonPreconditionChangeSubscription(aSubscription.copy(
            activity = RetrievedActivity(smallProducer = true, false, false, false, false),
            warehouseSites = List.empty))
          .sdilBackend.submitReturns(aSubscription.utr)
          .sdilBackend.submitVariations(userAnswersAllPages.id)

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> userAnswersAllPages.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj())

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.ReturnSentController.onPageLoad.url
          }
        }
      }

      "Redirect to start of journey when answers incomplete and small producer = false" in {
        val userAnswerMissing = emptyUserAnswers
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value

        setUpData(userAnswerMissing)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(smallProducer = false, true, true, true, true),
          warehouseSites = List.empty))

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> userAnswerMissing.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj())

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.OwnBrandsController.onPageLoad(NormalMode).url
          }
        }
      }

      "Redirect to missing first missing page in the journey when answers incomplete and small producer = true" in {
        val userAnswerMissing = emptyUserAnswers
          .set(PackagedContractPackerPage, false).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value

        setUpData(userAnswerMissing)

        build.commonPreconditionChangeSubscription(aSubscription.copy(
          activity = RetrievedActivity(smallProducer = true, true, true, true, true),
          warehouseSites = List.empty))

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/check-your-answers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> userAnswerMissing.id,
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj())

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe controllers.routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode).url
          }
        }
      }
    }
  }


  "POST /check-your-answers" should {
    "send the return and return variation and redirect to returnSent" when {
      "a nil return was submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceZero))

        setUpData(defaultNilReturnUserAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)

          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, emptyReturn, emptyVariation)
          }
        }
      }

      "a nil return was submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balance))

        setUpData(defaultNilReturnUserAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)

          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, emptyReturn, emptyVariation)
          }
        }
      }

      "a nil return was submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceNegative))

        setUpData(defaultNilReturnUserAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)

          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, emptyReturn, emptyVariation)
          }
        }
      }

      "all answers equal no were submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceZero))
        setUpData(checkYourAnswersFullAnswersAllNo)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, emptyReturn, emptyVariation)
          }
        }
      }

      "all answers equal no were submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balance))
        setUpData(checkYourAnswersFullAnswersAllNo)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, emptyReturn, emptyVariation)
          }
        }
      }

      "all answers equal no were submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceNegative))
        setUpData(checkYourAnswersFullAnswersAllNo)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, emptyReturn, emptyVariation)
          }
        }
      }


      "all answers equal yes with litres were submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceZero))
        setUpData(checkYourAnswersFullAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, populatedReturn, populatedVariation)
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balance))
        setUpData(checkYourAnswersFullAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, populatedReturn, populatedVariation)
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceNegative))
        setUpData(checkYourAnswersFullAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, populatedReturn, populatedVariation)
          }
        }
      }

      "all answers equal yes with litres were submitted, the balance brought forward is 0 and the user is a small producer" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(true))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceZero))
        setUpData(checkYourAnswersFullAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, populatedReturn, populatedVariation)
          }
        }
      }

      "all answers equal yes with litres were submitted, the balance brought forward is positive and the user is a small producer" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(true))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balance))
        setUpData(checkYourAnswersFullAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, populatedReturn, populatedVariation)
          }
        }
      }

      "all answers equal yes with litres were submitted, the balance brought forward is negative and the user is a small producer" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(true))))
        await(sdilSessionCache.save[BigDecimal](sdilNumber, SDILSessionKeys.balance(false), balanceNegative))
        setUpData(checkYourAnswersFullAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.submitReturns()
          .sdilBackend.submitVariations()

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
              "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).value mustBe routes.ReturnSentController.onPageLoad.url
            getAnswers(sdilNumber).get.submitted mustBe true
            ReturnSentTestHelper.checkSdilReturnAndVariationSent(wireMockServer, populatedReturn, populatedVariation)
          }
        }
      }
    }
  }
  testUnauthorisedUser(baseUrl + "/check-your-answers")
}
