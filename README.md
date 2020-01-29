
# Ontology-driven threat modelling (OdTM) basic models & repositories

The OdTM framework enables an ontological approach of the architectural security analysis with DFD diagrams.
The approach is based on the base threat model that enables creation of various domain specific threat models. 
Each domain specific threat model holds a set of typical components of some architectural domain, threats and countermeasures (security patterns)
associated with these components. 
A system architect can describe its computer system in terms of a domain specific model with DFD diagram(s). 
Then the automatic reasoning procedures can be used to build a threat model of the system (lists of relevant threats and countermeasures).


## Base Threat Model

The OdTM base threat model as an OWL ontology enables semantic interpretation of DFD diagrams and automatic
building of threat/countermeasure lists by reasoning features. It contains basic concepts and individuals, 
representing components of DFD diagrams, threats, countermeasures, and their properties.

* [the OWL file](OdTMBaseThreatModel.owl)

## OdTM Common Cloud Computing Threat Model (OdTMCCCTM)

OdTM CCCTM is intended to illustrate the building process of a domain-specific threat model.
It has been created from [CCCTM template](https://github.com/nets4geeks/CCCTM_template) 
for the [Microsoft Threat Modelling](https://aka.ms/threatmodelingtool) tool.
Note, this CCCTM implementation (and the XML template too) does not have a mitigation hierarchy.

* [the OWL file](OdTMCCCTM.owl)

