package pages

import models.BrandsPackagedAtOwnSites
import pages.behaviours.PageBehaviours

class BrandsPackagedAtOwnSitesPageSpec extends PageBehaviours {

  "BrandsPackagedAtOwnSitesPage" - {

    beRetrievable[BrandsPackagedAtOwnSites](BrandsPackagedAtOwnSitesPage)

    beSettable[BrandsPackagedAtOwnSites](BrandsPackagedAtOwnSitesPage)

    beRemovable[BrandsPackagedAtOwnSites](BrandsPackagedAtOwnSitesPage)
  }
}
