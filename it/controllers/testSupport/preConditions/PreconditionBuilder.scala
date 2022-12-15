package controllers.testSupport.preConditions

class PreconditionBuilder extends PreconditionHelpers {
  implicit val builder: PreconditionBuilder = this

  def user = UserStub()
  def sdilBackend = SdilBackendStub()

}

