package pages

import pages.behaviours.PageBehaviours

class BroughtIntoUkFromSmallProducersPageSpec extends PageBehaviours {

  "BroughtIntoUkFromSmallProducersPage" - {

    beRetrievable[Boolean](BroughtIntoUkFromSmallProducersPage)

    beSettable[Boolean](BroughtIntoUkFromSmallProducersPage)

    beRemovable[Boolean](BroughtIntoUkFromSmallProducersPage)
  }
}
