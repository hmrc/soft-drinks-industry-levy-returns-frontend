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
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes, ReplaceOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{LocalDateTime, ZoneId}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SDILSessionCacheRepository @Inject()(mongoComponent: MongoComponent,
                                           appConfig: FrontendAppConfig
                                         )(implicit ec: ExecutionContext) extends
    PlayMongoRepository[DatedCacheMap](
      mongoComponent = mongoComponent,
      collectionName = "sdil-session-cache",
      domainFormat = DatedCacheMap.formats,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions()
            .name("sdil-session-cache-expiry")
            .expireAfter(
              3600,
              TimeUnit.SECONDS
            )
        ),
        IndexModel(
          Indexes.ascending("id"),
          IndexOptions()
            .name("sdilIdentifierIndex")
            .sparse(true)
            .unique(true)
            .background(true)
        )
      ),
      replaceIndexes = false
    ){

  def upsert(cm: CacheMap): Future[Boolean] = {
    val cmUpdated = DatedCacheMap(cm.id, cm.data)
    val options = ReplaceOptions().upsert(true)
    collection
      .replaceOne(equal("id", cm.id), cmUpdated, options)
      .toFuture()
      .map { result =>
        result.wasAcknowledged()
      }
  }

  def removeRecord(id: String): Future[Boolean] = {
    collection.deleteOne(equal("id", id)).toFuture().map(_.getDeletedCount > 0)
  }

  def get(id: String): Future[Option[CacheMap]] = {
    collection.find(equal("id", id)).headOption().map { datedCacheMap =>
      datedCacheMap.map { value: DatedCacheMap =>
        value.toCacheMap
      }
    }
  }

  def updateLastUpdated(id: String): Future[Boolean] = {
    collection
      .updateOne(
        equal("id", id),
        set("lastUpdated", LocalDateTime.now(ZoneId.of("UTC")))
      )
      .toFuture()
      .map { result =>
        result.wasAcknowledged()
      }
  }

}
