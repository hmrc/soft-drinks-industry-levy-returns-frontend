package models

import play.api.libs.json._

case class BrandsPackagedAtOwnSites (lowBandLitres: Long, highBandLitres: Long)

object BrandsPackagedAtOwnSites {
  implicit val format = Json.format[BrandsPackagedAtOwnSites]
}
