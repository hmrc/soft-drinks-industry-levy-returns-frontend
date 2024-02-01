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

import models.LitresInBands
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages

trait LitresSpecHelper extends ViewSpecHelper {

  val lowBandValue: Long = 1000
  val highBandValue: Long = 2000
  val litresInBands: LitresInBands = LitresInBands(lowBandValue, highBandValue)

  object Selectors {
    val heading = "govuk-heading-l"
    val legendHeading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val govukFormGroup = "govuk-form-group"
    val label = "govuk-label"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLabels = "govuk-label govuk-radios__label"
    val button = "govuk-button"
    val form = "form"
  }

  def testLitresInBandsWithPrepopulatedData(document: Document): Unit = {
    "should include form groups for litres" - {
      "when the form is not prepopulated and has no errors" - {
        val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
        "that includes 2 input fields" in {
          formGroups.size() mustEqual 2
        }
        "that includes a field for low band that is populated" in {
          val lowBandGroup = formGroups.get(0)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe true
          lowBandGroup.getElementById("lowBand").attr("value") mustBe lowBandValue.toString
        }
        "that includes a field for high band that is populated" in {
          val highBandGroup = formGroups.get(1)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe true
          highBandGroup.getElementById("highBand").attr("value") mustBe highBandValue.toString
        }
      }
    }
  }

  def testLitresInBandsNoPrepopulatedData(document: Document): Unit = {
    "should include form groups for litres" - {
      "when the form is populated and has no errors" - {
        val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
        "that includes 2 input fields" in {
          formGroups.size() mustEqual 2
        }
        "that includes a field for low band that is not populated" in {
          val lowBandGroup = formGroups.get(0)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe "Litres in the low band"
          lowBandGroup.getElementById("lowBand-hint").text() mustBe "At least 5 grams of sugar per 100 millilitres"
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe false
        }
        "that includes a field for high band that is not populated" in {
          val highBandGroup = formGroups.get(1)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe "Litres in the high band"
          highBandGroup.getElementById("highBand-hint").text() mustBe "At least 8 grams of sugar per 100 millilitres"
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe false
        }
      }
    }
  }

  def testButton(document: Document): Unit = {
    "should contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }
  }

  def testAction(document: Document, expectedAction: String): Unit = {
    "should contains a form with the correct action" in {
      document.select(Selectors.form)
        .attr("action") mustEqual expectedAction
    }
  }

  def testEmptyFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form being empty" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe "Enter the number of litres in the low band"
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe "Enter the number of litres in the high band"
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testNoNumericFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing no numeric values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" in {
        val errorSummary = document
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        val errors = errorSummary.getElementsByTag("li")

        errors.size() mustEqual 2
        val error1 = errors.get(0)
        val error2 = errors.get(1)

        error1.text() mustBe "Number of litres in the low band must be numeric"
        error1.select("a").attr("href") mustBe "#lowBand"
        error2.text() mustBe "Number of litres in the high band must be numeric"
        error2.select("a").attr("href") mustBe "#highBand"
      }
    }
  }

  def testNegativeFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing no negative values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" in {
        val errorSummary = document
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        val errors = errorSummary.getElementsByTag("li")
        errors.size() mustEqual 2
        val error1 = errors.get(0)
        val error2 = errors.get(1)
        error1.text() mustBe "Number of litres in the low band must be a positive number"
        error1.select("a").attr("href") mustBe "#lowBand"
        error2.text() mustBe "Number of litres in the high band must be a positive number"
        error2.select("a").attr("href") mustBe "#highBand"
      }
    }
  }

  def testDecimalFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing decimal values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" in {
        val errorSummary = document
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        val errors = errorSummary.getElementsByTag("li")

        errors.size() mustEqual 2
        val error1 = errors.get(0)
        val error2 = errors.get(1)

        error1.text() mustBe "Number of litres in the low band must be a whole number"
        error1.select("a").attr("href") mustBe "#lowBand"
        error2.text() mustBe "Number of litres in the high band must be a whole number"
        error2.select("a").attr("href") mustBe "#highBand"
      }
    }
  }

  def testOutOfMaxValFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing values out of max range" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" in {
        val errorSummary = document
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        val errors = errorSummary.getElementsByTag("li")

        errors.size() mustEqual 2
        val error1 = errors.get(0)
        val error2 = errors.get(1)

        error1.text() mustBe "Number of litres in the low band must be less than 100,000,000,000,000"
        error1.select("a").attr("href") mustBe "#lowBand"
        error2.text() mustBe "Number of litres in the high band must be less than 100,000,000,000,000"
        error2.select("a").attr("href") mustBe "#highBand"
      }
    }
  }
}
