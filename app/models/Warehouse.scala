package models

import models.backend.Site
import play.api.libs.json.{Format, Json}
import sdil.models.Address

case class Warehouse(
                      tradingName: String,
                      address: Address
                    ) {
  def nonEmptyLines: Seq[String] =
    Seq(tradingName, address.line1, address.line2, address.line3, address.line4, address.postcode).filter(_.nonEmpty)
}

object Warehouse {

  def fromSite(site: Site): Warehouse =
    Warehouse(site.tradingName.getOrElse(""), Address.fromUkAddress(site.address))

  implicit val format: Format[Warehouse] = Json.format[Warehouse]
}