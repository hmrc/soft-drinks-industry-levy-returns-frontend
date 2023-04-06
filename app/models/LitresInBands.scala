package models

import play.api.libs.json.Json

case class LitresInBands(lowBand: Long, highBand: Long)

object LitresInBands {
  implicit val format = Json.format[LitresInBands]
}
