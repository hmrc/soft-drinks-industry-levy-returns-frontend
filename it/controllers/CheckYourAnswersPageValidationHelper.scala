package controllers

import controllers.testSupport.ReturnSummaryValidationHelper
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod}
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import utilitlies.CurrencyFormatter

trait CheckYourAnswersPageValidationHelper extends ReturnSummaryValidationHelper {
  def validateAmountToPaySubHeader(page: Document, total: BigDecimal) = {
    val subHeader = page.getElementById("cya-sub-header")
    if(total > 0) {
      subHeader.text() mustBe Messages("youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total))
    } else if(total < 0) {
      subHeader.text() mustBe Messages("yourSoftDrinksLevyAccountsWillBeCredited", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total * -1))
    } else {
      subHeader mustBe null
    }
  }

  def balance: BigDecimal = BigDecimal(10000)
  def balanceNegative: BigDecimal = BigDecimal(-1000)

  def balanceZero: BigDecimal = BigDecimal(0)

  def balanceHistory: Seq[FinancialLineItem] = List(
    ReturnCharge(requestReturnPeriod, BigDecimal(1000)),
    ReturnCharge(ReturnPeriod(2018, 2), BigDecimal(1000)))

  def balanceHistoryCredit: Seq[FinancialLineItem] = List(
    ReturnCharge(ReturnPeriod(2018, 2), BigDecimal(-1000)))

  def balanceHistoryNone: Seq[FinancialLineItem] = List(
    ReturnCharge(ReturnPeriod(2018, 2), BigDecimal(0)))

}
