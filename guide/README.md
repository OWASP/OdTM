
# Threat modeling guide

This guide describes an approach to the threat modeling of cloud computer systems,
based on the [Ontology-driven threat modelling (OdTM)](https://owasp.org/www-project-ontology-driven-threat-modeling-framework/) framework
and [Academic Cloud Computing Threat Patterns (ACCTP)](https://nets4geeks.github.io/acctp/) catalog.

The OdTM framework is based on Data Flow Diagrams (DFDs), used to depict information flows between system components 
and external entities for the security purposes.
To get into the cloud security field we use the ACCTP model as an ontological domain-specific threat model, 
built from the ACCTP catalog. The last is a catalog of threat patterns for cloud computing.

The proposed means allow creation of general threat models (lists) for common cloud configurations.
Such a list is used to discuss the security issues of cloud components and figure out the right 
security design decisions by a development team.

The guide can be useful for non experts in the security field, who plan to start using the cloud technologies 
for their applications.

## For System Architects (Developers)

The guide for system architects and developers is implemented as a set of laboratory works,
which can be easy adopted for education purposes.

You can use the third-party desktop threat modeling tool ([OWASP Threat Dragon](https://owasp.org/www-project-threat-dragon/)) 
to analyse proposed threat models, apply mitigations, and generate reports.
That tool was used to create a set of diagrams for this guide.
Then the diagrams have been processed by the [OdTMServer](../applications/OdTMServer) console application to create ontology-driven threat lists.

To create own ontology-driven threat models for cloud computing you should read [this instruction](instruction.md).

* Lab1: [Introduction to the ontology-driven threat modeling](lab1_introduction.md)
* Lab2: Modeling of the cloud architectural threats
* Lab3: Modeling of the compliance challenges of cloud systems
* Lab4: Modeling of the privacy challenges of cloud systems
* Lab5: Modeling of the IaaS clouds
* Lab6: Modeling of the PaaS clouds
* Instruction: [How to create ontology-driven threat models](instruction.md)

## For Knowledge Engineers

coming soon ...

