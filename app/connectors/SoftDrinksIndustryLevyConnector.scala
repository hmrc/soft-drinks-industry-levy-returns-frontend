/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import models.retrieved.{OptRetrievedSubscription, OptSmallProducer, RetrievedSubscription}
import models.{FinancialLineItem, LevyCalculation, LevyCalculationRequest, ReturnPeriod, ReturnsVariation, SdilReturn}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import repositories.{SDILSessionCache, SDILSessionKeys}
import util.GenericLogger
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SoftDrinksIndustryLevyConnector @Inject() (
  val http:          HttpClientV2,
  frontendAppConfig: FrontendAppConfig,
  sdilSessionCache:  SDILSessionCache,
  genericLogger:     GenericLogger
)(implicit
  ec: ExecutionContext
) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private val logger = genericLogger.logger

  private class RawHttpReads extends HttpReads[HttpResponse]:
    override def read(method: String, url: String, response: HttpResponse): HttpResponse = response

  private val rawHttpReads = new RawHttpReads

  private def outboundHeaderCarrier(hc: HeaderCarrier): HeaderCarrier =
    HeaderCarrier(
      requestId = hc.requestId,
      sessionId = hc.sessionId
    )

  private def sdilContext(
    path:      String,
    status:    Option[Int] = None,
    startTime: Option[Long] = None
  ): String =
    Seq(
      Some(s"path=$path"),
      status.map(st => s"status=$st"),
      startTime.map(st => s"durationMs=${System.currentTimeMillis() - st}")
    ).flatten.mkString(" ")

  private def executeGet[A](operation: String, path: String)(using hc: HeaderCarrier, rds: HttpReads[A]): Future[A] =
    val urlString = s"$sdilUrl$path"
    val startTime = System.currentTimeMillis()
    logger.info(
      s"SDIL $operation request ${sdilContext(path, startTime = Some(startTime))}"
    )
    http
      .get(url"$urlString")(using outboundHeaderCarrier(hc))
      .execute[HttpResponse](using rawHttpReads, ec)
      .map { response =>
        logger.info(
          s"SDIL $operation response ${sdilContext(path, status = Some(response.status), startTime = Some(startTime))}"
        )
        rds.read("GET", urlString, response)
      }
      .recoverWith { case NonFatal(e) =>
        logger.error(
          s"SDIL $operation failure ${sdilContext(path, startTime = Some(startTime))} error=${e.getMessage}",
          e
        )
        Future.failed(e)
      }

  private def executePost[A](operation: String, path: String, body: play.api.libs.json.JsValue)(using
    hc:  HeaderCarrier,
    rds: HttpReads[A]
  ): Future[A] =
    val urlString = s"$sdilUrl$path"
    val startTime = System.currentTimeMillis()
    logger.info(
      s"SDIL $operation request ${sdilContext(path, startTime = Some(startTime))}"
    )
    http
      .post(url"$urlString")(using outboundHeaderCarrier(hc))
      .withBody(body)
      .execute[HttpResponse](using rawHttpReads, ec)
      .map { response =>
        logger.info(
          s"SDIL $operation response ${sdilContext(path, status = Some(response.status), startTime = Some(startTime))}"
        )
        rds.read("POST", urlString, response)
      }
      .recoverWith { case NonFatal(e) =>
        logger.error(
          s"SDIL $operation failure ${sdilContext(path, startTime = Some(startTime))} error=${e.getMessage}",
          e
        )
        Future.failed(e)
      }

  def retrieveSubscription(identifierValue: String, identifierType: String)(implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] =
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some(optSubscription) => Future.successful(optSubscription.optRetrievedSubscription)
      case None                  =>
        executeGet[Option[RetrievedSubscription]](
          operation = "retrieveSubscription",
          path = s"/subscription/$identifierType/$identifierValue"
        )
          .flatMap { optRetrievedSubscription =>
            sdilSessionCache
              .save[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map(_ => optRetrievedSubscription)
          }
    }
  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sdilSessionCache.fetchEntry[OptSmallProducer](sdilRef, SDILSessionKeys.smallProducerForPeriod(period)).flatMap {
      case Some(optSP) => Future.successful(optSP.optSmallProducer)
      case None        =>
        executeGet[Option[Boolean]](
          operation = "checkSmallProducerStatus",
          path = s"/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"
        )
          .flatMap { optSP =>
            sdilSessionCache
              .save(sdilRef, SDILSessionKeys.smallProducerForPeriod(period), OptSmallProducer(optSP))
              .map(_ => optSP)
          }
    }

  def getPendingReturnPeriods(utr: String)(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] =
    executeGet[List[ReturnPeriod]](
      operation = "getPendingReturnPeriods",
      path = s"/returns/$utr/pending"
    )

  def balance(sdilRef: String, withAssessment: Boolean)(implicit hc: HeaderCarrier): Future[BigDecimal] =
    sdilSessionCache.fetchEntry[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment)).flatMap {
      case Some(b) => Future.successful(b)
      case None    =>
        executeGet[BigDecimal](
          operation = "balance",
          path = s"/balance/$sdilRef/$withAssessment"
        )
          .flatMap { b =>
            sdilSessionCache
              .save[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment), b)
              .map(_ => b)
          }
    }

  def balanceHistory(sdilRef: String, withAssessment: Boolean)(implicit hc: HeaderCarrier): Future[List[FinancialLineItem]] = {
    import FinancialLineItem.formatter
    sdilSessionCache.fetchEntry[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment)).flatMap {
      case Some(fli) => Future.successful(fli)
      case None      =>
        executeGet[List[FinancialLineItem]](
          operation = "balanceHistory",
          path = s"/balance/$sdilRef/history/all/$withAssessment"
        )
          .flatMap { fli =>
            sdilSessionCache
              .save[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment), fli)
              .map(_ => fli)
          }
    }
  }

  def returns_update(utr: String, period: ReturnPeriod, sdilReturn: SdilReturn)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    executePost[HttpResponse](
      operation = "returns_update",
      path = s"/returns/$utr/year/${period.year}/quarter/${period.quarter}",
      body = Json.toJson(sdilReturn)
    )(using hc, rawHttpReads)
      .map { response =>
        Some(response.status)
      }

  def calculateLevy(sdilRef: String, lowLitres: Long, highLitres: Long, returnPeriod: ReturnPeriod)(implicit
    hc: HeaderCarrier
  ): Future[LevyCalculation] = {
    val cacheKey = SDILSessionKeys.levyCalculation(lowLitres, highLitres, returnPeriod)
    sdilSessionCache.fetchEntry[LevyCalculation](sdilRef, cacheKey).flatMap {
      case Some(calc) => Future.successful(calc)
      case None       =>
        http
          .post(url"$sdilUrl/levy/calculate")
          .withBody(Json.toJson(LevyCalculationRequest(lowLitres, highLitres, returnPeriod)))
          .execute[LevyCalculation]
          .flatMap { calc =>
            sdilSessionCache
              .save[LevyCalculation](sdilRef, cacheKey, calc)
              .map(_ => calc)
          }
    }
  }

  def returns_variation(sdilRef: String, variation: ReturnsVariation)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    executePost[HttpResponse](
      operation = "returns_variation",
      path = s"/returns/variation/sdil/$sdilRef",
      body = Json.toJson(variation)
    )(using hc, rawHttpReads)
      .map { response =>
        Some(response.status)
      }

}
