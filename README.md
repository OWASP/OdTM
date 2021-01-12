
# OWASP Ontology-driven threat modelling (OdTM) framework

Ontology-driven Threat Modelling (OdTM) framework is a set of means for implementation 
of an ontological approach into automatic threat modelling of computer systems.

It is an [OWASP Incubator Project](https://owasp.org/www-project-ontology-driven-threat-modeling-framework/).

The ontological approach, provided by the OdTM framework, has two general benefits.
Firstly, it enables formalization of security related knowledge 
(architectual components and associated threats and countermeasures)
of different computer system types (architectural domains) in form of domain-specific threat models 
(ontologies).
Secondly, after a system architect has described a computer system in terms 
of a domain-specific model, for example, with DFD (Data Flow Diagram), 
it allows the use of automatic reasoning procedures to build a threat model of the system 
(i.e. figure out relevant threats and countermeasures).

Our approach is based on a [base threat model (ontology)](docs/BASEMODEL.md).
The base model enables creation of various domain-specific threat models 
and their integration with external sources, like attack/vulnerability/weakness enumerations, 
also with catalogues of traditional security patterns.

We also have an implementation of the [Academic Cloud Computing Threat Patterns (ACCTP)](docs/ODTMACCTP.md) model
as a domain-specific threat model.

All the models are implemented as OWL (Web Ontology Language) ontologies 
with Description Logics (DLs) as a mathematical background.

The framework includes a set of software tools for automation of the threat modelling process.
In particular, we are working on the [OdTM Server](applications/OdTMServer/) 
as an alternative rule engine for threat modelling tools.

The applications are built with Java, 
the [OWL API](https://github.com/owlcs/owlapi) 
and [HermiT Reasoner](http://www.hermit-reasoner.com/) libraries.

A basic introduction to the OdTM framework is [here](https://www.researchgate.net/publication/339415212_Security_patterns_based_approach_to_automatically_select_mitigations_in_ontology-driven_threat_modelling)

If you want to refer to OdTM, please cite:
>Brazhuk A. Security patterns based approach to automatically select mitigations in ontology-driven threat modelling // Open Semantic Technologies for Intelligent Systems (OSTIS). – 2020. – №. 4. – С. 267-272

Note, [the OWASP repository of the project is here](https://github.com/OWASP/OdTM)
(please send your contributions, issues etc. to this one). 
And a [mirror is here](https://github.com/nets4geeks/OdTM).


## Base Threat Model

* [OWL file](OdTMBaseThreatModel.owl)
* [Description](docs/BASEMODEL.md)
* [Changelog](docs/BASEMODEL_changelog.md)

## Domain-specific threat models

* [Academic Cloud Computing Threat Patterns (ACCTP) model](docs/ODTMACCTP.md)
* [Common Cloud Computing Threat Model or CCCTM](docs/ODTMCCCTM.md) (obsolete)

## Applications

* [OdTM Server](applications/OdTMServer/)
* [Check application](applications/checkApplication/)

