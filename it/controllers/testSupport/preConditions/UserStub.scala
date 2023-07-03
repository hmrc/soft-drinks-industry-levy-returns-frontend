package controllers.testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._

case class UserStub
()
(implicit builder: PreconditionBuilder) {

  val identifier = "some-id"
  val UTR = "0000001611"
  val sdilNumber = "XKSDIL000000022"

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
        .willReturn(unauthorized().withHeader("WWW-Authenticate", s"""MDTP detail="$reason"""")))

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
