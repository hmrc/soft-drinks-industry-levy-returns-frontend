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
import play.api.libs.functional.syntax._
import play.api.libs.json._
import queries.{Gettable, Settable}
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

case class UserAnswers(
                        id: String,
                        data: JsObject = Json.obj(),
                        smallProducerList: List[SmallProducer] = List.empty,
                        packagingSiteList: Map[String, Site] = Map.empty,
                        warehouseList: Map[String, Warehouse] = Map.empty,
                        submitted:Boolean = false,
                        lastUpdated: Instant = Instant.now
                      ) {


    def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

    def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
    d =>
    val updatedAnswers = copy(data = d)
    page.cleanup(Some(value), updatedAnswers)
    }
  }

    def setAndRemoveLitresIfReq(page: Settable[Boolean], litresPage: Settable[LitresInBands], value: Boolean)
                               (implicit writes: Writes[Boolean]): Try[UserAnswers] = {
      set(page, value).map { updatedAnswers =>
        if (value) {
          updatedAnswers
        } else {
          removeLitres(litresPage, updatedAnswers.data)
        }
      }
    }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {
    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
      Success(jsValue)
      case JsError(_) =>
      Success(data)
    }

    updatedData.flatMap {
      d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }

private def removeLitres(page: Settable[LitresInBands], updatedData: JsObject): UserAnswers = {
  val dataWithNoLitres = updatedData.removeObject(page.path) match {
    case JsSuccess(jsValue, _) =>
      jsValue
    case JsError(_) =>
      updatedData
  }

  val updatedAnswers = copy(data = dataWithNoLitres)
    page.cleanup(None, updatedAnswers).get
}

object UserAnswers {

  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue]  = CryptoFormats.encryptedValueFormat
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._

  def reads()(implicit encryption: Encryption): Reads[UserAnswers] = {
    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[EncryptedValue] and
        (__ \ "smallProducerList").read[EncryptedValue] and
        (__ \ "packagingSiteList").read[Map[String, EncryptedValue]] and
        (__ \ "warehouseList").read[Map[String, EncryptedValue]] and
        (__ \ "submitted").read[Boolean] and
        (__ \ "lastUpdated").read[Instant]
      )(ModelEncryption.decryptUserAnswers _)
  }

  def writes(implicit encryption: Encryption): OWrites[UserAnswers] = new OWrites[UserAnswers] {
    override def writes(userAnswers: UserAnswers): JsObject = {
      val encryptedValue: (String, EncryptedValue, EncryptedValue, Map[String, EncryptedValue], Map[String, EncryptedValue], Boolean, Instant) = {
        ModelEncryption.encryptUserAnswers(userAnswers)
      }
      Json.obj(
        "id" -> encryptedValue._1,
        "data" -> encryptedValue._2,
        "smallProducerList" -> encryptedValue._3,
        "packagingSiteList" -> encryptedValue._4,
        "warehouseList" -> encryptedValue._5,
        "submitted" -> encryptedValue._6,
        "lastUpdated" -> encryptedValue._7
      )
    }
  }


  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
  }

   def format(implicit encryption: Encryption): OFormat[UserAnswers] = OFormat(reads, writes)
  }

}
