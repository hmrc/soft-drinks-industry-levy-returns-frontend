package controllers.testSupport.preConditions

class PreconditionBuilder extends PreconditionHelpers {
  implicit val builder: PreconditionBuilder = this

  def user: UserStub = UserStub()
  def sdilBackend: SdilBackendStub = SdilBackendStub()
  def alf: ALFStub = ALFStub()

}

