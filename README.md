
# Ontology-driven threat modelling (OdTM) framework

The OdTM framework implements an ontological approach to the threat modelling (architectural/design security analysis) of computer systems.
The approach is based on a base threat model, domain-specific threat models, and context security patterns and threats.

The base threat model enables creation of various domain-specific threat models. 
Each domain-specific threat model holds a set of typical components of some type of computer systems (architectural domain), 
also threats and countermeasures, associated with these components.

A system architect can describe its computer system in terms of a domain-specific model, for example, with DFD (Data Flow Diagram).
Then automatic reasoning procedures are used to build a threat model of the system (lists of relevant threats and countermeasures).

Context security patterns and threats enable integration of domain-specific threat models with different external sources, 
like attack/vulnerability/weakness enumerations, also with [catalogues of traditional security patterns](https://github.com/nets4geeks/SPCatalogMaker).

All the models are implemented as OWL (Web Ontology Language) ontologies with Description Logics (DL) as a mathematical background.

A description is [here](https://www.researchgate.net/publication/339415212_Security_patterns_based_approach_to_automatically_select_mitigations_in_ontology-driven_threat_modelling)

If you want to refer to the OdTM framework, please cite:
>Brazhuk A. Security patterns based approach to automatically select mitigations in ontology-driven threat modelling // Open Semantic Technologies for Intelligent Systems (OSTIS). – 2020. – №. 4. – С. 267-272


## Base Threat Model

* [OWL file](OdTMBaseThreatModel.owl)
* [Description](docs/BASEMODEL.md)


## Domain-specific threat models

Common Cloud Computing Threat Model (CCCTM)

* [OWL file](OdTMCCCTM.owl)
* [Description](docs/ODTMCCCTM.md)


