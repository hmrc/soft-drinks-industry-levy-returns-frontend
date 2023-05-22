package repositories

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{Format, JsObject, Json}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

class SDILSessionCacheRepositoryISpec extends AnyFreeSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with OptionValues with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val encryption: Encryption = app.injector.instanceOf[Encryption]
  implicit val cryptEncryptedValueFormats: Format[EncryptedValue]  = CryptoFormats.encryptedValueFormat

  val repository: SDILSessionCacheRepository = app.injector.instanceOf[SDILSessionCacheRepository]

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

  "indexes" - {
    "are correct" in {
      repository.indexes.toList.toString() mustBe Seq(
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
      ).toString
    }
  }

  ".upsert" - {
    "insert successfully when nothing exists in DB" in {
      val cacheMap = CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang")))
      val timeBeforeTest = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
      await(repository.upsert(cacheMap))
      val updatedRecord = await(repository.collection.find[BsonDocument](BsonDocument()).toFuture()).head

      val timeAfterTest = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)

      val resultParsedToJson = Json.parse(updatedRecord.toJson).as[JsObject]

      val dataDecrypted = {
        val json = (resultParsedToJson \ "data").as[Map[String, EncryptedValue]]
        json.map(data => data._1 -> Json.parse(encryption.crypto.decrypt(data._2, cacheMap.id)))
      }
      val id = (resultParsedToJson \ "id").as[String]
      val lastUpdated = (resultParsedToJson \ "lastUpdated" \ "$date").as[LocalDateTime].toEpochSecond(ZoneOffset.UTC)
      dataDecrypted mustBe cacheMap.data
      id mustBe cacheMap.id

      assert(lastUpdated > timeBeforeTest || lastUpdated == timeBeforeTest)
      assert(lastUpdated < timeAfterTest || lastUpdated == timeAfterTest)
    }
    "upsert a record that already exists successfully" in {
      val cacheMap = CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang")))
      val timeBeforeTest = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)

      await(repository.collection.countDocuments().head()) mustBe 0
      await(repository.upsert(cacheMap))
      await(repository.collection.countDocuments().head()) mustBe 1

      val updatedCacheMap = CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang2")))
      await(repository.upsert(updatedCacheMap))
      val updatedRecord = await(repository.collection.find[BsonDocument](BsonDocument()).toFuture()).head

      val timeAfterTest = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)

      val resultParsedToJson = Json.parse(updatedRecord.toJson).as[JsObject]
      val dataDecrypted = {
        val json = (resultParsedToJson \ "data").as[Map[String, EncryptedValue]]
        json.map(data => data._1 -> Json.parse(encryption.crypto.decrypt(data._2, cacheMap.id)))
      }
      val id = (resultParsedToJson \ "id").as[String]
      val lastUpdated = (resultParsedToJson \ "lastUpdated" \ "$date").as[LocalDateTime].toEpochSecond(ZoneOffset.UTC)

      dataDecrypted mustBe updatedCacheMap.data
      id mustBe updatedCacheMap.id

      assert(lastUpdated > timeBeforeTest || lastUpdated == timeBeforeTest)
      assert(lastUpdated < timeAfterTest || lastUpdated == timeAfterTest)
    }
  }

  ".removeRecord" - {
    "remove a record successfully" in {
      val cacheMap = CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang")))
      await(repository.upsert(cacheMap))
      await(repository.collection.countDocuments().head()) mustBe 1

      await(repository.removeRecord(cacheMap.id))
      await(repository.collection.countDocuments().head()) mustBe 0
    }
  }
  ".get" - {
    "get a record successfully" in {
      val cacheMap = CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang")))
      await(repository.upsert(cacheMap))

      await(repository.collection.countDocuments().head()) mustBe 1

      val result = await(repository.get(cacheMap.id))
      result.get mustBe cacheMap
    }
  }
  ".updateLastUpdated" - {
    "update last updated successfully" in {
      val cacheMap = CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang")))
      await(repository.upsert(cacheMap))
      await(repository.collection.countDocuments().head()) mustBe 1
      val timeBeforeTest = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
      val result = await(repository.updateLastUpdated(cacheMap.id))
      result mustBe true

      val timeAfterTest = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
      val updatedRecord = await(repository.collection.find[BsonDocument](BsonDocument()).toFuture()).head

      val resultParsedToJson = Json.parse(updatedRecord.toJson).as[JsObject]
      val lastUpdated = (resultParsedToJson \ "lastUpdated" \ "$date").as[LocalDateTime].toEpochSecond(ZoneOffset.UTC)

      assert(lastUpdated > timeBeforeTest || lastUpdated == timeBeforeTest)
      assert(lastUpdated < timeAfterTest || lastUpdated == timeAfterTest)
    }
  }

}
