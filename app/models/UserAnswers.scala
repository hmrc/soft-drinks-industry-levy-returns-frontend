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
import queries.{Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

case class UserAnswers(
                        id: String,
                        data: JsObject = Json.obj(),
                        smallProducerList: List[SmallProducer] = List.empty,
                        packagingSiteList: Map[String, Site] = Map.empty,
                        warehouseList: Map[String, Warehouse] = Map.empty,
                        submitted:Boolean = true,
                        lastUpdated: Instant = Instant.now
                      ) {

    def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

    def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    println(Console.YELLOW + "getting to set 2222222222222222222222222 " + Console.WHITE)
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
    println(Console.YELLOW + "getting to setAndRemoveLitres 1111111111111111" + Console.WHITE)
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
}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "smallProducerList").read[List[SmallProducer]] and
        (__ \ "packagingSiteList").read[Map[String, Site]] and
        (__ \ "warehouseList").read[Map[String, Warehouse]] and
        (__ \ "submitted").read[Boolean] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      ) (UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "smallProducerList").write[List[SmallProducer]] and
        (__ \ "packagingSiteList").write[Map[String, Site]] and
        (__ \ "warehouseList").write[Map[String, Warehouse]] and
        (__ \ "submitted").write[Boolean] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      ) (unlift(UserAnswers.unapply))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
