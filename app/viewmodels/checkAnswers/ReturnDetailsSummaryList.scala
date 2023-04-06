package viewmodels.checkAnswers

import config.FrontendAppConfig
import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}

trait ReturnDetailsSummaryList {

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): SummaryList

}
