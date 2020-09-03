# ChangeLog

### v0.1.12-SNAPSHOT - September 2nd, 2020
- Upgrade gradle 5.1.1 -> 5.3
- Fix tests in circle ci

### v0.1.11 - April 20th, 2019
 - Rename `GdmcValidateBuildscriptDepsTask` -> `GdmcValidateDepsTask` and take a collection of `Dependencies` as input.
 - Upgrade gradle 4.9 -> 5.1.1
 - Use slf4j under the hood for gdmc logging

### v0.1.10 - July 29th, 2018
 - Upgrade gradle 4.4 -> 4.9
 - Upgrade deployable 0.1.12 -> 0.2.0
 - remove support for old `maven` plugin

### v0.1.9 - June 27th, 2018
 - Introduce changelog
 - fix bug in `validateSelf` task when the project is referenced with an `inheritVersion` in gdmc
