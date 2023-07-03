package controllers.testSupport.preConditions

import models.retrieved.RetrievedSubscription

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  val UTR = "0000001611"
  val sdilNumber = "XKSDIL000000022"

  def commonPreconditionChangeSubscription(retrievedSubscription: RetrievedSubscription): PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionToModify("utr", UTR, retrievedSubscription)
      .sdilBackend.pendingReturnPeriod("0000001611")
  }

  def commonPreconditionBoth: PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolledBoth
      .sdilBackend.retrieveSubscription("sdil", sdilNumber)
      .sdilBackend.pendingReturnPeriod("0000001611")
  }

  def commonPreconditionSdilRef: PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolledSDILRef
      .sdilBackend.retrieveSubscription("sdil", sdilNumber)
      .sdilBackend.pendingReturnPeriod("0000001611")
  }

  def authorisedWithSdilSubscriptionIncDeRegDatePrecondition: PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
  }

  //invalid user preconditions
  def authorisedButNoEnrolmentsPrecondition: Any = {
    builder
      .user.isAuthorisedButNotEnrolled
  }

  def authorisedWithNoSubscriptionPrecondition: PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionNone("utr", UTR)
  }

  def authorisedWithNoSubscriptionPreconditionWithSDilRef: PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolledBoth
      .sdilBackend.retrieveSubscriptionNone("sdil", sdilNumber)
  }

  def unauthorisedPrecondition: PreconditionBuilder = {
    builder
      .user.isNotAuthorised()
  }

  def authorisedButInternalIdPrecondition: PreconditionBuilder = {
    builder
      .user.isAuthorisedWithMissingInternalId
  }

}
