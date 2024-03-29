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

import models.backend.Site
import play.api.libs.json._
import repositories.DatedCacheMap
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue

import java.time.Instant

object ModelEncryption {
  def encryptDatedCacheMap(datedCacheMap: DatedCacheMap)(implicit encryption: Encryption): (String, Map[String, EncryptedValue], Instant) = {
    (
      datedCacheMap.id,
      datedCacheMap.data.map(item => item._1 -> encryption.crypto.encrypt(item._2.toString(), datedCacheMap.id)),
      datedCacheMap.lastUpdated)
  }
  def decryptDatedCacheMap(
    id: String,
    data: Map[String, EncryptedValue],
    lastUpdated: Instant)(implicit encryption: Encryption): DatedCacheMap = {
    DatedCacheMap(
      id = id,
      data = data.map(item => item._1 -> Json.parse(encryption.crypto.decrypt(item._2, id))),
      lastUpdated = lastUpdated)
  }

  def encryptUserAnswers(userAnswers: UserAnswers)(implicit encryption: Encryption): (String, ReturnPeriod, EncryptedValue, EncryptedValue, Map[String, EncryptedValue], Map[String, EncryptedValue], Boolean, Boolean, Instant) = {
    (
      userAnswers.id,
      userAnswers.returnPeriod,
      encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
      encryption.crypto.encrypt(Json.toJson(userAnswers.smallProducerList).toString(), userAnswers.id),
      userAnswers.packagingSiteList.map(site => site._1 -> encryption.crypto.encrypt(Json.toJson(site._2).toString(), userAnswers.id)),
      userAnswers.warehouseList.map(warehouse => warehouse._1 -> encryption.crypto.encrypt(Json.toJson(warehouse._2).toString(), userAnswers.id)),
      userAnswers.submitted, userAnswers.isNilReturn, userAnswers.lastUpdated)
  }

  def decryptUserAnswers(
    id: String,
    returnPeriod: ReturnPeriod,
    data: EncryptedValue,
    smallProducerList: EncryptedValue,
    packagingSiteList: Map[String, EncryptedValue],
    warehouseList: Map[String, EncryptedValue],
    submitted: Boolean,
    isNilReturn: Boolean,
    lastUpdated: Instant)(implicit encryption: Encryption): UserAnswers = {
    UserAnswers(
      id = id,
      returnPeriod = returnPeriod,
      data = Json.parse(encryption.crypto.decrypt(data, id)).as[JsObject],
      smallProducerList = Json.parse(encryption.crypto.decrypt(smallProducerList, id)).as[List[SmallProducer]],
      packagingSiteList = packagingSiteList.map(site => site._1 -> Json.parse(encryption.crypto.decrypt(site._2, id)).as[Site]),
      warehouseList = warehouseList.map(warehouse => warehouse._1 -> Json.parse(encryption.crypto.decrypt(warehouse._2, id)).as[Site]),
      submitted = submitted,
      isNilReturn = isNilReturn,
      lastUpdated = lastUpdated)
  }

}
