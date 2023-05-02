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

package controllers.test

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AddressFrontendStubControllerSpec extends SpecBase {

  val controller = new AddressFrontendStubController(mcc)

  "initialise" - {
    "should return Accepted with the correct location header" in {
      val res = controller.initalise().apply(FakeRequest())
      status(res) mustEqual 202
      headers(res).get(LOCATION) mustEqual Some("/soft-drinks-industry-levy-returns-frontend/test-only/rampOn")
    }
  }

  "rampOn" - {
    "should redirect to the callback url" in {
      val res = controller.rampOn().apply(FakeRequest())
      status(res) mustEqual 303
      redirectLocation(res) mustEqual Some("/soft-drinks-industry-levy-returns-frontend/address-lookup/callback")
    }
  }


  "addresses" - {
    "should return 200 and addresses" in {
      val addressConfirmed =
        "[{\"auditRef\":\"bed4bd24-72da-42a7-9338-f43431b7ed72\"," +
          "\"id\":\"GB990091234524\",\"address\":{\"organisation\":\"Some Trading Name\",\"lines\":[\"10 Other Place\"," +
          "\"Some District\",\"Anytown\"],\"postcode\":\"ZZ1 1ZZ\"," +
          "\"country\":{\"code\":\"GB\",\"name\":\"United Kingdom\"}}}]"

      val res = controller.addresses("12345678").apply(FakeRequest())

      status(res) mustEqual 200
      contentAsString(res) mustEqual addressConfirmed

    }
  }

}
