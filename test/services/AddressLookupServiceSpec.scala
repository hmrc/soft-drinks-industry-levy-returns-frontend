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

import base.ReturnsTestData._
import base.SpecBase
import connectors.AddressLookupConnector
import controllers.routes
import models.NormalMode
import models.alf.AlfAddress
import models.alf.init._
import models.backend.{ Site, UkAddress }
import models.core.ErrorModel
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{ mock, when }
import play.api.test.{ DefaultAwaitTimeout, FakeRequest, FutureAwaits }

import scala.concurrent.Future

class AddressLookupServiceSpec extends SpecBase with FutureAwaits with DefaultAwaitTimeout {

  val mockSdilConnector = mock[AddressLookupConnector]
  val service = new AddressLookupService(mockSdilConnector, frontendAppConfig)

  "getAddress" - {
    "return an address when Connector returns success" in {
      when(mockSdilConnector.getAddress("123456789")(hc, implicitly)).thenReturn(Future.successful(Right(customerAddressMax)))

      val res = service.getAddress("123456789")

      whenReady(res) { result =>
        result mustBe customerAddressMax
      }
    }
    "return an exception when Connector returns error" in {
      when(mockSdilConnector.getAddress("123456789")(hc, implicitly)).thenReturn(Future.successful(Left(ErrorModel(1, "foo"))))

      val res = intercept[Exception](await(service.getAddress("123456789")))
      res.getMessage mustBe "Error returned from ALF for 123456789 1 foo for None"
    }
  }

  "addAddressUserAnswers" - {
    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend where sdilId DOESN'T exist" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None))
      val addedWarehouse = Map(
        "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), None, Some(organisation), None))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMax.address,
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap),
        siteId = sdilId,
        alfId = alfId)

