/*
 * Copyright 2023 HM Revenue & Customs
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

package views

import config.FrontendAppConfig
import forms.HowManyAsAContractPackerFormProvider
import models.{ CheckMode, LitresInBands, NormalMode }
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.HowManyBroughtIntoTheUKFromSmallProducersView

class HowManyBroughtIntoUkFromSmallProducersViewSpec extends ViewSpecHelper with LitresSpecHelper {

  val howManyBroughtIntoTheUKFromSmallProducersView: HowManyBroughtIntoTheUKFromSmallProducersView =
    application.injector.instanceOf[HowManyBroughtIntoTheUKFromSmallProducersView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val formProvider = new HowManyAsAContractPackerFormProvider()
  val form: Form[LitresInBands] = formProvider.apply()
  val formWithHighAndLowBands: Form[LitresInBands] = form.fill(litresInBands)
  val formWithLowBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(highBand = 0))
  val formWithHighBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(lowBand = 0))
  val formEmpty: Form[LitresInBands] = form.bind(Map("lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[LitresInBands] = form.bind(Map("lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

  "How Many Packaged As Contract Packer View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(formEmpty, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyBroughtIntoTheUKFromSmallProducersView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        val title =
          "How many litres of liable drinks have you brought into the UK from small producers? - Soft Drinks Industry Levy - GOV.UK"

        "should have the expected title" in {
          document.title() mustBe title
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe
            "How many litres of liable drinks have you brought into the UK from small producers?"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).first().text() mustBe Messages("Include your own brands of liable drinks produced outside of the UK.")
        }

        val expectedDetails = Map(
          "Liable drinks from small producers" -> "If you are a registered small producer and you bring your own brand of liable drinks into the UK, you still need to report them but you will not pay the levy on them. If you bring liable drinks into the UK from someone else who would be considered a small producer, you need to get evidence of the: contact details, EU VAT number (if they have one) and website of the business amount of litres of liable drinks packaged globally for brands the business owns in the past 12 months signature of someone from the business, their position and the date of the signature")

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)
        testButton(document)
        testDetails(document, expectedDetails)
        testAction(document, controllers.routes.HowManyBroughtIntoTheUKFromSmallProducersController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: " + title
          testEmptyFormErrors(documentFormErrorsEmpty, errorTitle)
          testNoNumericFormErrors(documentFormErrorsNoneNumeric, errorTitle)
          testNegativeFormErrors(documentFormErrorsNegative, errorTitle)
          testDecimalFormErrors(documentFormErrorsNotWhole, errorTitle)
          testOutOfMaxValFormErrors(documentFormErrorsOutOfRange, errorTitle)
        }

        testBackLink(document)
        validateTimeoutDialog(document)
        validateTechnicalHelpLinkPresent(document)
        validateAccessibilityStatementLinkPresent(document)
      }
    }
  }
}
