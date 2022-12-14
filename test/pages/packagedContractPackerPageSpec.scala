package pages

import pages.behaviours.PageBehaviours

class packagedContractPackerPageSpec extends PageBehaviours {

  "ackagedContractPackerPage" - {

    beRetrievable[Boolean](PackagedContractPackerPage)

    beSettable[Boolean](PackagedContractPackerPage)

    beRemovable[Boolean](PackagedContractPackerPage)
  }
}
