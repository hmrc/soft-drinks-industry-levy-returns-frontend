package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.BrandsPackagedAtOwnSites

class BrandsPackagedAtOwnSitesFormProvider @Inject() extends Mappings {

   def apply(): Form[BrandsPackagedAtOwnSites] = Form(
     mapping(
      "lowBandLitres" -> long(
        "brandsPackagedAtOwnSites.error.lowBandLitres.required",
                    "brandsPackagedAtOwnSites.error.lowBandLitres.negative",
                    "brandsPackagedAtOwnSites.error.lowBandLitres.nonNumeric",
                    "brandsPackagedAtOwnSites.error.lowBandLitres.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "brandsPackagedAtOwnSites.error.lowBandLitres.outOfMaxVal")),
        "highBandLitres" -> long(
        "brandsPackagedAtOwnSites.error.highBandLitres.required",
                    "brandsPackagedAtOwnSites.error.highBandLitres.negative",
                    "brandsPackagedAtOwnSites.error.highBandLitres.nonNumeric",
                    "brandsPackagedAtOwnSites.error.highBandLitres.wholeNumber")
  .verifying(maximumValueNotEqual(100000000000000L, "brandsPackagedAtOwnSites.error.highBandLitres.outOfMaxVal"))
    )(BrandsPackagedAtOwnSites.apply)(BrandsPackagedAtOwnSites.unapply)
   )
 }
