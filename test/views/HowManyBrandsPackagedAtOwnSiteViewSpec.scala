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
import forms.BrandsPackagedAtOwnSitesFormProvider
import models.{ CheckMode, LitresInBands, NormalMode }
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.helpers.{ LitresSpecHelper, ViewSpecHelper }
import views.html.BrandsPackagedAtOwnSitesView

class HowManyBrandsPackagedAtOwnSiteViewSpec extends ViewSpecHelper with LitresSpecHelper {

  val howManyBrandsPackagedAtOwnSiteView: BrandsPackagedAtOwnSitesView = application.injector.instanceOf[BrandsPackagedAtOwnSitesView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val formProvider = new BrandsPackagedAtOwnSitesFormProvider()
  val form: Form[LitresInBands] = formProvider.apply()
  val formWithHighAndLowBands: Form[LitresInBands] = form.fill(litresInBands)
  val formWithLowBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(highBand = 0))
  val formWithHighBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(lowBand = 0))
  val formEmpty: Form[LitresInBands] = form.bind(Map("lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[LitresInBands] = form.bind(Map("lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

  "Brands Packaged at Own Sites View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(formEmpty, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyBrandsPackagedAtOwnSiteView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustBe "How many litres of liable drinks have you packaged at UK sites you operate? - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe "How many litres of liable drinks have you packaged at UK sites you operate?"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).text() mustBe "This includes brands you own or have the rights to manufacture."
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)
        testButton(document)
        testAction(document, controllers.routes.BrandsPackagedAtOwnSitesController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: " + "How many litres of liable drinks have you packaged at UK sites you operate? - Soft Drinks Industry Levy - GOV.UK"
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
