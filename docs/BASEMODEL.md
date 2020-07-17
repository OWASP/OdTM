
# Base Threat Model

The OdTM base threat model is an OWL ontology.
It enables semantic interpretation of DFD diagrams and automatic building of threat/countermeasure lists by automatic reasoning procedures.
It contains basic concepts and individuals, representing components of DFD diagrams, threats, countermeasures, and their properties.

* [OWL file](../OdTMBaseThreatModel.owl)

## Structure

Stencils and their properties:

![structure 1](structure1.png)

DL "axioms" that represent this picture:

![axioms 1](lst1.png)

Threats and countermeasures:

![structure 2](structure2.png)

The sentences for this part of the ontology:

![axioms 2](lst2.png)

## Example


A simple threat model looks like:

```
Template1 ≡ DataFlow ∩ ∃hasSource.Process ∩ ∃hasTarget.Process ∩ ∃crosses.TrustLineBoundary
ContextSecurityPattern(pattern1)
Template1 ⊆ ∃isProtectedBy.{pattern1}
Threat(threat2)
Template1 ⊆ ∃isAffectedBy.{threat2}
```

A simple DFD diagram looks like:

![Diagram example](dfd_example.png)

Using DL it possible to define this picture as

```
Process(pr1)
Process(pr2)
DataFlow(flow)
hasSource(flow, pr1)
hasTarget(flow, pr2)
TrustLineBoundary(line)
crosses(flow, line)
TrustBorderBoundary(box)
includes(box, pr1)
```

After putting all the DL items to OWL and combining with the base model and reasoning in Protege it might look like:

![reasoning example](protege_example.png)


