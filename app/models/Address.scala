package models

import models.backend.UkAddress
import play.api.libs.json.{Json, OFormat}

case class Address(line1: String, line2: String, line3: String, line4: String, postcode: String) {
  def nonEmptyLines: Seq[String] = Seq(line1, line2, line3, line4, postcode).filter(_.nonEmpty)
}

object Address {
  def fromString(s: String): Address = {
    def getLine(n: Int) = lines.init.lift(n).getOrElse("")
    lazy val lines = s.split(",")

    Address(getLine(0), getLine(1), getLine(2), getLine(3), lines.lastOption.getOrElse(""))
  }

  def fromUkAddress(address: UkAddress): Address = {
    def getLine(n: Int) = address.lines.lift(n).getOrElse("")

    Address(getLine(0), getLine(1), getLine(2), getLine(3), address.postCode)
  }

  implicit val address: OFormat[Address] = Json.format[Address]
}