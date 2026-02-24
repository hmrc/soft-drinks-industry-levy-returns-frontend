/*
 * Copyright 2026 HM Revenue & Customs
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

package config

import play.api.Configuration

import java.time.LocalDate
import java.time.format.DateTimeParseException

case class BandRates(lowerBandCostPerLitre: BigDecimal, higherBandCostPerLitre: BigDecimal)

final case class BandRatePeriod(
                                 startDate: LocalDate,
                                 endDate: Option[LocalDate],
                                 rates: BandRates
                               )

final class SdilBandRatesConfig(configuration: Configuration) {

  private val periods: Seq[BandRatePeriod] = {
    val cfgs = configuration.get[Seq[Configuration]]("sdil.bandRates")

    val parsed = cfgs.map { c =>
      val start = readLocalDate(c, "startDate")
      val end = readOptionalLocalDate(c, "endDate")

      val lower = BigDecimal(c.get[String]("lowerBandCostPerLitre"))
      val higher = BigDecimal(c.get[String]("higherBandCostPerLitre"))

      val period = BandRatePeriod(
        startDate = start,
        endDate = end,
        rates = BandRates(lower, higher)
      )

      // Validate date range if endDate is present
      end.foreach { e =>
        require(!e.isBefore(start), s"sdil.bandRates endDate $e is before startDate $start")
      }

      period
    }.sortBy(_.startDate)

    parsed
  }

  /**
   * Find rates for a given date:
   * startDate <= date AND (endDate absent OR date <= endDate)
   */
  def bandRatesFor(date: LocalDate): BandRates =
    periods
      .find(p => !date.isBefore(p.startDate) && p.endDate.forall(e => !date.isAfter(e)))
      .map(_.rates)
      .getOrElse {
        throw new IllegalArgumentException(
          s"No SDIL band rates configured for date $date. Check sdil.bandRates start/end dates."
        )
      }

  private def readLocalDate(c: Configuration, key: String): LocalDate = {
    val raw = c.get[String](key)
    try LocalDate.parse(raw)
    catch {
      case _: DateTimeParseException =>
        throw new IllegalArgumentException(s"Invalid date for sdil.bandRates.$key: '$raw' (expected yyyy-MM-dd)")
    }
  }

  private def readOptionalLocalDate(c: Configuration, key: String): Option[LocalDate] =
    c.getOptional[String](key).map { raw =>
      try LocalDate.parse(raw)
      catch {
        case _: DateTimeParseException =>
          throw new IllegalArgumentException(s"Invalid date for sdil.bandRates.$key: '$raw' (expected yyyy-MM-dd)")
      }
    }
}