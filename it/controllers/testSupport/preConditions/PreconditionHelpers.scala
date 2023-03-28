package controllers.testSupport.preConditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr","0000001611")
      .sdilBackend.retrieveSubscription("sdil","XKSDIL000000022")
      .sdilBackend.oldestPendingReturnPeriod("0000001611")
      .sdilBackend.balance("XKSDIL000000022", false)
      .sdilBackend.balanceHistory("XKSDIL000000022", false)
  }

}
