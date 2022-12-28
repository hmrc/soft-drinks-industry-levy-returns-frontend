package pages

import models.AddASmallProducer
import pages.behaviours.PageBehaviours

class AddASmallProducerPageSpec extends PageBehaviours {

  "AddASmallProducerPage" - {

    beRetrievable[AddASmallProducer](AddASmallProducerPage)

    beSettable[AddASmallProducer](AddASmallProducerPage)

    beRemovable[AddASmallProducer](AddASmallProducerPage)
  }
}
