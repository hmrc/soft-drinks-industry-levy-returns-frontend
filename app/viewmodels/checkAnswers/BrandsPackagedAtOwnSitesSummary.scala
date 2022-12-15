package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.BrandsPackagedAtOwnSitesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BrandsPackagedAtOwnSitesSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BrandsPackagedAtOwnSitesPage).map {
      answer =>

      val value = HtmlFormat.escape(answer.lowBandLitres.toString).toString + "<br/>" + HtmlFormat.escape(answer.highBandLitres.toString).toString

        SummaryListRowViewModel(
          key     = "brandsPackagedAtOwnSites.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("brandsPackagedAtOwnSites.change.hidden"))
          )
        )
    }
}
