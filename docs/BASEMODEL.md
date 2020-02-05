

# Base Threat Model

The OdTM base threat model is an OWL ontology.
It enables semantic interpretation of DFD diagrams and automatic building of threat/countermeasure lists by reasoning features.
It contains basic concepts and individuals, representing components of DFD diagrams, threats, countermeasures, and their properties.

* [OWL file](../OdTMBaseThreatModel.owl)

## Structure

Stencils and their properties:

![structure 1](structure1.png)

Threats and countermeasures:

![structure 2](structure2.png)

## Example

A simple DFD diagram looks like:

![Diagram example](dfd_example.png)

Using DL it possible to define this as

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
