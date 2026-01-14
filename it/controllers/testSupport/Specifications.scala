/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.testSupport

import controllers.testSupport.actions.ActionsBuilder
import controllers.testSupport.preConditions.PreconditionBuilder
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.PlaySpec

trait Specifications extends PlaySpec with AnyWordSpecLike with ScalaFutures {
  this: TestConfiguration =>

  val build     = new PreconditionBuilder
  lazy val user = new ActionsBuilder(baseUrl)

}
