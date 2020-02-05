

# Base Threat Model

The OdTM base threat model is an OWL ontology.
It enables semantic interpretation of DFD diagrams and automatic building of threat/countermeasure lists by reasoning features.
It contains basic concepts and individuals, representing components of DFD diagrams, threats, countermeasures, and their properties.

* [OWL file](../OdTMBaseThreatModel.owl)

## Example

A simple DFD diagram looks like:

![Diagram example](dfd_example.png)

Using DL it possible to define this as

```
Process(pr1)
Process(pr2)
DataFlow(f low)
hasSource(f low, pr1)
hasTarget(f low, pr2)
TrustLineBoundary(line)
crosses(f low, line)
TrustBorderBoundary(box)
includes(box, pr1)
```
