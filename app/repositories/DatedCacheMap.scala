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

import models.ModelEncryption
import play.api.libs.functional.syntax._
import play.api.libs.json._
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time.Instant

case class DatedCacheMap(
  id: String,
  data: Map[String, JsValue],
  lastUpdated: Instant = Instant.now())

object DatedCacheMap {
  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    def reads(implicit encryption: Encryption): Reads[DatedCacheMap] = {
      (
        (__ \ "id").read[String] and
        (__ \ "data").read[Map[String, EncryptedValue]] and
        (__ \ "lastUpdated").read[Instant])(ModelEncryption.decryptDatedCacheMap _)
    }

    def writes(implicit encryption: Encryption): OWrites[DatedCacheMap] = new OWrites[DatedCacheMap] {
      override def writes(datedCacheMap: DatedCacheMap): JsObject = {
        val encryptedValue: (String, Map[String, EncryptedValue], Instant) = {
          ModelEncryption.encryptDatedCacheMap(datedCacheMap)
        }
        Json.obj(
          "id" -> encryptedValue._1,
          "data" -> encryptedValue._2,
          "lastUpdated" -> encryptedValue._3)
      }
    }

    def formats(implicit encryption: Encryption): OFormat[DatedCacheMap] = OFormat(reads, writes)

  }
  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}

