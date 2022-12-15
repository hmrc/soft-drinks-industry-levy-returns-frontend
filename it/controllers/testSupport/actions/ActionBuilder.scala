package controllers.testSupport.actions

class ActionsBuilder(baseUrl: String) {

  implicit val builder: ActionsBuilder = this

  def posts = new PostCalls(baseUrl)
  def gets = new GetCalls(baseUrl)

}
