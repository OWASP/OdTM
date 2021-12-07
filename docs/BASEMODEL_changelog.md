
# Changelog of Base Threat Model

## Version 0.60 (November 2021)

* added the 'ThreatRestriction' class 
* added the 'satisfiesThreatRestriction' property
* added the 'ClassifiedStencil' class
* added the 'refToATTCK' 'refToCAPEC', 'refToCWE', 'refToCVE' properties
* added inverse 'isRefToThreat','isRefToATTCK', 'isRefToCAPEC', 'isRefToCWE' properties
* added property chains refToCAPECresoned, refToCWEreasoned & refToCVEreasoned
* added the 'refToTacticProperty' property
* added the 'refToEnum' superproperty

## Version 0.55 (February 2021)

* added the 'isAffectedByTargets' property
* added hand made reasons of the STRIDE threats for targets
* a bit of integration with the security pattern (SP) schema

## Version 0.50 (August 2020)

* added a classification approach based on the 'ClassifiedXXX' classes
* added a conception of protocol profiles with the HTTP and DNS examples
* added STRIDE-based generic threats (found in the Threat Dragon sources)
* added a simple testing tool and a set of tests
* added some data properties to describe threats

## Version 0.02 (March 2020)

* added a semantic interpretation of the DFD graphical notation
* added an approach to automatically reason threats and countermeasures
* added mapping of threats to data flows with flow templates
* added STRIDE and CIA labels

