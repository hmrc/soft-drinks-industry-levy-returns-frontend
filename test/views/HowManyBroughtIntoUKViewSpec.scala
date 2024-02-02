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
import forms.HowManyBroughtIntoUkFormProvider
import models.{ CheckMode, LitresInBands, NormalMode }
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.HowManyBroughtIntoUkView

class HowManyBroughtIntoUKViewSpec extends ViewSpecHelper with LitresSpecHelper {

  val howManyBroughtIntoTheUKView: HowManyBroughtIntoUkView = application.injector.instanceOf[HowManyBroughtIntoUkView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val formProvider = new HowManyBroughtIntoUkFormProvider()
  val form: Form[LitresInBands] = formProvider.apply()
  val formWithHighAndLowBands: Form[LitresInBands] = form.fill(litresInBands)
  val formWithLowBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(highBand = 0))
  val formWithHighBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(lowBand = 0))
  val formEmpty: Form[LitresInBands] = form.bind(Map("lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[LitresInBands] = form.bind(Map("lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

  "How Many Brought Into UK View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(formEmpty, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyBroughtIntoTheUKView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        val title =
          "How many litres of liable drinks have you brought into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"

        "should have the expected title" in {
          document.title() mustBe title
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe
            "How many litres of liable drinks have you brought into the UK from anywhere outside of the UK?"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).first().text() mustBe Messages("Do not include liable drinks from small producers or your own brands if you are a registered small producer.")
        }

        val expectedDetails = Map(Messages("What is a small producer?") -> Messages("A business is a small producer if it: has had less than 1 million litres of its own brands of liable drinks packaged globally in the past 12 months will not have more than 1 million litres of its own brands of liable drinks packaged globally in the next 30 days"))

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)
        testButton(document)
        testDetails(document, expectedDetails)
        testAction(document, controllers.routes.HowManyBroughtIntoUkController.onSubmit(mode).url)

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
