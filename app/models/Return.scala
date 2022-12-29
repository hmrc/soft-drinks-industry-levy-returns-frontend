package models

import java.time.LocalDate

case class ReturnPeriod(year: Int, quarter: Int) {
  require(quarter <= 3 && quarter >= 0)
  require(year >= 2018)
  def start: LocalDate = LocalDate.of(year, quarter * 3 + 1, if (count == 0) 5 else 1)
  def end: LocalDate = next.start.minusDays(1)
  def deadline: LocalDate = end.plusDays(30)
  def next: ReturnPeriod = ReturnPeriod(count + 1)
  def previous: ReturnPeriod = ReturnPeriod(count - 1)
  def count: Int = year * 4 + quarter - 2018 * 4 - 1
}

object ReturnPeriod {
  def apply(o: Int): ReturnPeriod = {
    val i = o + 1
    ReturnPeriod(2018 + i / 4, i % 4)
  }

  def apply(date: LocalDate): ReturnPeriod = ReturnPeriod(date.getYear, quarter(date))
  def quarter(date: LocalDate): Int = { date.getMonthValue - 1 } / 3

}