
# OdTM Server

The OdTM Server implements an ontology-driven threat rule engine.
Our plans include creation of a JSON-based remote API service, 
aimed to perform automatic threat modelling on a given system description 
through a domain-specific threat model.

At the moment you can try the simple console application 
that allows to load the base model and a set of domain-specific models
and test them against a semantic interpretation of DFD.
You can represent source data as:

* a JSON file of the OWASP [Threat Dragon](https://owasp.org/www-project-threat-dragon/) model.
You can create a diagram with Threat Dragon like [this one](cases/tdexample.json).
And process the model with the OdTMServer console application and get a threat model like [this](cases/tdexample_modelled.json). 

* a text file with ABox axioms, like [this one](cases/01verysimplecase)



To compile & run the application:

* clone the whole repository [https://github.com/nets4geeks/OdTM.git](https://github.com/nets4geeks/OdTM.git),
go to the 'applications/OdTMServer' folder and run 'mvn compile' there.

* edit 'server.properties' and execute the 'run' script there.

