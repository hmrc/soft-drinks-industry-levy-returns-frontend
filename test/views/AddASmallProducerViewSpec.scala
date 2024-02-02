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

import base.ReturnsTestData.{ emptyUserAnswers, litres, returnPeriod }
import config.FrontendAppConfig
import forms.AddASmallProducerFormProvider
import models.{ AddASmallProducer, CheckMode, EditMode, Litres, LitresInBands, Mode, NormalMode, ReturnPeriod }
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.AddASmallProducerView

class AddASmallProducerViewSpec extends ViewSpecHelper with LitresSpecHelper {

  val addASmallProducerView: AddASmallProducerView = application.injector.instanceOf[AddASmallProducerView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val sdilProducerReference: String = "XKSDIL000000023"
  val addASmallProducer: AddASmallProducer = AddASmallProducer(Option("PRODUCER"), sdilProducerReference, lowBandValue, highBandValue)

  val formProvider = new AddASmallProducerFormProvider()

  val form: Form[AddASmallProducer] = formProvider.apply(emptyUserAnswers)
  val formWithHighAndLowBands: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> s"$lowBandValue", "highBand" -> s"$highBandValue"))
  val formWithLowBandOnly: Form[AddASmallProducer] = form.fill(addASmallProducer.copy(lowBand = 1, highBand = 0))
  val formWithHighBandOnly: Form[AddASmallProducer] = form.fill(addASmallProducer.copy(lowBand = 0, highBand = 1))
  val emptyForm: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

  "Add a small producer view " - {
    List(NormalMode, CheckMode, EditMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = addASmallProducerView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = addASmallProducerView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = addASmallProducerView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = addASmallProducerView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = addASmallProducerView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = addASmallProducerView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = addASmallProducerView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        val title = "Enter the registered small producer's details - Soft Drinks Industry Levy - GOV.UK"

        "should have the expected title" in {
          document.title() mustBe title
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe
            "Enter the registered small producer's details"
        }

        def testActionForAddSmallProducer(document: Document, mode: Mode): Unit = {
          "should contains a form with the correct action" in {
            println(Console.YELLOW + "in test Action document is " + document + Console.WHITE)
            val action = document.select(Selectors.form).attr("action")
            val route = if (mode != NormalMode) {
              controllers.routes.AddASmallProducerController.onEditPageSubmit(mode, sdilProducerReference).url
            } else {
              controllers.routes.AddASmallProducerController.onSubmit(mode).url
            }
            action mustEqual route
          }
        }

        testActionForAddSmallProducer(documentWithValidData, mode)
        testButton(documentWithValidData)

        val formGroupsNotPopulated = document.getElementsByClass(Selectors.govukFormGroup)

        "that includes 4 input fields" in {
          formGroupsNotPopulated.size() mustEqual 4
        }

        "that includes a field for small producer name that is not populated" in {
          val smallProducerGroup = formGroupsNotPopulated.get(0)
          smallProducerGroup.getElementsByClass(Selectors.label).text() mustBe "Small producer name (optional)"
          smallProducerGroup.getElementById("producerName").hasAttr("value") mustBe false
        }

        "that includes a field for SDIL reference number that is not populated" in {
          val sdilRefGroup = formGroupsNotPopulated.get(1)
          sdilRefGroup.getElementsByClass(Selectors.label).text() mustBe "Soft Drinks Industry Levy reference number"
          sdilRefGroup.getElementById("referenceNumber-hint").text() mustBe "This is 6 letters then 9 numbers, like XCSDIL123456789"
          sdilRefGroup.getElementById("referenceNumber").hasAttr("value") mustBe false
        }

        "that includes a field for low band that is not populated" in {
          val lowBandGroup = formGroupsNotPopulated.get(2)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe "Litres in the low band"
          lowBandGroup.getElementById("lowBand-hint").text() mustBe "At least 5 grams of sugar per 100 millilitres"
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe false
        }

        "that includes a field for high band that is not populated" in {
          val highBandGroup = formGroupsNotPopulated.get(3)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe "Litres in the high band"
          highBandGroup.getElementById("highBand-hint").text() mustBe "At least 8 grams of sugar per 100 millilitres"
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe false
        }

        val formGroupsPopulated = documentWithValidData.getElementsByClass(Selectors.govukFormGroup)

        "that includes a field for small producer name that is populated" in {
          val smallProducerGroup = formGroupsPopulated.get(0)
          smallProducerGroup.getElementsByClass(Selectors.label).text() mustBe "Small producer name (optional)"
          smallProducerGroup.getElementById("producerName").hasAttr("value") mustBe true
          smallProducerGroup.getElementById("producerName").attr("value") mustBe "PRODUCER"
        }
        "that includes a field for SDIL reference number that is populated" in {
          val sdilRefGroup = formGroupsPopulated.get(1)
          sdilRefGroup.getElementsByClass(Selectors.label).text() mustBe "Soft Drinks Industry Levy reference number"
          sdilRefGroup.getElementById("referenceNumber-hint").text() mustBe "This is 6 letters then 9 numbers, like XCSDIL123456789"
          sdilRefGroup.getElementById("referenceNumber").hasAttr("value") mustBe true
          sdilRefGroup.getElementById("referenceNumber").attr("value") mustBe sdilProducerReference
        }

        "that includes a field for low band that is populated" in {
          val lowBandGroup = formGroupsPopulated.get(2)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe "Litres in the low band"
          lowBandGroup.getElementById("lowBand-hint").text() mustBe "At least 5 grams of sugar per 100 millilitres"
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe true
          lowBandGroup.getElementById("lowBand").attr("value") mustBe lowBandValue.toString
        }

        "that includes a field for high band that is populated" in {
          val highBandGroup = formGroupsPopulated.get(3)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe "Litres in the high band"
          highBandGroup.getElementById("highBand-hint").text() mustBe "At least 8 grams of sugar per 100 millilitres"
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe true
          highBandGroup.getElementById("highBand").attr("value") mustBe highBandValue.toString
        }

        "and the form has errors " - {
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
