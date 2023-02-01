package forms.behaviours

import play.api.data.{Form, FormError}

trait SDILReferenceFieldBehaviours extends FieldBehaviours {

  def invalidRefNumber(form: Form[_],
                       fieldName: String,
                       requiredError: FormError): Unit = {

    "not bind when key is not present at all 2" in {

      val result = form.bind(Map("value" -> "000000381XHSDIL")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind SameRef values" in {

      val result = form.bind(Map(fieldName -> "XHSDIL000000381")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def sameRefNumber(form: Form[_],
                    fieldName: String,
                    requiredError: FormError): Unit = {

    "not bind when key is not present at all 3" in {

      val result = form.bind(Map("value" -> "XKSDIL000000022")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind SameRef values 1" in {

      val result = form.bind(Map(fieldName -> "XKSDIL000000022")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def existingRefNumber(form: Form[_],
                        fieldName: String,
                        requiredError: FormError): Unit = {

    "not bind when key is not present at all 4" in {

      val result = form.bind(Map("value" -> "XHSDIL000000381")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind SameRef values 2" in {

      val result = form.bind(Map(fieldName -> "XHSDIL000000381")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def largeRefNumber(form: Form[_],
                     fieldName: String,
                     requiredError: FormError): Unit = {

    "not bind when key is not present at all 5" in {

      val result = form.bind(Map("value" -> "XGSDIL000000437")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind LargeRef values" in {

      val result = form.bind(Map(fieldName -> "XGSDIL000000437")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def userRefNumber(form: Form[_],
                    fieldName: String,
                    requiredError: FormError): Unit = {

    "not bind when key is not present at all 6" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind User Reference Number values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

}
