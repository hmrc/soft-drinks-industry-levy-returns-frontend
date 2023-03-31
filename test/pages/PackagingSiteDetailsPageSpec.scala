package pages

import pages.behaviours.PageBehaviours
import viewmodels.govuk.SummaryListFluency
import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import forms.PackagingSiteDetailsFormProvider
import models.backend.{Site, UkAddress}
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.PackagingSiteDetailsView
import controllers.routes
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.PackagingSiteDetailsSummary

class packagingSiteDetailsPageSpec extends SpecBase with MockitoSugar with SummaryListFluency with PageBehaviours {

//  val formProvider = new PackagingSiteDetailsFormProvider()
  val form = new PackagingSiteDetailsFormProvider()
  val view: PackagingSiteDetailsView = application.injector.instanceOf[PackagingSiteDetailsView]
  val packagingSummaryList: List[SummaryListRow] =
    PackagingSiteDetailsSummary.row2(List())(messages(application))

  SummaryListViewModel(
    rows = packagingSummaryList
  )
  val filledSummaryList: FluentSummaryList
  def psdPopForm() = {
    val popForm = form().fill(true)
    val popView = view(popForm, NormalMode, packagingSummaryList)(implicit request: Request[_], messages: Messages)
  }
  val htmlfor: HtmlFormat.Appendable =
    view(form, NormalMode, SummaryList, routes.PackagingSiteDetailsController.onPageLoad(NormalMode))(implicit request: Request[_], messages: Messages)

  object Selectors {
    val heading = "govuk-heading-m"
    val body = "govuk-body"
    val button = "govuk-button"
    val form = "form"
  }

  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  val PackagingSite2 = Site(
    UkAddress(List("30 Rhes Priordy", "East London"), "E73 2RP"),
    Some("10"),
    None,
    None)

  val packagingSiteListWith2 = List(PackagingSite1, PackagingSite2)
  val packagingSiteListWith1 = List(PackagingSite1)

  val userAnswersWith1PackagingSite = UserAnswers(sdilNumber, Json.obj(), List.empty, packagingSiteListWith1)
  val userAnswersWith2PackagingSites = UserAnswers(sdilNumber, Json.obj(), List.empty, packagingSiteListWith2)

  "packagingSiteDetailsPage" - {

    beRetrievable[Boolean](PackagingSiteDetailsPage)

    beSettable[Boolean](PackagingSiteDetailsPage)

    beRemovable[Boolean](PackagingSiteDetailsPage)
  }

  "must return OK and the correct view for a GET" in {

    val application = applicationBuilder(userAnswers = Some(userAnswersWith1PackagingSite)).build()

    running(application) {
      val request = FakeRequest(GET, packagingSiteDetailsRoute)

      val result = route(application, request).value

      val packagingSummaryList: List[SummaryListRow] =
        PackagingSiteDetailsSummary.row2(List())(messages(application))

      SummaryListViewModel(
        rows = packagingSummaryList
      )

      application.injector.instanceOf[PackagingSiteDetailsView]

      status(result) mustEqual OK
      val page = Jsoup.parse(contentAsString(result))

      page.title() must include("You added 1 packaging sites")
      page.getElementsByTag("h1").text() mustEqual "You added 1 packaging sites"
      page.getElementsByTag("h2").text() must include("Do you want to add another UK packaging site?")
    }
  }

}
