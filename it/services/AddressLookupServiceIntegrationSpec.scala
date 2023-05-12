package services

import controllers.testSupport.helpers.ALFTestHelper
import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.alf.init.{JourneyConfig, JourneyOptions}
import models.alf.{AlfAddress, AlfResponse}
import models.core.ErrorModel
import org.scalatest.TryValues
import play.api.http.Status
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupServiceIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues with FutureAwaits with DefaultAwaitTimeout {
  implicit val hc = HeaderCarrier()
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages = messagesApi.preferred(Seq(Lang("en")))

  "getAddress" should {

    "find the address in alf from the id given from alf" in {

      val addressFromAlf = AlfResponse(AlfAddress(
        organisation = Some("soft drinks ltd"),
        List("line 1", "line 2", "line 3", "line 4"),
        postcode = Some("aa1 1aa"),
        countryCode = Some("UK")
      ))

      val id = "001"
      given.alf.getAddress(id)
      val res = service.getAddress(id)
      whenReady(res) { result =>
        result mustBe addressFromAlf
      }

    }

    "return exception if json doesn't match AlfResponse" in {

      val id = "001"
      given.alf.getBadAddress(id)
      val res = service.getAddress(id)
      intercept[Exception](await(res))
    }

    "return exception if a response other than 200 is received" in {

      val id = "001"
      given.alf.getBadResponse(id)
      val res = service.getAddress(id)
      intercept[Exception](await(res))
    }
  }
  "initJourney" should {
    val journeyConfig = JourneyConfig(1, JourneyOptions(""), None, None)

    "return successful response with successful response from ALF" in {
      given.alf.getSuccessResponseFromALFInit(locationHeaderReturned ="foo")

      whenReady(service.initJourney(journeyConfig)) { result =>
        result mustBe Right("foo")
        ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfig) mustBe true
      }
    }

    "return error when error response returned from ALF" in {
      given.alf.getFailResponseFromALFInit( Status.INTERNAL_SERVER_ERROR)

      whenReady(service.initJourney(journeyConfig)) { result =>
        result mustBe Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Unexpected error occurred when init journey from ALF"))
        ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfig) mustBe true
      }
    }
  }
  "initJourneyAndReturnOnRampUrl" should {

    "return ramp on url when success" in {
      val req = FakeRequest()
      val sdilId: String = "bar"
      val journeyConfig = service.createJourneyConfig(PackingDetails, sdilId)(req, messages)
      given.alf.getSuccessResponseFromALFInit(locationHeaderReturned = "foo")

      whenReady(service.initJourneyAndReturnOnRampUrl(PackingDetails)(implicitly,implicitly, messages, req)) { result =>
        result mustBe "foo"
        ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfig) mustBe true
      }
    }
    "throw exception when fail" in {
      val req = FakeRequest()
      val sdilId: String = "bar"
      val journeyConfig = service.createJourneyConfig(PackingDetails, sdilId)(req, messages)
      given.alf.getFailResponseFromALFInit(Status.INTERNAL_SERVER_ERROR)

      intercept[Exception](await(service.initJourneyAndReturnOnRampUrl(PackingDetails)(implicitly,implicitly, messages, req)))
      ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfig) mustBe true
    }
  }
}
