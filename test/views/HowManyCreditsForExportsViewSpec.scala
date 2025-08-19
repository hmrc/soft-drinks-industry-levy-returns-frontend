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
import forms.HowManyCreditsForExportFormProvider
import models.{ CheckMode, LitresInBands, NormalMode }
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.helpers.{ LitresSpecHelper, ViewSpecHelper }
import views.html.HowManyCreditsForExportView

class HowManyCreditsForExportsViewSpec extends ViewSpecHelper with LitresSpecHelper {

  val howManyCreditsForExportsView: HowManyCreditsForExportView = application.injector.instanceOf[HowManyCreditsForExportView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val formProvider = new HowManyCreditsForExportFormProvider()
  val form: Form[LitresInBands] = formProvider.apply()
  val formWithHighAndLowBands: Form[LitresInBands] = form.fill(litresInBands)
  val formWithLowBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(highBand = 0))
  val formWithHighBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(lowBand = 0))
  val formEmpty: Form[LitresInBands] = form.bind(Map("lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[LitresInBands] = form.bind(Map("lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

  "How Many Credits For Exports View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = howManyCreditsForExportsView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyCreditsForExportsView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyCreditsForExportsView(formEmpty, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyCreditsForExportsView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyCreditsForExportsView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyCreditsForExportsView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyCreditsForExportsView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        val title =
          "How many credits do you want to claim for liable drinks that have been exported? - Soft Drinks Industry Levy - GOV.UK"

        "should have the expected title" in {
          document.title() mustBe title
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe
            "How many credits do you want to claim for liable drinks that have been exported?"
        }

        val expectedDetails = Map(
          "What can I claim a credit for?" -> "You can claim a credit for liable drinks that have been, or you expect to be, exported by you or someone else. You will need to get and keep evidence of details such as the: brand of the liable drinks supplier or consigner disposed of as waste customer and destination the liable drinks are supplied to method of delivery If you do not have the evidence by the end of the quarter after you reported the liable drinks as exported, you must add the levy credit back in your next return.")

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)
        testButton(document)
        testDetails(document, expectedDetails)
        testAction(document, controllers.routes.HowManyCreditsForExportController.onSubmit(mode).url)

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
        validateCreditClaim(document)
      }
    }
  }
}
