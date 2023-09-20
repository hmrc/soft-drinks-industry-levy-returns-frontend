package controllers

import cats.implicits.catsSyntaxSemigroup
import controllers.testSupport.ReturnSummaryValidationHelper
import models.backend.Site
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod, ReturnsVariation, SdilReturn, SmallProducer}
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import utilitlies.CurrencyFormatter

trait CheckYourAnswersPageValidationHelper extends ReturnSummaryValidationHelper {
  def validateAmountToPaySubHeader(page: Document, total: BigDecimal) = {
    val subHeader = page.getElementById("cya-inset-sub-header")
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

  def emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))
  def populatedReturn: SdilReturn = SdilReturn((lowBand, highBand), (lowBand, highBand), List(SmallProducer(producerName.get, refNumber, (lowBand, highBand))),
    (lowBand, highBand), (lowBand, highBand), (lowBand, highBand), (lowBand, highBand))

  def emptyVariation = ReturnsVariation(
    orgName = aSubscription.orgName,
    ppobAddress = aSubscription.address,
    importer = (false, (0L, 0L)),
    packer = (false, (0L, 0L)),
    warehouses = List.empty,
    packingSites = List.empty,
    phoneNumber = aSubscription.contact.phoneNumber,
    email = aSubscription.contact.email,
    taxEstimation = BigDecimal(0)
  )

  def populatedVariation = ReturnsVariation(
    orgName = aSubscription.orgName,
    ppobAddress = aSubscription.address,
    importer = (true, ((populatedReturn.totalImported)).combineN(4)),
    packer = (true, ((populatedReturn.totalPacked)).combineN(4)),
    warehouses = List(warehouse),
    packingSites = List(PackagingSite1),
    phoneNumber = aSubscription.contact.phoneNumber,
    email = aSubscription.contact.email,
    taxEstimation = BigDecimal(5040)
  )


}
