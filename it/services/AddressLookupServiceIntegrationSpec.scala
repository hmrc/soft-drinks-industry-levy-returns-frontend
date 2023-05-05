package services

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.alf.AlfResponse
import models.alf.init.{JourneyConfig, JourneyOptions}
import models.core.ErrorModel
import org.scalatest.TryValues
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupServiceIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues  {
  implicit val hc = HeaderCarrier()

  "getAddress" should {

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

    "should return internal server error if json doesn't match AlfResponse" in {

      val id = "001"
      given.alf.getBadAddress(id)
      val res = service.getAddress(id)
      whenReady(res) { result =>
        result mustBe Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Invalid Json returned from Address Lookup"))
      }

    }

    "should return downstream error if a response other than 200 is received" in {

      val id = "001"
      given.alf.getBadResponse(id)
      val res = service.getAddress(id)
      whenReady(res) { result =>
        result mustBe Left(ErrorModel(404, "Downstream error returned when retrieving CustomerAddressModel from AddressLookup"))
      }

    }
  }
  "initJourney" should {
    val journeyConfig = JourneyConfig(1, JourneyOptions(""), None, None)

    "should return successful response with successful response from ALF" in {
      given.alf.getSuccessResponseFromALFInit(Json.toJson(journeyConfig), locationHeaderReturned ="foo")

      whenReady(service.initJourney(journeyConfig)) { result =>
        result mustBe Right("foo")
      }
    }

    "should return error when error response returned from ALF" in {
      given.alf.getFailResponseFromALFInit(Json.toJson(journeyConfig), Status.INTERNAL_SERVER_ERROR)

      whenReady(service.initJourney(journeyConfig)) { result =>
        result mustBe Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Unexpected error occurred when init journey from ALF"))
      }
    }
  }

}
