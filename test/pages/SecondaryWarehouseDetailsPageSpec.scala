package pages

import pages.behaviours.PageBehaviours

class SecondaryWarehouseDetailsPageSpec extends PageBehaviours {

  "SecondaryWarehouseDetailsPage" - {

    beRetrievable[Boolean](SecondaryWarehouseDetailsPage)

    beSettable[Boolean](SecondaryWarehouseDetailsPage)

    beRemovable[Boolean](SecondaryWarehouseDetailsPage)
  }
}
