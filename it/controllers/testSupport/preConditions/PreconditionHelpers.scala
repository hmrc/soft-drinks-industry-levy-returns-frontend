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

package controllers.testSupport.preConditions

import models.retrieved.RetrievedSubscription

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  val UTR        = "0000001611"
  val sdilNumber = "XKSDIL000000022"

  def commonPreconditionChangeSubscription(retrievedSubscription: RetrievedSubscription): PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscriptionToModify("utr", UTR, retrievedSubscription)
      .sdilBackend
      .pendingReturnPeriod("0000001611")
      .sdilBackend
      .calculateLevyDefault()
      .sdilBackend
      .calculateLevy(BigDecimal("180"), BigDecimal("240"), 1000L, 1000L)

  def commonPreconditionBoth: PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolledBoth.sdilBackend.retrieveSubscription("sdil", sdilNumber).sdilBackend.pendingReturnPeriod("0000001611")

  def commonPreconditionSdilRef: PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolledSDILRef.sdilBackend.retrieveSubscription("sdil", sdilNumber).sdilBackend.pendingReturnPeriod("0000001611")

  def authorisedWithSdilSubscriptionIncDeRegDatePrecondition: PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")

  // invalid user preconditions
  def authorisedButNoEnrolmentsPrecondition: Any =
    builder.user.isAuthorisedButNotEnrolled

  def authorisedWithNoSubscriptionPrecondition: PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend.retrieveSubscriptionNone("utr", UTR)

  def authorisedWithNoSubscriptionPreconditionWithSDilRef: PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolledBoth.sdilBackend.retrieveSubscriptionNone("sdil", sdilNumber)

  def authorisedWithNoPendingReturns(retrievedSubscription: RetrievedSubscription): PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscriptionToModify("utr", UTR, retrievedSubscription)
      .sdilBackend
      .pendingReturnPeriodNone("0000001611")

  def unauthorisedPrecondition: PreconditionBuilder =
    builder.user.isNotAuthorised()

  def authorisedButInternalIdPrecondition: PreconditionBuilder =
    builder.user.isAuthorisedWithMissingInternalId

}
