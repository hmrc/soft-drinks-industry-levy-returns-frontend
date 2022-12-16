package controllers

import base.SpecBase
import forms.HowManyBoughtIntoUkFormProvider
import models.{NormalMode, HowManyBoughtIntoUk, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.HowManyBoughtIntoUkPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.owManyBoughtIntoUkView

import scala.concurrent.Future

class HowManyBoughtIntoUkControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HowManyBoughtIntoUkFormProvider()
  val form = formProvider()

  val value1max: Long = 100000000000000L
  val value1 = value1max - 1

  val value2max: Long = 100000000000000L
  val value2 = value2max - 1

  lazy val owManyBoughtIntoUkRoute = routes.owManyBoughtIntoUkController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      HowManyBoughtIntoUkPage.toString -> Json.obj(
        "lowBandLitres" -> value1,
        "highBandLitres" -> value2
      )
    )
  )

  "HowManyBoughtIntoUk Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, owManyBoughtIntoUkRoute)

        val view = application.injector.instanceOf[owManyBoughtIntoUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, owManyBoughtIntoUkRoute)

        val view = application.injector.instanceOf[owManyBoughtIntoUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(HowManyBoughtIntoUk(value1, value2)), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, owManyBoughtIntoUkRoute)
            .withFormUrlEncodedBody(("lowBandLitres", value1.toString), ("highBandLitres", value2.toStringalue))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, owManyBoughtIntoUkRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[owManyBoughtIntoUkView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

  }
}
