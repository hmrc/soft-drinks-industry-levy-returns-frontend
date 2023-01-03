#!/bin/bash

echo ""
echo "Applying migration smallProducerDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /smallProducerDetails                        controllers.smallProducerDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /smallProducerDetails                        controllers.smallProducerDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changesmallProducerDetails                  controllers.smallProducerDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changesmallProducerDetails                  controllers.smallProducerDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "smallProducerDetails.title = smallProducerDetails" >> ../conf/messages.en
echo "smallProducerDetails.heading = smallProducerDetails" >> ../conf/messages.en
echo "smallProducerDetails.checkYourAnswersLabel = smallProducerDetails" >> ../conf/messages.en
echo "smallProducerDetails.error.required = Select yes if smallProducerDetails" >> ../conf/messages.en
echo "smallProducerDetails.change.hidden = smallProducerDetails" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarysmallProducerDetailsUserAnswersEntry: Arbitrary[(smallProducerDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[smallProducerDetailsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarysmallProducerDetailsPage: Arbitrary[smallProducerDetailsPage.type] =";\
    print "    Arbitrary(smallProducerDetailsPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(smallProducerDetailsPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration smallProducerDetails completed"
