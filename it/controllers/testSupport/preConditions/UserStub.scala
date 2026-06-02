/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock.*

case class UserStub()(implicit builder: PreconditionBuilder) {

  val identifier         = "some-id"
  val UTR                = "0000001611"
  val sdilNumber         = "XKSDIL000000022"
  val inactiveSdilNumber = "XKSDIL000000026"

  def isAuthorised: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolled: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "0000001611"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isNotAuthorised(reason: String = "MissingBearerToken"): PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(unauthorized().withHeader("WWW-Authenticate", s"""MDTP detail="$reason""""))
    )

    builder
  }

  def isAuthorisedButNotEnrolled: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder
  }

  def isAuthorisedAndEnrolledSDILRef: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$sdilNumber"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder
  }

  def isAuthorisedAndEnrolledWithInactiveAndActiveSdilRefs: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$inactiveSdilNumber"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$sdilNumber"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder
  }

  def isAuthorisedAndEnrolledWithUtrInactiveAndActiveSdilRefs: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "$UTR"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$inactiveSdilNumber"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$sdilNumber"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder
  }

  def isAuthorisedAndEnrolledBoth: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "$UTR"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$sdilNumber"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedWithMissingInternalId: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "credentialRole": "Assistant",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedWithInvalidRole: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "credentialRole": "Assistant",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedButInvalidAffinity: PreconditionBuilder = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "$identifier",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "affinityGroup" : "Agent",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

}
