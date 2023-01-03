package pages

import pages.behaviours.PageBehaviours

class smallProducerDetailsPageSpec extends PageBehaviours {

  "smallProducerDetailsPage" - {

    beRetrievable[Boolean](smallProducerDetailsPage)

    beSettable[Boolean](smallProducerDetailsPage)

    beRemovable[Boolean](smallProducerDetailsPage)
  }
}
