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

package base
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.Configuration
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException.{ Missing, WrongType }
import config.Service

class ServiceConfigLoaderSpec extends AnyWordSpec with Matchers {

  "Service ConfigLoader" should {

    "load a Service from configuration and build the correct baseUrl" in {
      val confStr =
        """
          |example-service {
          |  host = "localhost"
          |  port = "8080"
          |  protocol = "http"
          |}
          |""".stripMargin

      val service =
        Configuration(ConfigFactory.parseString(confStr)).get[Service]("example-service")

      service.host shouldBe "localhost"
      service.port shouldBe "8080"
      service.protocol shouldBe "http"
      service.toString shouldBe "http://localhost:8080"
    }

    "support implicit conversion to String (baseUrl)" in {
      val confStr =
        """
          |svc {
          |  host = "api.service.gov.uk"
          |  port = "443"
          |  protocol = "https"
          |}
          |""".stripMargin

      val svc = Configuration(ConfigFactory.parseString(confStr)).get[Service]("svc")

      val asString: String = svc
      asString shouldBe "https://api.service.gov.uk:443"
    }

    "throw a Missing exception if a required key is absent" in {
      val confStr =
        """
          |broken {
          |  host = "localhost"
          |  port = "9000"
          |  # protocol is missing
          |}
          |""".stripMargin

      val cfg = Configuration(ConfigFactory.parseString(confStr))
      intercept[Missing] {
        cfg.get[Service]("broken")
      }
    }

    "throw a WrongType exception if a key has the wrong type" in {
      val confStr =
        """
          |wrong-type {
          |  host = "localhost"
          |  port = "9000"
          |  protocol = { nested = "value" }  # definitely not a string
          |}
          |""".stripMargin

      val cfg = Configuration(ConfigFactory.parseString(confStr))
      intercept[WrongType] {
        cfg.get[Service]("wrong-type")
      }
    }

    "throw a Missing exception if the prefix does not exist" in {
      val cfg = Configuration(ConfigFactory.parseString("{}", com.typesafe.config.ConfigParseOptions.defaults()))
      intercept[Missing] {
        cfg.get[Service]("does-not-exist")
      }
    }
  }
}