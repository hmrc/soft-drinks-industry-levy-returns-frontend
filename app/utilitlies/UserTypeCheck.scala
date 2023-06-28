/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utilitlies

import cats.implicits._
import models.{SdilReturn, UserAnswers}
import models.retrieved.RetrievedSubscription
import pages.{HowManyBroughtIntoTheUKFromSmallProducersPage, HowManyBroughtIntoUkPage}


object UserTypeCheck {
  def isNewImporter(sdilReturn: SdilReturn,subscription: RetrievedSubscription): Boolean = {
  (sdilReturn.totalImported._1 > 0L && sdilReturn.totalImported._2 > 0L) && !subscription.activity.importer && subscription.warehouseSites.isEmpty
}
  def isNewPacker(sdilReturn: SdilReturn, subscription: RetrievedSubscription): Boolean = {
    (sdilReturn.totalPacked._1 > 0L && sdilReturn.totalPacked._2 > 0L) && !subscription.activity.contractPacker
  }
}
