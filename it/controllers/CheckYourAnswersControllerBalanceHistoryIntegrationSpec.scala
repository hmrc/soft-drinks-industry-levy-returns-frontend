package controllers

import controllers.testSupport.helpers.ReturnSentTestHelper
import models.retrieved.OptSmallProducer
import models.{Amounts, FinancialLineItem}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.await
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import repositories.SDILSessionKeys

class CheckYourAnswersControllerBalanceHistoryIntegrationSpec extends CheckYourAnswersPageValidationHelper {

  override def configParams: Map[String, Any] = Map(
    "balanceAll.enabled" -> true
  )

  "GET /check-your-answers - balanceAll enabled" should {
    "render the check your answers page with details of the return" when {
      "a nil return was submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))

        setUpData(defaultNilReturnUserAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryNone(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 0)
            validateSummaryRowsPresentWithAllNo(page, amountsZero, true)
          }
        }
      }

      "a nil return was submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))

        setUpData(defaultNilReturnUserAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)

          .sdilBackend.balanceHistoryInCredit(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 1000)
            validateSummaryRowsPresentWithAllNo(page, Amounts(0, -1000, 1000), true)
          }
        }
      }

      "a nil return was submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))

        setUpData(defaultNilReturnUserAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistory(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, -2000)
            validateSummaryRowsPresentWithAllNo(page, Amounts(0, 2000, -2000), true)
          }
        }
      }

      "all answers equal no were submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        setUpData(checkYourAnswersFullAnswersAllNo)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryNone(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 0)
            validateSummaryRowsPresentWithAllNo(page, amountsZero, true)
          }
        }
      }

      "all answers equal no were submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        setUpData(checkYourAnswersFullAnswersAllNo)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryInCredit(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 1000)
            validateSummaryRowsPresentWithAllNo(page, Amounts(0, -1000, 1000), true)
          }
        }
      }

      "all answers equal no were submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        setUpData(checkYourAnswersFullAnswersAllNo)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistory(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, -2000)
            validateSummaryRowsPresentWithAllNo(page, Amounts(0, 2000, -2000), true)
          }
        }
      }


      "all answers equal yes with litres were submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryNone(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 420)
            validateSummaryRowsPresentWithAllYes(page, Amounts(420, 0, 420), true)
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryInCredit(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 1420)
            validateSummaryRowsPresentWithAllYes(page, Amounts(420, -1000, 1420), true)
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistory(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, -1580)
            validateSummaryRowsPresentWithAllYes(page, Amounts(420, 2000, -1580), true)
          }
        }
      }

      "all answers equal yes with litres were submitted, the balance brought forward is 0 and the user is a small producer" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(true))))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryNone(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 0)
            validateSummaryRowsPresentWithAllYes(page, Amounts(0, 0, 0), true)
          }
        }
      }

      "all answers equal yes with litres were submitted, the balance brought forward is positive and the user is a small producer" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(true))))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistoryInCredit(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, 1000)
            validateSummaryRowsPresentWithAllYes(page, Amounts(0, -1000, 1000), true)
          }
        }
      }

      "all answers equal yes with litres were submitted, the balance brought forward is negative and the user is a small producer" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(true))))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
          .sdilBackend.balanceHistory(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/check-your-answers")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            validateAmountToPaySubHeader(page, -2000)
            validateSummaryRowsPresentWithAllYes(page, Amounts(0, 2000, -2000),true)
          }
        }
      }
    }
  }

  "POST /check-your-answers (balanceAllEnabled)" should {
    "send the return and return variation and redirect to returnSent" when {
      "a nil return was submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[OptSmallProducer](sdilNumber,
          SDILSessionKeys.smallProducerForPeriod(requestReturnPeriod), OptSmallProducer(Some(false))))
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryNone))

        setUpData(defaultNilReturnUserAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)

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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryCredit))

        setUpData(defaultNilReturnUserAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistory))

        setUpData(defaultNilReturnUserAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryNone))
        setUpData(checkYourAnswersFullAnswersAllNo)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryCredit))
        setUpData(checkYourAnswersFullAnswersAllNo)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistory))
        setUpData(checkYourAnswersFullAnswersAllNo)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryNone))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryCredit))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistory))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryNone))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistoryCredit))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
        await(sdilSessionCache.save[Seq[FinancialLineItem]](sdilNumber, SDILSessionKeys.balanceHistory(false), balanceHistory))
        setUpData(checkYourAnswersFullAnswers)
        given.commonPreconditionChangeSubscription(aSubscription)
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
}