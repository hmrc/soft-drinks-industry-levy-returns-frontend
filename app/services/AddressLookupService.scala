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

package services

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import connectors.httpParsers.ResponseHttpParser.HttpResult
import controllers.routes
import models.alf.init._
import models.alf.{ AlfAddress, AlfResponse }
import models.backend.{ Site, UkAddress }
import models.{ Mode, NormalMode, UserAnswers }
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.AddressHelper

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AddressLookupService @Inject() (
  addressLookupConnector: AddressLookupConnector,
  frontendAppConfig: FrontendAppConfig) extends AddressHelper {

  val logger: Logger = Logger(this.getClass)

  def addressChecker(address: AlfAddress, alfId: String): UkAddress = {
    val ukAddress: UkAddress = UkAddress(address.lines, address.postcode.getOrElse(""), alfId = Some(alfId))

    if (ukAddress.lines.isEmpty && ukAddress.postCode == "" && address.organisation.isEmpty) {
      throw new RuntimeException("Not Found (Alf has returned an empty address and organisation name)")
    } else {
      ukAddress
    }
  }

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AlfResponse] = {
    addressLookupConnector.getAddress(id).map {
      case Right(addressResponse) => addressResponse
      case Left(error) => throw new Exception(s"Error returned from ALF for $id ${error.status} ${error.message} for ${hc.requestId}")
    }
  }

  def addAddressUserAnswers(
    addressLookupState: AddressLookupState,
    address: AlfAddress,
    userAnswers: UserAnswers,
    siteId: String,
    alfId: String): UserAnswers = {

    val convertedAddress: UkAddress = addressChecker(address, alfId)
    val site = Site(convertedAddress, tradingName = address.organisation)

    addressLookupState match {
      case PackingDetails =>
        userAnswers.addPackagingSite(site, siteId)
      case WarehouseDetails =>
        userAnswers.addWarehouse(site, siteId)
    }
  }

  def initJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[String]] = {
    addressLookupConnector.initJourney(journeyConfig)
  }

  def initJourneyAndReturnOnRampUrl(state: AddressLookupState, siteId: String = generateId, mode: Mode = NormalMode)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages, requestHeader: RequestHeader): Future[String] = {
    val journeyConfig: JourneyConfig = createJourneyConfig(state, siteId, mode: Mode)
    initJourney(journeyConfig).map {
      case Right(onRampUrl) => onRampUrl
      case Left(error) => throw new Exception(s"Failed to init ALF ${error.message} with status ${error.status} for ${hc.requestId}")
    }
  }

  def createJourneyConfig(state: AddressLookupState, siteId: String, mode: Mode = NormalMode)(implicit requestHeader: RequestHeader, messages: Messages): JourneyConfig = {
    JourneyConfig(
      version = frontendAppConfig.AddressLookupConfig.version,
      options = JourneyOptions(
        continueUrl = returnContinueUrl(state, siteId, mode: Mode),
        homeNavHref = None,
        signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
        accessibilityFooterUrl = None,
        phaseFeedbackLink = Some(frontendAppConfig.feedbackUrl(requestHeader)),
        deskProServiceName = None,
        showPhaseBanner = Some(false),
        alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
        includeHMRCBranding = Some(true),
        ukMode = Some(true),
        selectPageConfig = Some(SelectPageConfig(
          proposalListLimit = Some(frontendAppConfig.AddressLookupConfig.selectPageConfigProposalLimit),
          showSearchAgainLink = Some(true))),
        showBackButtons = Some(true),
        disableTranslations = Some(true),
        allowedCountryCodes = None,
        confirmPageConfig = Some(ConfirmPageConfig(
          showSearchAgainLink = Some(true),
          showSubHeadingAndInfo = Some(true),
          showChangeLink = Some(true),
          showConfirmChangeText = Some(true))),
        timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = frontendAppConfig.timeout,
          timeoutUrl = controllers.auth.routes.AuthController.signOut().url,
          timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url))),
        serviceHref = Some(frontendAppConfig.sdilHomeUrl),
        pageHeadingStyle = Some("govuk-heading-l")),
      labels = returnJourneyLabels(state),
      requestedVersion = None)
  }

  private def returnJourneyLabels(state: AddressLookupState)(implicit messages: Messages): Option[JourneyLabels] = {
    state match {
      case PackingDetails => Some(
        JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some(messages("service.name")),
              phaseBannerHtml = None)),
            selectPageLabels = None,
            lookupPageLabels = Some(
              LookupPageLabels(
                title = Some(messages("addressLookupFrontend.packingDetails.lookupPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.packingDetails.lookupPageLabels.title")),
                postcodeLabel = Some(messages("addressLookupFrontend.packingDetails.lookupPageLabels.postcodeLabel")))),
            editPageLabels = Some(
              EditPageLabels(
                title = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.title")),
                line1Label = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.line1Label")),
                line2Label = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.line2Label")),
                line3Label = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.line3Label")),
                townLabel = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.townLabel")),
                postcodeLabel = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.postcodeLabel")),
                organisationLabel = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.organisationLabel")))),
            confirmPageLabels = None,
            countryPickerLabels = None))))

      case WarehouseDetails => Some(
        JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some(messages("service.name")),
              phaseBannerHtml = None)),
            selectPageLabels = None,
            lookupPageLabels = Some(
              LookupPageLabels(
                title = Some(messages("addressLookupFrontend.warehouseDetails.lookupPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.warehouseDetails.lookupPageLabels.title")),
                postcodeLabel = Some(messages("addressLookupFrontend.warehouseDetails.lookupPageLabels.postcodeLabel")))),
            editPageLabels = Some(
              EditPageLabels(
                title = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.title")),
                line1Label = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.line1Label")),
                line2Label = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.line2Label")),
                line3Label = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.line3Label")),
                townLabel = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.townLabel")),
                postcodeLabel = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.postcodeLabel")),
                organisationLabel = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.organisationLabel")))),
            confirmPageLabels = None,
            countryPickerLabels = None))))
    }
  }

  private def returnContinueUrl(state: AddressLookupState, siteId: String, mode: Mode): String = {
    state match {
      case WarehouseDetails => frontendAppConfig.AddressLookupConfig.WarehouseDetails.offRampUrl(siteId, mode)
      case PackingDetails => frontendAppConfig.AddressLookupConfig.PackingDetails.offRampUrl(siteId, mode)
    }
  }
}
