package models

import play.api.libs.json._

case class $className$ ($field1Name$: Long, $field2Name$: Long)

object $className$ {
  implicit val format = Json.format[$className$]
}
