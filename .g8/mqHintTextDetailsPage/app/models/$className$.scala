package models

import play.api.libs.json._

case class $className$ ($field1Name$: Double, $field2Name$: Double)

object $className$ {
  implicit val format = Json.format[$className$]
}
