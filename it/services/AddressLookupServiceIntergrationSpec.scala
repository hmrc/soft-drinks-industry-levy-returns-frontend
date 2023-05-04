package services

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.Address
import org.scalatest.TryValues
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupServiceIntergrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues  {
  implicit val hc = HeaderCarrier()

  "AddressLookupService" should {
    "should find the address in alf from the id given from alf" in {

      val aAddress = Address(
        organisation = Some("soft drinks ltd"),
        line1 = Some("line 1"),
        line2 = Some("line 2"),
        line3 = Some("line 3"),
        line4 = Some("line 4"),
        postcode = Some("aa1 1aa"),
        countryCode = Some("UK")
      )

      val id = "001"
      given.alf.getAddress(id)
      val res = service.getAddress(id)
      whenReady(res) { result =>
        result mustBe aAddress
      }

    }
  }

}
