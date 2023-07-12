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

package controllers.actions

import base.ReturnsTestData._
import base.SpecBase
import models.backend.{Contact, UkAddress}
import models.requests.DataRequest
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{AddASmallProducer, LitresInBands, NormalMode, UserAnswers}
import pages._
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class RequiredUserAnswersSpec extends SpecBase {

  val requiredUserAnswers = application.injector.instanceOf[RequiredUserAnswers]
  val basicRequestWithEmptyAnswers = DataRequest(FakeRequest(),
    "",
    RetrievedSubscription(
      "","","", UkAddress(List.empty, "", None),
      RetrievedActivity(true,true,true,true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None),
    emptyUserAnswers,
    defaultReturnsPeriod
  )

  val basicJourney =List(RequiredPage(PackagedContractPackerPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(HowManyAsAContractPackerPage,
      Some(PreviousPage(PackagedContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    RequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(AddASmallProducerPage,
      Some(PreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]),
    RequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(HowManyBroughtIntoUkPage,
      Some(PreviousPage(BroughtIntoUKPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    RequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(HowManyBroughtIntoTheUKFromSmallProducersPage,
      Some(PreviousPage(BroughtIntoUkFromSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    RequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(HowManyCreditsForExportPage,
      Some(PreviousPage(ClaimCreditsForExportsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    RequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(HowManyCreditsForLostDamagedPage,
      Some(PreviousPage(ClaimCreditsForLostDamagedPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]])
  )
  val importerFalseJourney = List(RequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))

  val smallProducerFalseJourney = List(RequiredPage(OwnBrandsPage, None)(implicitly[Reads[Boolean]]),
    RequiredPage(BrandsPackagedAtOwnSitesPage,
      Some(PreviousPage(OwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))

  val coPackerFalseJourney = List(RequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]))

  "checkYourAnswersRequiredData" - {

    "should return Redirect to start page when user answers is empty and the small producer is true" in {
      val res = requiredUserAnswers.checkYourAnswersRequiredData(Future.successful(Ok("")))(basicRequestWithEmptyAnswers)
      redirectLocation(res).get mustBe controllers.routes.PackagedContractPackerController.onPageLoad(NormalMode).url
    }
    "should return Redirect to start page when user answers is empty and the small producer is false" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(false,true,true,true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.checkYourAnswersRequiredData(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(subscription = subscription))
      redirectLocation(res).get mustBe controllers.routes.OwnBrandsController.onPageLoad(NormalMode).url
    }
    "should allow user to continue if all user answers are filled in and user is NOT newImporter && NOT co packer && NOT small producer" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj("ownBrands" -> false,
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "broughtIntoUkFromSmallProducers" -> false,
          "claimCreditsForExports" -> false,
          "claimCreditsForLostDamaged" -> false),
        List.empty, Map.empty)
      val res = requiredUserAnswers.checkYourAnswersRequiredData(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers))
      status(res) mustBe OK
    }
  }
  "requireData" - {
    "should take a random page and allow user to carry on their action with empty answers" in {
      val res = requiredUserAnswers.requireData(OwnBrandsPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers)
      status(res) mustBe OK
    }
    s"should check for $CheckYourAnswersPage and redirect to start page when answers incomplete but not a nil return and small producer is false" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(false,true,true,true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.requireData(CheckYourAnswersPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(
        subscription = subscription, userAnswers = emptyUserAnswers.copy(data = Json.obj("foo" -> "bar"))))
      redirectLocation(res).get mustBe controllers.routes.OwnBrandsController.onPageLoad(NormalMode).url
    }

    s"should check for $CheckYourAnswersPage and redirect to start page when answers incomplete but not a nil return and small producer is true" in {
      val res = requiredUserAnswers.requireData(CheckYourAnswersPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(
        userAnswers = emptyUserAnswers.copy(data = Json.obj("foo" -> "bar"))))
      redirectLocation(res).get mustBe controllers.routes.PackagedContractPackerController.onPageLoad(NormalMode).url
    }
    s"should check for $CheckYourAnswersPage and redirect to start page when answers data is empty" in {
      val res = requiredUserAnswers.requireData(CheckYourAnswersPage)(Future.successful(Ok("foo")))(basicRequestWithEmptyAnswers)
      redirectLocation(res).get mustBe controllers.routes.PackagedContractPackerController.onPageLoad(NormalMode).url
    }

    s"should check for $CheckYourAnswersPage and allow the user to continue when all answers are complete" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj("ownBrands" -> false,
          "packagedContractPacker" -> true,
        "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
        "exemptionsForSmallProducers" -> false,
        "broughtIntoUK" -> false,
        "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923),
        "broughtIntoUkFromSmallProducers" -> false,
        "claimCreditsForExports" -> false,
        "claimCreditsForLostDamaged" -> false),
        List.empty, Map.empty)
      val res = requiredUserAnswers.requireData(CheckYourAnswersPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers))
      status(res) mustBe OK
    }
  }

  "returnMissingAnswers" - {
    "should return all missing answers in a list when user answers is empty" in {
      implicit val req = basicRequestWithEmptyAnswers
      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.mainRoute)
      res mustBe List(RequiredPage(PackagedContractPackerPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]))
    }
    "should return SOME missing answers when SOME answers are populated" in {
      val someAnswersCompleted = UserAnswers("foo",
        Json.obj("ownBrands" -> false,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923),
          "broughtIntoUkFromSmallProducers" -> false,
          "claimCreditsForLostDamaged" -> false),
        List.empty, Map.empty)

      implicit val req = basicRequestWithEmptyAnswers.copy(userAnswers = someAnswersCompleted)
      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.mainRoute)
      res mustBe List(RequiredPage(PackagedContractPackerPage, None)(implicitly[Reads[Boolean]]),
        RequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]))
    }
  }

  "mainRoute" - {

    "should return all correct answers if user is newImporter && newCopacker && small producer true" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj(
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923)),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, false, contractPacker =false, importer = false,false),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney ++ coPackerFalseJourney ++ importerFalseJourney
    }
    "should return all correct answers if user is newImporter && NOT newCopacker && small producer true" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj(
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923)),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true,true, contractPacker =true,importer = false,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney ++ importerFalseJourney
    }
    "should return all correct answers if user is NOT newImporter && newCopacker && small producer true" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj(
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923)),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, true, contractPacker = false, importer = true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney ++ coPackerFalseJourney
    }
    "should return all correct answers if user is NOT newImporter && NOT newcopacker &&  small producer true" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj(
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923)),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, true, contractPacker = true, importer = true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney
    }
    "should return all correct answers if user is newImporter && newcopacker && small producer false" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj(
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923)),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, true, contractPacker = false, importer = false,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers, subscription = subscription))

      res mustBe smallProducerFalseJourney ++ basicJourney ++ coPackerFalseJourney ++ importerFalseJourney
    }
    "should return all correct answers if user is NOT newImporter && NOT new co packer && NOT small producer" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj("ownBrands" -> false,
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923),
          "broughtIntoUkFromSmallProducers" -> false,
          "claimCreditsForExports" -> false,
          "claimCreditsForLostDamaged" -> false),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, true, contractPacker = true,importer = true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe smallProducerFalseJourney ++ basicJourney
    }
  }
  "packingListReturnChange" - {
    "should be correct if user is NOT a new packer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(true,true,contractPacker = true,true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = requiredUserAnswers.packingListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription))

      res mustBe List.empty
    }
    "should be correct if user is a new packer" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj("ownBrands" -> false,
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923),
          "broughtIntoUkFromSmallProducers" -> false,
          "claimCreditsForExports" -> false,
          "claimCreditsForLostDamaged" -> false),
        List.empty, Map.empty)
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(true,true, contractPacker = false,true,true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = requiredUserAnswers.packingListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription, userAnswers = completedUserAnswers))

      res mustBe coPackerFalseJourney
    }
  }
  "warehouseListReturnChange" - {
    "should be correct if user is NOT a new importer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(true, true, true, importer = true, true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = requiredUserAnswers.warehouseListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription))
      res mustBe List.empty
    }
    "should be correct if user is a new importer" in {
      val completedUserAnswers = UserAnswers("foo",
        Json.obj("ownBrands" -> false,
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 100, "highBand" -> 652),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 259, "highBand" -> 923),
          "broughtIntoUkFromSmallProducers" -> false,
          "claimCreditsForExports" -> false,
          "claimCreditsForLostDamaged" -> false),
        List.empty, Map.empty)

      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(true, true, true, importer = false, true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = requiredUserAnswers.warehouseListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription, userAnswers = completedUserAnswers))

      res mustBe importerFalseJourney
    }
  }
  "smallProducerCheck" - {
    "should be correct if user is NOT a new small producer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, true, true, true, true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.smallProducerCheck(subscription)
      res mustBe List.empty

    }
    "should be correct if user is a small producer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, true, true, true, true),LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.smallProducerCheck(subscription)

      res mustBe smallProducerFalseJourney
    }
  }
}