      res.warehouseList mustBe addedWarehouse
    }
    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend where sdilId DOES exist" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map(sdilId ->
        Site(UkAddress(List("foo", "bar"), "wizz"), None, Some("super cola"), None))
      val updatedWarehouseMap = Map(sdilId ->
        Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMax.address,
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap),
        siteId = sdilId,
        alfId = alfId)

      res.warehouseList mustBe updatedWarehouseMap
    }

    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend with missing address lines" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None))
      val addedWarehouseMissingLines = Map(
        "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2), postcode, alfId = Some(alfId)), None, Some(organisation), None))
      val customerAddressMissingLines: AlfAddress =
        AlfAddress(
          Some(organisation),
          List(addressLine1, addressLine2),
          Some(postcode),
          Some(countryCode))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap),
        siteId = sdilId,
        alfId = alfId)

      res.warehouseList mustBe addedWarehouseMissingLines
    }

    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend with full address lines" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = AlfAddress(
          Some(organisation),
          List(addressLine1, addressLine2, addressLine3, addressLine4),
          Some(postcode),
          Some(countryCode)),
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap),
        alfId = alfId,
        siteId = sdilId)

      res.warehouseList mustBe Map(
        "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), None, Some(organisation), None))
    }

    s"add to the cache the address of a $PackingDetails when a user returns from address lookup frontend with missing address lines" in {
      val addressLookupState = PackingDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val packingMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None))
      val addedPackingSiteMissingLines = Map(
        "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2), postcode, alfId = Some(alfId)), None, Some(organisation), None))
      val customerAddressMissingLines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2),
        Some(postcode),
        Some(countryCode))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswers.copy(packagingSiteList = packingMap),
        alfId = alfId,
        siteId = sdilId)

      res.packagingSiteList mustBe addedPackingSiteMissingLines
    }

    s"add to the cache the address of a $PackingDetails when a user returns from address lookup frontend with full address lines and sdilRef DOESN'T exist" in {
      val addressLookupState = PackingDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val packingMap = Map(sdilId -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None))
      val addedPackingSite = Map(sdilId ->
        Site(UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), None, Some(organisation), None))

      val customerAddressMissingLines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2, addressLine3, addressLine4),
        Some(postcode),
        Some(countryCode))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswers.copy(packagingSiteList = packingMap),
        alfId = alfId,
        siteId = sdilId)

      res.packagingSiteList mustBe addedPackingSite
    }
    s"add to the cache the address of a $PackingDetails when a user returns from address lookup frontend with full address lines and sdilRef DOES exist" in {
      val addressLookupState = PackingDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val packingMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None))
      val addedPackingSite = Map(
        "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), None, Some(organisation), None))

      val customerAddressMissingLines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2, addressLine3, addressLine4),
        Some(postcode),
        Some(countryCode))

      val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswers.copy(packagingSiteList = packingMap),
        alfId = alfId,
        siteId = sdilId)

      res.packagingSiteList mustBe addedPackingSite
    }
    "don't add to userAnswers when no details are added in alf and throw exception" in {
      val addressLookupState = WarehouseDetails
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), None, Some("super cola"), None))
      val customerAddressMissingLinesAndName: AlfAddress = AlfAddress(
        None,
        List(),
        None,
        None)

      lazy val res = service.addAddressUserAnswers(
        addressLookupState = addressLookupState,
        address = customerAddressMissingLinesAndName,
        userAnswers = emptyUserAnswers.copy(warehouseList = warehouseMap),
        alfId = "foo",
        siteId = "bar")

      val errorMessage = "Not Found (Alf has returned an empty address and organisation name)"

      val result: String = intercept[Exception](res).getMessage

      result mustEqual errorMessage

    }
  }
  "initJourney" - {
    "should return response from connector" in {
      val journeyConfig = JourneyConfig(1, JourneyOptions(""), None, None)
      when(mockSdilConnector.initJourney(ArgumentMatchers.eq(journeyConfig))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right("foo")))

      whenReady(service.initJourney(journeyConfig)) {
        res => res mustBe Right("foo")
      }
    }
  }

  "initJourneyAndReturnOnRampUrl" - {
    s"should return Successful future when connector returns success for $PackingDetails" in {
      val sdilId = "Foobar"
      val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
        version = frontendAppConfig.AddressLookupConfig.version,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/packing-site-details/$sdilId",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703bar"),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(SelectPageConfig(
            proposalListLimit = Some(10),
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
        labels = Some(
          JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("Soft Drinks Industry Levy"),
                phaseBannerHtml = None)),
              selectPageLabels = None,
              lookupPageLabels = Some(
                LookupPageLabels(
                  title = Some("Find UK packaging site address"),
                  heading = Some("Find UK packaging site address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK packaging site address"),
                  heading = Some("Enter the UK packaging site address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Packaging site name (optional)"))),
              confirmPageLabels = None,
              countryPickerLabels = None)))),
        requestedVersion = None)

      when(mockSdilConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right("foo")))
      whenReady(service.initJourneyAndReturnOnRampUrl(PackingDetails, sdilId)(implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))) {
        res => res mustBe "foo"
      }
    }
    s"should return Successful future when connector returns success for $WarehouseDetails" in {
      val sdilId = "Foobar"
      val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/secondary-warehouses/$sdilId",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703bar"),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(SelectPageConfig(
            proposalListLimit = Some(10),
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
        labels = Some(
          JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("Soft Drinks Industry Levy"),
                phaseBannerHtml = None)),
              selectPageLabels = None,
              lookupPageLabels = Some(
                LookupPageLabels(
                  title = Some("Find UK warehouse address"),
                  heading = Some("Find UK warehouse address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK warehouse address"),
                  heading = Some("Enter the UK warehouse address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Trading name (optional)"))),
              confirmPageLabels = None,
              countryPickerLabels = None)))),
        requestedVersion = None)

      when(mockSdilConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right("foo")))
      whenReady(service.initJourneyAndReturnOnRampUrl(WarehouseDetails, sdilId)(implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))) {
        res => res mustBe "foo"
      }
    }

    "should return Exception if connector returns left" in {
      when(mockSdilConnector.initJourney(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Left(ErrorModel(1, "foo"))))
      val res = intercept[Exception](await(service.initJourneyAndReturnOnRampUrl(PackingDetails)(implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))))
      res.getMessage mustBe "Failed to init ALF foo with status 1 for None"
    }
  }

  "createJourneyConfig" - {
    s"should return a journey config for $WarehouseDetails" in {
      val request = FakeRequest("foo", "bar")
      val exampleSdilIdWeGenerate: String = "wizz"
      val res = service.createJourneyConfig(WarehouseDetails, exampleSdilIdWeGenerate, NormalMode)(request, implicitly)
      val expected = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/secondary-warehouses/$exampleSdilIdWeGenerate",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703${request.uri}"),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(SelectPageConfig(
            proposalListLimit = Some(10),
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
        labels = Some(
          JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("Soft Drinks Industry Levy"),
                phaseBannerHtml = None)),
              selectPageLabels = None,
              lookupPageLabels = Some(
                LookupPageLabels(
                  title = Some("Find UK warehouse address"),
                  heading = Some("Find UK warehouse address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK warehouse address"),
                  heading = Some("Enter the UK warehouse address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Trading name (optional)"))),
              confirmPageLabels = None,
              countryPickerLabels = None)))),
        requestedVersion = None)

      res mustBe expected
    }

    s"should return a journey config for $PackingDetails" in {
      val request = FakeRequest("foo", "bar")
      val exampleSdilIdWeGenerate: String = "wizz"
      val res = service.createJourneyConfig(PackingDetails, exampleSdilIdWeGenerate)(request, implicitly)
      val expected = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/packing-site-details/$exampleSdilIdWeGenerate",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703${request.uri}"),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(SelectPageConfig(
            proposalListLimit = Some(10),
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
        labels = Some(
          JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("Soft Drinks Industry Levy"),
                phaseBannerHtml = None)),
              selectPageLabels = None,
              lookupPageLabels = Some(
                LookupPageLabels(
                  title = Some("Find UK packaging site address"),
                  heading = Some("Find UK packaging site address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK packaging site address"),
                  heading = Some("Enter the UK packaging site address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Packaging site name (optional)"))),
              confirmPageLabels = None,
              countryPickerLabels = None)))),
        requestedVersion = None)

      res mustBe expected
    }
  }
}