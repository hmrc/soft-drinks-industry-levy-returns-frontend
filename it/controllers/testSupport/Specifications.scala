package controllers.testSupport

import controllers.testSupport.actions.ActionsBuilder
import controllers.testSupport.preConditions.PreconditionBuilder
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.PlaySpec

trait Specifications extends PlaySpec with AnyWordSpecLike with ScalaFutures {
  this: TestConfiguration =>

  val build = new PreconditionBuilder
  lazy val user = new ActionsBuilder(baseUrl)

}
