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

package utilitlies

import models.{Amounts, SdilCalculation}
import repositories.{SDILSessionCache, SDILSessionKeys}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CacheHelper @Inject()(
                           sessionCache :SDILSessionCache
                           )(implicit ec: ExecutionContext){

  def cacheAmounts(sdilEnrolment: String, amounts: Amounts) = {
    sessionCache.save(sdilEnrolment, SDILSessionKeys.AMOUNTS, amounts).map {
      case result if !result.id.isEmpty => result
      case _ => throw new RuntimeException(s"Failed to save amounts in session cache for $sdilEnrolment")
    }
  }

  def cacheRowAmounts(sdilEnrolment: String, rowCalculations: Map[String, SdilCalculation]) = {
    sessionCache.save(sdilEnrolment, SDILSessionKeys.ROW_CALCULATIONS, rowCalculations).map {
      case result if !result.id.isEmpty => result
      case _ => throw new RuntimeException(s"Failed to save row calculations in session cache for $sdilEnrolment")
    }
  }

}
