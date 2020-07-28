
# OdTM Server

The OdTM Server implements an ontology-driven threat rule engine.
Our plans include creation of the JSON-based remote API application, 
aimed to perform automatic threat modelling on a given system description 
through a domain-specific threat model.

At the moment you can test the simple console application 
that allows loading the base model and a set of domain-specific models
and test them against semantic interpretations of DFD:

* represented as a text file with ABox axioms, like [this one](OdTMServer/cases/01verysimplecase)



To compile & run the application:

* clone the whole repository [https://github.com/nets4geeks/OdTM.git](https://github.com/nets4geeks/OdTM.git),
go to the 'applications/OdTMServer' folder and run 'mvn compile' there.

* edit 'server.properties' and execute the 'run' script there.

