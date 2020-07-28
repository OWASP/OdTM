
# Ontology-driven threat modelling (OdTM) framework

The ontology-driven threat modelling (OdTM) framework implements an ontological approach
to threat modelling (architectural/design security analysis) of computer systems.

The ontological approach has two general benefits.
Firstly, it enables formalization of security related knowledge 
(architectual components and associated threats and countermeasures)
of different computer system types (architectural domains) in form of domain-specific threat models 
(ontologies).
Secondly, after a system architect has described a computer system in terms 
of a domain-specific model, for example, with DFD (Data Flow Diagram), 
it allows the use of automatic reasoning procedures to build a threat model of the system 
(i.e. figure out relevant threats and countermeasures).

Our approach is based on a base threat model (ontology).
The base model enables creation of various domain-specific threat models 
and their integration with external sources, like attack/vulnerability/weakness enumerations, 
also with catalogues of traditional security patterns.

All the models are implemented as OWL (Web Ontology Language) ontologies 
with Description Logics (DL) as a mathematical background.

Our framework includes a set of software tools for automation of the threat modelling process.
In particular, you can use the [OdTM Server](applications/OdTMServer/) 
with threat modelling tools as an alternative rule engine.

The [applications](applications/) are built with Java, 
the [OWL API](https://github.com/owlcs/owlapi) and [HermiT Reasoner](http://www.hermit-reasoner.com/) libraries.

Some details of the OdTM framework is [here](https://www.researchgate.net/publication/339415212_Security_patterns_based_approach_to_automatically_select_mitigations_in_ontology-driven_threat_modelling)

If you want to refer to OdTM, please cite:
>Brazhuk A. Security patterns based approach to automatically select mitigations in ontology-driven threat modelling // Open Semantic Technologies for Intelligent Systems (OSTIS). – 2020. – №. 4. – С. 267-272


## Base Threat Model

* [OWL file](OdTMBaseThreatModel.owl)
* [Description](docs/BASEMODEL.md)
* [Changelog](docs/BASEMODEL_changelog.md)

## Domain-specific threat models

Common Cloud Computing Threat Model (CCCTM)

* [OWL file](OdTMCCCTM.owl)
* [Description](docs/ODTMCCCTM.md)

## Applications

* [In folder](applications/)
