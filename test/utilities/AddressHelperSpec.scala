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

package utilities

import base.SpecBase
import utilitlies.AddressHelper

class AddressHelperSpec extends SpecBase with AddressHelper {

    "Address Helper" - {
      "generate a unique id" in {

        val result:Map[String,String] = Map((generateId , "testAddress"), (generateId , "testAddress"), (generateId , "testAddress") , (generateId,"testAddress"))

        result.size mustBe 4
        result.head._1.length mustBe 36
      }
    }
}
