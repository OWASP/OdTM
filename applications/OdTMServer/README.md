
# OdTM Server

The OdTM Server implements an ontology-driven threat rule engine.
Our plans include creation of a JSON-based remote API service, 
aimed to perform automatic threat modelling on a given system description 
through a domain-specific threat model.

At the moment you can try the simple console application 
that allows to load the base model and a set of domain-specific models
and test them against a semantic interpretation of DFD.
You can represent source data as:

* a JSON file, compatible with the [OWASP Threat Dragon](https://owasp.org/www-project-threat-dragon/) tool.
Creating a diagram with GUI of Threat Dragon, like [this one](cases/tdexample_acctp.json),
and processing by the OdTMServer application, you can get a threat model like [this](cases/tdexample_acctp_modelled.json). 

* a text file with ABox axioms, like [this one](cases/01verysimplecase). 
You can get a simple console output, used for the test purposes.

To compile & run the application:

* clone the whole OdTM repository, go to the 'applications/OdTMServer' folder and run 'mvn compile' there.

* edit 'server_xxx.properties' and execute the 'server_xxx.run' script there.

