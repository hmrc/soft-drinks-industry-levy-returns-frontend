package pages

import pages.behaviours.PageBehaviours

class packagingSiteDetailsPageSpec extends PageBehaviours {

  "productionSiteDetailsPage" - {

    beRetrievable[Boolean](PackagingSiteDetailsPage)

    beSettable[Boolean](PackagingSiteDetailsPage)

    beRemovable[Boolean](PackagingSiteDetailsPage)
  }
}
