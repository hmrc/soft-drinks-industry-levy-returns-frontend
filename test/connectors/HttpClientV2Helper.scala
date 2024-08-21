/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads }

import java.net.URL
import scala.concurrent.ExecutionContext

trait HttpClientV2Helper extends SpecBase with MockitoSugar with ScalaFutures {

  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val requestBuilder: RequestBuilder = mock[RequestBuilder]
  when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.post(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.delete(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.put(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(requestBuilder.withBody(any[JsValue])(any(), any(), any())).thenReturn(requestBuilder)

  def requestBuilderExecute[A] = requestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])

}
