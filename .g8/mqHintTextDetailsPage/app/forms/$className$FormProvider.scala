package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.$className$

class $className$FormProvider @Inject() extends Mappings {

   def apply(): Form[$className$] = Form(
     mapping(
      "$field1Name$" -> int(
        "$className;format="decap"$.error.required", "$className;format="decap"$.error.nonNumeric")
  .verifying(inRange($minimum$, $maximum$, "$className;format="decap"$.error.outOfRange")),
        "$field2Name$" -> int(
        "$className;format="decap"$.error.required", "$className;format="decap"$.error.nonNumeric")
  .verifying(inRange($minimum$, $maximum$, "$className;format="decap"$.error.outOfRange"))
    )($className$.apply)($className$.unapply)
   )
 }
