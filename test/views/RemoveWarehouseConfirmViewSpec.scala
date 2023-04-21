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

import org.jsoup.nodes.Document
import forms.RemoveWarehouseConfirmFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.{RemoveSmallProducerConfirmView, RemoveWarehouseConfirmView}
import messages.RemoveWarehouseConfirmMessages
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html

class RemoveWarehouseConfirmViewSpec extends ViewSpecHelper  {

  lazy val view: RemoveWarehouseConfirmView = application.injector.instanceOf[RemoveWarehouseConfirmView]

  val form = new RemoveWarehouseConfirmFormProvider

  lazy val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

  implicit lazy val testMessages: Messages =
    messagesApi.preferred(FakeRequest())

  def documentPopForm(isYes: Boolean = true) = {
    val popForm = form.apply()
      .fill(isYes)
    val popView = view(popForm, NormalMode, " 33 Rhes Priordy East London E73 2RP", "1")(FakeRequest(), testMessages)
    doc(popView)
  }

  object Selectors {
    val heading = "govuk-heading-m"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLables = "govuk-label govuk-radios__label"
    val body = "govuk-body-m"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "RemoveWarehouseConfirmView" - {
    "the form is not prepopulated and has no error" - {
      val popForm = form.apply()
      val html =
        view(popForm, NormalMode, "33 Rhes Priordy East London E73 2RP", "1")(FakeRequest(), testMessages)
      val document = doc(html)

      "have the expected title" in {
        document.getElementsByClass(Selectors.heading).text shouldBe RemoveWarehouseConfirmMessages.title
      }

      "have the expected address message" in{
        document.getElementsByClass(Selectors.body).text shouldBe RemoveWarehouseConfirmMessages.address
      }

      "have the expected radio button yes" in{
        document.getElementsByClass(Selectors.radios).get(0).text shouldBe RemoveWarehouseConfirmMessages.yes
      }

      "have the expected radio button no" in{
        document.getElementsByClass(Selectors.radios).get(1).text shouldBe RemoveWarehouseConfirmMessages.no
      }

    }

    "the form is not prepopulated and has no error" - {
      val formWithErrors = form.apply().bind(
        Map("select-continue" -> "")
      )
      val html =
        view(formWithErrors,NormalMode, "33 Rhes Priordy East London E73 2RP", "1")(FakeRequest(), testMessages)
      val document = doc(html)

      "have a page title containing error" in {
        document.getElementsByClass(Selectors.heading).text shouldBe RemoveWarehouseConfirmMessages.title
      }

      "contains a message that links to field with error" in {
        val errorSummary = document
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary.text() shouldBe RemoveWarehouseConfirmMessages.errorMessage
      }

    }

  }
}