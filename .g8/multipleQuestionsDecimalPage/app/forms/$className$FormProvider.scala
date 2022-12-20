package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.$className$

class $className$FormProvider @Inject() extends Mappings {

   def apply(): Form[$className$] = Form(
     mapping(
      "$field1Name$" -> long(
        "$className;format="decap"$.error.$field1Name$.required",
                    "$className;format="decap"$.error.$field1Name$.negative",
                    "$className;format="decap"$.error.$field1Name$.nonNumeric",
                    "$className;format="decap"$.error.$field1Name$.wholeNumber",
  "$className;format="decap"$.error.$field1Name$.outOfMaxVal")
  .verifying(maximumValueNotEqual($field1Maximum$, "$className;format="decap"$.error.$field1Name$.outOfMaxVal")),
        "$field2Name$" -> long(
        "$className;format="decap"$.error.$field2Name$.required",
                    "$className;format="decap"$.error.$field2Name$.negative",
                    "$className;format="decap"$.error.$field2Name$.nonNumeric",
                    "$className;format="decap"$.error.$field2Name$.wholeNumber",
  "$className;format="decap"$.error.$field2Name$.outOfMaxVal")
  .verifying(maximumValueNotEqual($field2Maximum$, "$className;format="decap"$.error.$field2Name$.outOfMaxVal"))
    )($className$.apply)($className$.unapply)
   )
 }
