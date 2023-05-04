package services

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.AlfResponse
import org.scalatest.TryValues
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupServiceIntergrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues  {
  implicit val hc = HeaderCarrier()

  "AddressLookupService" should {
    "should find the address in alf from the id given from alf" in {

      val aAddress = AlfResponse(
        organisation = Some("soft drinks ltd"),
        List("line 1", "line 2", "line 3", "line 4"),
        postcode = Some("aa1 1aa"),
        countryCode = Some("UK")
      )

      val id = "001"
      given.alf.getAddress(id)
      val res = service.getAddress(id)
      whenReady(res) { result =>
        result mustBe Right(aAddress)
      }

    }
  }

}
