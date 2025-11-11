
# Soft Drinks Industry Levy Returns Frontend

## About
The Soft Drinks Industry Levy (SDIL) digital service is split into a number of different microservices all serving specific functions which are listed below:

**Liability tool** - Standalone frontend service that is used to check a company's liability in regards to the levy.
**Registration Frontend** - The initial subscription registration service.
**Returns Frontend** - The returns journey frontend for the service.
**Variations Frontend** - Service to submit variations on registration and returns functionalities.
**Accounts Frontend** - Dashboard functionality service.
**Backend** - The service that the frontend uses to call HOD APIs to retrieve and send information relating to business information and subscribing to the levy.
**Stub** - Microservice that is used to mimic the DES APIs when running services locally or in the development and staging environments.
For details about the sugar tax see [the GOV.UK guidance](https://www.gov.uk/guidance/soft-drinks-industry-levy)

## Feature switches
**addressLookupFrontendTest.enabled** switches between  our stub endpoints and real ALF
**defaultReturnTest.enabled** defaults return for testing purposes so users do not have to navigate from dashboard
**balanceAll.enabled** use balance history / balance in final calculation
## Running from source
Clone the repository using SSH:

`git@github.com:hmrc/soft-drinks-industry-levy-returns-frontend.git`

If you need to setup SSH, see [the github guide to setting up SSH](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/)

Run the code from source using

`./run.sh`

Run other services required for running this service via the service manager. (*You need to be on the VPN*)
`sm2 --start SDIL_ALL`

To start the service on local use :
`sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

lowerBandCostPerLitre = "0.18"
higherBandCostPerLitre = "0.24"
lowerBandCostPerLitrePostApril2025 = "0.194"
higherBandCostPerLitrePostApril2025 = "0.259"



## Adding 2026 rates
1. Make sure you have your NEW_LOWER_BAND_VALUE and NEW_HIGHER_BAND_VALUE band rate values for 2026 tax year
2. Do a global find for `(2025 to 2025)` in Scala files and replace with `(2025 to 2026)`
3. Add lowerBandCostPerLitrePostApril2026 with NEW_LOWER_BAND_VALUE in application.conf 
4. Add higherBandCostPerLitrePostApril2026 with NEW_HIGHER_BAND_VALUE in application.conf
5. Add `Year2026 -> BandRates(lowerBandCostPerLitrePostApril2026, higherBandCostPerLitrePostApril2026)` to `LevyCalculator.bandRatesByTaxYear` function in `LevyCalculator.scala` 
6. Add `2026 -> Year2026` to `TaxYear.fromYear` function in `LevyCalculator.scala`
7. Add `2026 -> BigDecimal(NEW_LOWER_BAND_VALUE)` to `lowerBandCostPerLitreMap` function in `TaxRateUtil.scala`
8. Add `2026 -> BigDecimal(NEW_HIGHER_BAND_VALUE)` to `higherBandCostPerLitreMap` function in `TaxRateUtil.scala`
9. Test the behaviour out and make sure it works!
10. When happy with the changes, update these instructions for 'Adding 2027 rates'