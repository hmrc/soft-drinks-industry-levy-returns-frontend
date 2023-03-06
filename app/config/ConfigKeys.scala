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

package config

object ConfigKeys {

  val stubAgentClientLookupFeature: String = "features.stubAgentClientLookup"
  val stubAddressLookupFeature: String = "features.stubAddressLookup.enabled"

  val addressLookupFrontend: String = "address-lookup-frontend"
  val addressLookupCallback: String = s"$addressLookupFrontend.callbackUrl"
  val addressLookupFrontendHost: String = s"$addressLookupFrontend.host"
  val signInContinueBaseUrl: String = "signIn.continueBaseUrl"

  val timeoutPeriod: String = "timeout.period"
  val timeoutCountDown: String = "timeout.countDown"

  val accessibilityReportUrl: String = "accessibility-statement.service-path"
  val deskproServiceName: String = "Soft Drinks Industry Levy Returns"

}
