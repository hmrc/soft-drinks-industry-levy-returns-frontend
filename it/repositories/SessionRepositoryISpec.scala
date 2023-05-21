package repositories

import models.backend.{Site, UkAddress}
import models.{SmallProducer, UserAnswers, Warehouse}
import org.mongodb.scala.bson.BsonDocument
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

import java.time.{Instant, LocalDate}
import java.util.concurrent.TimeUnit

class SessionRepositoryISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with OptionValues with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val repository: SessionRepository = app.injector.instanceOf[SessionRepository]
  val encryption: Encryption = app.injector.instanceOf[Encryption]
  implicit val cryptEncryptedValueFormats: Format[EncryptedValue]  = CryptoFormats.encryptedValueFormat

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

  "indexes" - {
    "are correct" in {
      repository.indexes.toList.toString() mustBe List(IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(900, TimeUnit.SECONDS)
      )).toString()
    }
  }

  ".set" - {
    "must set the last updated time on the supplied user answers to `now`, and save them" in {
      val userAnswersBefore = UserAnswers("id", Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))
      val timeBeforeTest = Instant.now()
      val setResult     = await(repository.set(userAnswersBefore))
      val updatedRecord = await(repository.get(userAnswersBefore.id)).get
      lazy val timeAfterTest = Instant.now()

      setResult mustEqual true
      assert(updatedRecord.lastUpdated.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeBeforeTest.toEpochMilli)
      assert(updatedRecord.lastUpdated.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeAfterTest.toEpochMilli)

      updatedRecord.id mustBe userAnswersBefore.id
      updatedRecord.submitted mustBe userAnswersBefore.submitted
      updatedRecord.data mustBe userAnswersBefore.data
      updatedRecord.smallProducerList mustBe userAnswersBefore.smallProducerList
      updatedRecord.warehouseList mustBe userAnswersBefore.warehouseList
      updatedRecord.packagingSiteList mustBe userAnswersBefore.packagingSiteList
      }

    "correctly encrypt the records data" in {
      val userAnswersBefore = UserAnswers("id",
        Json.obj("foo" -> "bar"),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Warehouse(Some("foo"),UkAddress(List("foo"),"foo", Some("foo")))),
        false,
        Instant.ofEpochSecond(1))
      val setResult = await(repository.set(userAnswersBefore))
      setResult mustBe true
      val updatedRecord = await(repository.collection.find[BsonDocument](BsonDocument()).toFuture()).head
      val resultParsedToJson = Json.parse(updatedRecord.toJson).as[JsObject]
      val dataDecrypted = {
        Json.parse(encryption.crypto.decrypt((resultParsedToJson \ "data").as[EncryptedValue],userAnswersBefore.id)).as[JsObject]
      }
      val smallProducerListDecrypted = {
        Json.parse(encryption.crypto.decrypt((resultParsedToJson \ "smallProducerList").as[EncryptedValue],userAnswersBefore.id)).as[List[SmallProducer]]
      }
      val packagingSiteListDecrypted = {
        val json = (resultParsedToJson \ "packagingSiteList").as[Map[String, EncryptedValue]]
        json.map(site => site._1 -> Json.parse(encryption.crypto.decrypt(site._2, userAnswersBefore.id)).as[Site])
      }
      val warehouseListDecrypted = {
        val json = (resultParsedToJson \ "warehouseList").as[Map[String, EncryptedValue]]
        json.map(warehouse => warehouse._1 -> Json.parse(encryption.crypto.decrypt(warehouse._2, userAnswersBefore.id)).as[Warehouse])
      }

      dataDecrypted mustBe userAnswersBefore.data
      smallProducerListDecrypted mustBe userAnswersBefore.smallProducerList
      packagingSiteListDecrypted mustBe userAnswersBefore.packagingSiteList
      warehouseListDecrypted mustBe userAnswersBefore.warehouseList
      (resultParsedToJson \ "submitted").get.as[Boolean] mustBe userAnswersBefore.submitted
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {
        val userAnswersBefore = UserAnswers("id", Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))
        await(repository.set(userAnswersBefore))

        val timeBeforeTest = Instant.now()
        val updatedRecord = await(repository.get(userAnswersBefore.id)).get
        lazy val timeAfterTest = Instant.now()

        assert(updatedRecord.lastUpdated.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeBeforeTest.toEpochMilli)
        assert(updatedRecord.lastUpdated.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeAfterTest.toEpochMilli)

        updatedRecord.id mustBe userAnswersBefore.id
        updatedRecord.submitted mustBe userAnswersBefore.submitted
        updatedRecord.data mustBe userAnswersBefore.data
        updatedRecord.smallProducerList mustBe userAnswersBefore.smallProducerList
        updatedRecord.warehouseList mustBe userAnswersBefore.warehouseList
        updatedRecord.packagingSiteList mustBe userAnswersBefore.packagingSiteList
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {
      val userAnswersBefore = UserAnswers("id", Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))
      repository.set(userAnswersBefore).futureValue

      val result = repository.clear(userAnswersBefore.id).futureValue

      result mustEqual true
      repository.get(userAnswersBefore.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {
        val userAnswersBefore = UserAnswers("id", Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))
        await(repository.set(userAnswersBefore))
        val timeBeforeTest = Instant.now()
        val result = await(repository.keepAlive(userAnswersBefore.id))
        lazy val timeAfterTest = Instant.now()
        result mustEqual true
        val updatedRecord = await(repository.collection.find(BsonDocument()).headOption()).get

        assert(updatedRecord.lastUpdated.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeBeforeTest.toEpochMilli)
        assert(updatedRecord.lastUpdated.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeAfterTest.toEpochMilli)

        updatedRecord.id mustBe userAnswersBefore.id
        updatedRecord.submitted mustBe userAnswersBefore.submitted
        updatedRecord.data mustBe userAnswersBefore.data
        updatedRecord.smallProducerList mustBe userAnswersBefore.smallProducerList
        updatedRecord.warehouseList mustBe userAnswersBefore.warehouseList
        updatedRecord.packagingSiteList mustBe userAnswersBefore.packagingSiteList
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        await(repository.keepAlive("id that does not exist")) mustEqual true
      }
    }
  }
}
