/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.actions

import base.SpecBase
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class ActionHelpersSpec extends SpecBase {

  object Helpers extends ActionHelpers

  "getAllSdilEnrolments" - {
    "return all valid SDIL refs from enrolments" in {
      val enrolments = Enrolments(
        Set(
          Enrolment("HMRC-OBTDS-ORG", Seq(EnrolmentIdentifier("EtmpRegistrationNumber", "XCSDIL00045983")), "Activated"),
          Enrolment("HMRC-OBTDS-ORG", Seq(EnrolmentIdentifier("EtmpRegistrationNumber", "XRSDIL00045793")), "Activated"),
          Enrolment("HMRC-OBTDS-ORG", Seq(EnrolmentIdentifier("EtmpRegistrationNumber", "XRSXIL00045793")), "Activated")
        )
      )

      val result = Helpers.getAllSdilEnrolments(enrolments)

      result must contain allOf ("XCSDIL00045983", "XRSDIL00045793")
      result must not contain "XRSXIL00045793"
    }

    "return empty sequence when no SDIL enrolments exist" in {
      val enrolments = Enrolments(Set.empty)

      val result = Helpers.getAllSdilEnrolments(enrolments)

      result mustBe empty
    }
  }
}
