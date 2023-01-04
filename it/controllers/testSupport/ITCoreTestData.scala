package controllers.testSupport

import models.UserAnswers
import pages.OwnBrandsPage
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt

trait ITCoreTestData {
  val sdilNumber = "XKSDIL000000022"
  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(sdilNumber, Json.obj())

  def ownBrandPageAnswers = emptyUserAnswers
    .set(OwnBrandsPage, true)

}
