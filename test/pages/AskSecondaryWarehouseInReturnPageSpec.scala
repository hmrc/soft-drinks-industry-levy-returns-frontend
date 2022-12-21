package pages

import pages.behaviours.PageBehaviours

class AskSecondaryWarehouseInReturnPageSpec extends PageBehaviours {

  "AskSecondaryWarehouseInReturnPage" - {

    beRetrievable[Boolean](AskSecondaryWarehouseInReturnPage)

    beSettable[Boolean](AskSecondaryWarehouseInReturnPage)

    beRemovable[Boolean](AskSecondaryWarehouseInReturnPage)
  }
}
