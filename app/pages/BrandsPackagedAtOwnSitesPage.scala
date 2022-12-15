package pages

import models.BrandsPackagedAtOwnSites
import play.api.libs.json.JsPath

case object BrandsPackagedAtOwnSitesPage extends QuestionPage[BrandsPackagedAtOwnSites] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "brandsPackagedAtOwnSites"
}
