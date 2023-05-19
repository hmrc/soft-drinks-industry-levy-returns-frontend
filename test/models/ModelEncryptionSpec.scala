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

package models

import base.SpecBase
import models.backend.{Site, UkAddress}
import play.api.libs.json.{JsObject, Json}
import services.Encryption

import java.time.{Instant, LocalDate}

class ModelEncryptionSpec extends SpecBase {
  val userAnswers = UserAnswers("id",
    Json.obj("foo" -> "bar"),
    List(SmallProducer("foo", "bar", (1,1))),
    Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
    Map("foo" -> Warehouse(Some("foo"),UkAddress(List("foo"),"foo", Some("foo")))),
    false,
    Instant.ofEpochSecond(1))

  implicit val encryption: Encryption = application.injector.instanceOf[Encryption]

  "encrypt" - {
    "should encrypt userAnswers" in {
      val userAnswers = UserAnswers("id",
        Json.obj("foo" -> "bar"),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Warehouse(Some("foo"),UkAddress(List("foo"),"foo", Some("foo")))),
        false,
        Instant.ofEpochSecond(1))

      val result = ModelEncryption.encrypt(userAnswers)
      result._1 mustBe userAnswers.id
      Json.parse(encryption.crypto.decrypt(result._2, userAnswers.id)).as[JsObject] mustBe userAnswers.data
      Json.parse(encryption.crypto.decrypt(result._3, userAnswers.id)).as[List[SmallProducer]] mustBe userAnswers.smallProducerList
      Json.parse(encryption.crypto.decrypt(result._4.head._2, userAnswers.id)).as[Site] mustBe userAnswers.packagingSiteList.head._2
      result._4.head._1 mustBe userAnswers.packagingSiteList.head._1
      Json.parse(encryption.crypto.decrypt(result._5.head._2, userAnswers.id)).as[Warehouse] mustBe userAnswers.warehouseList.head._2
      result._5.head._1 mustBe userAnswers.warehouseList.head._1
      result._6 mustBe userAnswers.submitted
      result._7 mustBe userAnswers.lastUpdated
    }
  }
  "decrypt" - {
    "should decrypt userAnswers in tuple form" in {
      val userAnswers = UserAnswers("id",
        Json.obj("foo" -> "bar"),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Warehouse(Some("foo"),UkAddress(List("foo"),"foo", Some("foo")))),
        false,
        Instant.ofEpochSecond(1))

     val result = ModelEncryption.decrypt(
        userAnswers.id,
        encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
        encryption.crypto.encrypt(Json.toJson(userAnswers.smallProducerList).toString(), userAnswers.id),
        userAnswers.packagingSiteList.map(site => site._1 -> encryption.crypto.encrypt(Json.toJson(site._2).toString(), userAnswers.id)),
        userAnswers.warehouseList.map(warehouse => warehouse._1 -> encryption.crypto.encrypt(Json.toJson(warehouse._2).toString(), userAnswers.id)),
        userAnswers.submitted, userAnswers.lastUpdated
      )
      result mustBe userAnswers

    }
  }
}
