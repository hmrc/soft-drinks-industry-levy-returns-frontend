package controllers.testSupport.preConditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr","0000001611")
      .sdilBackend.retrieveSubscription("sdil","XKSDIL000000022")
      .sdilBackend.oldestPendingReturnPeriod("0000001611")

  }

  def newImporterPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000001611")
      .sdilBackend.retrieveSubscription("sdil", "XGSDIL000001611")
      .sdilBackend.oldestPendingReturnPeriod("0000001611")
  }

  def newPackagerPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000000069")
      .sdilBackend.retrieveSubscription("sdil", "XCSDIL000000069")
      .sdilBackend.oldestPendingReturnPeriod("0000000069")
  }

  def newPackagerAndImporterPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000000232")
      .sdilBackend.retrieveSubscription("sdil", "XSSDIL000000232")
      .sdilBackend.oldestPendingReturnPeriod("0000000232")
  }

}
