
# Ontology-driven threat modelling (OdTM) framework

The OdTM framework implements an ontological approach to the threat modelling (architectural/design security analysis) of computer systems.
The approach is based on a base threat model, domain-specific threat models, and context security patterns and threats.

The base threat model enables creation of various domain-specific threat models. 
Each domain-specific threat model holds a set of typical components of some type of computer systems (architectural domain), 
also threats and countermeasures, associated with these components.

A system architect can describe its computer system in terms of a domain-specific model, for example, with DFD (Data Flow Diagram).
Then automatic reasoning procedures are used to build a threat model of the system (lists of relevant threats and countermeasures).

Context security patterns and threats enable integration of domain-specific threat models with different external sources, 
like attack/vulnerability/weakness enumerations, also with catalogues of traditional security patterns.

All the models are implemented as OWL (Web Ontology Language) ontologies with Description Logics (DL) as a mathematical background.

A description is [here](https://www.researchgate.net/publication/338999512_Framework_for_ontology-driven_threat_modelling_of_modern_computer_systems)

If you want to refer to the OdTM framework, please cite:
>Brazhuk A., Olizarovich E. Framework for ontology-driven threat modelling of modern computer systems //International Journal of Open Information Technologies. – 2020. – Vol. 8. – No. 2. – С. 14-20.



## Base Threat Model

* [OWL file](OdTMBaseThreatModel.owl)
* [Description](docs/BASEMODEL.md)


## Domain-specific threat models

Common Cloud Computing Threat Model (CCCTM)

* [OWL file](OdTMCCCTM.owl)
* [Description](docs/ODTMCCCTM.md)


