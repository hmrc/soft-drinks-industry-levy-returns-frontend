package pages

import pages.behaviours.PageBehaviours

class SmallProducerDetailsPageSpec extends PageBehaviours {

  "SmallProducerDetailsPage" - {

    beRetrievable[Boolean](SmallProducerDetailsPage)

    beSettable[Boolean](SmallProducerDetailsPage)

    beRemovable[Boolean](SmallProducerDetailsPage)
  }
}
