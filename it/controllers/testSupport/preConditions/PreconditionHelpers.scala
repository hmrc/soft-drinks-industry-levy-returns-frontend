package controllers.testSupport.preConditions

import models.retrieved.RetrievedSubscription

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition(retrievedSubscription: RetrievedSubscription) = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr","0000001611", retrievedSubscription)
      .sdilBackend.retrieveSubscription("sdil","XKSDIL000000022", retrievedSubscription)
      .sdilBackend.pendingReturnPeriod("0000001611")
  }

}
