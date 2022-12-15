package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class packagedContractPackerFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "packagedContractPacker.error.required"
  val invalidKey = "error.boolean"

  val form = new PackagedContractPackerFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
