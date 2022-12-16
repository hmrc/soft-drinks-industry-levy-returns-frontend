package pages

import models.HowManyBoughtIntoUk
import pages.behaviours.PageBehaviours

class HowManyBoughtIntoUkPageSpec extends PageBehaviours {

  "HowManyBoughtIntoUkPage" - {

    beRetrievable[HowManyBoughtIntoUk](HowManyBoughtIntoUkPage)

    beSettable[HowManyBoughtIntoUk](HowManyBoughtIntoUkPage)

    beRemovable[HowManyBoughtIntoUk](HowManyBoughtIntoUkPage)
  }
}
