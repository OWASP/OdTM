
# OdTM Server

OdTM Server implements an ontology-driven alternative to rule engines for threat modelling tools.
It is going to be JSON-based remote API application, performing automatic threat modelling
on given domain-specific models and use cases.

Here very first steps have been made. It allows to load the base model,
a set of domain models and test them against semantic interpretations of DFD, like [this one](OdTMServer/cases/01verysimplecase).

To compile & run the application:

-clone the whole repository [https://github.com/nets4geeks/OdTM.git](https://github.com/nets4geeks/OdTM.git),
go to the 'applications/OdTMServer' folder and run 'mvn compile' there.

-edit 'server.properties' and execute the 'run' script there.




