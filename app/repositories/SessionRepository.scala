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

package repositories

import config.FrontendAppConfig
import errors.{ ReturnsErrors, SessionDatabaseInsertError }
import models.UserAnswers
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import services.Encryption
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{ Clock, Instant }
import java.util.concurrent.TimeUnit
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import org.mongodb.scala.SingleObservableFuture

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  clock: Clock)(implicit ec: ExecutionContext, encryption: Encryption)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongoComponent,
    domainFormat = UserAnswers.MongoFormats.format,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(appConfig.cacheTtl.toLong, TimeUnit.SECONDS)))) {

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def keepAlive(id: String): Future[Either[ReturnsErrors, Boolean]] = {
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("lastUpdated", Instant.now(clock)))
      .toFuture()
      .map(_ => Right(true))
      .recover {
        case _ => Left(SessionDatabaseInsertError)
      }
  }

  def get(id: String): Future[Option[UserAnswers]] =
    keepAlive(id).flatMap {
      _ =>
        collection
          .find(byId(id))
          .headOption()
    }

  def set(answers: UserAnswers): Future[Either[ReturnsErrors, Boolean]] = {

    val updatedAnswers = answers copy (lastUpdated = Instant.now(clock))

    collection
      .replaceOne(
        filter = byId(updatedAnswers.id),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(true))
      .toFuture()
      .map(_ => Right(true))
      .recover {
        case _ => Left(SessionDatabaseInsertError)
      }
  }

  def clear(id: String): Future[Boolean] =
    collection
      .deleteOne(byId(id))
      .toFuture()
      .map(_ => true)
}
