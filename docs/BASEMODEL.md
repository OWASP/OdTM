
# Base Threat Model

The OdTM base threat model is an ontology, implemented with OWL (Web Ontology Language).
It enables semantic interpretation of DFDs (Data Flow Diagrams) and automatic building of threat/countermeasure lists.
It contains concepts and instances to represent components of diagrams, threats, countermeasures, and their properties.
Also, it provides a basic STRIDE approach to the threat modelling and labelling of security things with different tags.

* [OWL file](../OdTMBaseThreatModel.owl)

Common ontology description uses separation of axioms to the ABox (individuals and their relations) 
and TBox (concepts and properties) parts.
Also, it is supposed that automatic reasoning procedures exist, 
which allow to get extra facts (inferred axioms) from an ontology.

## Semantic interpretation of DFDs

Semantic interpretation means that a DFD can be represented as a sequence of facts (axioms) about its items.

Here is an informal graphical representation of concepts (classes) and their properties (i.e. TBox) 
that are in charge of semantic interpretation: 

![structure 1](structure1.png)

A bit of the OWL axioms (in the functional syntax), related to that picture:

```
SubClassOf(:Target :Stencil)
SubClassOf(:Process :Target)
SubClassOf(:ExternalInteractor :Target)
SubClassOf(:DataStore :Target)
SubClassOf(:DataFlow :Stencil)
InverseObjectProperties(:hasSource :isSourceOf)
InverseObjectProperties(:hasTarget :isTargetOf)
SubObjectPropertyOf(:hasSource :hasEdge)
SubObjectPropertyOf(:hasTarget :hasEdge)
```

A 'SubClassOf' axiom tells that the first concept is a specific type of the second one.
There are two general types of stencils - 'Target' and 'DataFlow'. 

Targets represent different entities of a system. 
They know three types of targets - 'Process', 'DataStore', and 'ExternalInteractor' (or actor).

'DataFlow' models a directional flow between two targets. 
It has two properties 'hasSource' and 'hasTarget'.
And the 'isSourceOf' and 'isTargetOf' properties belong to the source and target items.

The 'InverseObjectProperties' axioms tell that 'hasSource' is inverse of 'isSource' 
('hasTarget' is inverse of 'isTargetOf' as well).

Also, there is the 'hasEdge' property that is a generalization of 'hasSource' and 'hasTarget'
(see the 'SubObjectPropertyOf' axioms).
The same is with 'isEdge' (not shown in the listing) - 'isSourceOf' and 'isTargetOf' are its subproperties.

Also the base model allows to model boundaries (see details in the OWL file).

A simple DFD diagram looks like:

![Diagram example](dfd_example.png)

It is possible to interpret that picture as a set of the ABox axioms. 
Some of them are like shown here:

```
ClassAssertion(:Process :pr1)
ClassAssertion(:Process :pr2)
ClassAssertion(:DataFlow :flow)
ObjectPropertyAssertion(:hasSource :flow :pr1)
ObjectPropertyAssertion(:hasTarget :flow :pr2)
```

The 'ClassAssertion' axioms mean that the 'pr1' and 'pr2' instances have the 'Process' type,
and the 'flow' instance is a 'DataFlow'.
The 'ObjectPropertyAssertion' lines tell that 'flow' has source 'pr1' and target 'pr2'.

You do not need to tell in this example, that 'pr1' is a source of 'flow' and 'pr2' is its target.
These facts could be got automatically, if applied reasoning (e.g. with HermiT).


## Automatic reasoning of threats 

In modern computer systems data flows are origins of security issues, 
because most of the attacks are remote and sourced from networks. 
Usually threats are applied to a system by data flows.
Also, reasons for adding a countermeasure to particular architecture depend on the presence of a data flow.

A part of the TBox, related to threats and countermeasures:

![structure 2](structure2.png)

Here it is the 'affects' property, aimed to tell that some threat affects a flow or target.
And the 'isAffectedBy' property is inverse to 'affects'.

The simplest approach of threat reasoning includes two steps: 
a) use defined classes (templates) for 'catching' affected flows and targets,
and b) automatically apply instances of threats to the affected items. 

### A data flow example

This OWL construction describes a defined class (AffectedByGenericFlowThreat),
which takes all instances (i.e. flows) that have the edge (source or target):

```
EquivalentClasses(
   :AffectedByGenericFlowThreat 
   ObjectSomeValuesFrom(:hasEdge :Target)
)
```

In Protege it is the same as define 'Equivalent To' area of the 'AffectedByGenericFlowThreat'
class as 'hasEdge some Target'.

The next expression tells that all instances of the 'AffectedByGenericFlowThreat' class
are affected by 'threat_GenericDenialOfService' (attn. the last is an instance):

```
SubClassOf(
   :AffectedByGenericFlowThreat 
   ObjectHasValue(:isAffectedBy :threat_GenericDenialOfService)
)
```

In Protege it is like 'isAffectedBy value threat_GenericDenialOfService' in the 'Subclass Of' area.

Now consider the axioms:

```
ClassAssertion(:Process :pr1)
...
ObjectPropertyAssertion(:hasSource :flow :pr1)
```

The 'Process' class is subclass of 'Targer' (see above), 
so HermiT should classify the 'flow' instance as 'AffectedByGenericFlowThreat' 
and apply the 'isAffectedBy threat_GenericDenialOfService' property to it.


### A target example

Lets define a template for a threat, affecting the 'Process' instances.

We need two defined classes to catch flows that have some process as a source or target:

* 'HasTargetProcess' with the 'hasTarget some Process' definition
* 'HasSourceProcess' with the 'hasSource some Process' definition

And we need two defined classes to find processes, connected to such flows:

* 'AffectedByGenericProcessThreatAsSource' with 'isSourceOf some HasSourceProcess' as a definition
* 'AffectedByGenericProcessThreatAsTarget' with 'isTargetOf some HasTargetProcess' as a definition

Now it is possible to add threat instances to the last two concepts with the 'SubclassOf axioms.

Pay attention, a such threat instance, taken from the base model, 
is common for every affected entity of a semantic interpretation.
This does not allow to distinct threats, for example, for applying entity-dependent mitigations to a specific threat.  
To 'personalize' the reasoning results, you should create an instance of the threat class for each affected entity.

To enable this feature in applications (e.g. with OWL API), the flow and target templates can have
standard subclass axioms with the 'suggestsThreatCategory' property like:

```
SubClassOf(
   :AffectedByGenericProcessThreatAsSource 
   ObjectSomeValuesFrom(:suggestsThreatCategory :GenericProcessThreat)
)
```

In the example an application could be able to get subclasses of 'GenericProcessThreat'
and create instances of threats (if user wished that).

And an example of a such threat class is:

```
SubClassOf(:GenericDenialOfServiceThreat :GenericDataStoreThreat)
SubClassOf(:GenericDenialOfServiceThreat :GenericFlowThreat)
SubClassOf(:GenericDenialOfServiceThreat :GenericProcessThreat)
SubClassOf(:GenericDenialOfServiceThreat :GenericThreat)
SubClassOf(:GenericDenialOfServiceThreat ObjectHasValue(:labelsSTRIDE :STRIDE_Denial_of_Service))
```

The instance of the generic DoS is:

```
ClassAssertion(:GenericDenialOfServiceThreat :threat_GenericDenialOfService)
ClassAssertion(:SchemaInstance :threat_GenericDenialOfService)
```

## STRIDE approach

Let us argue that: 
* processes are affected by every STRIDE threat 
* datastores suffer from the tampering, repudiation, information disclosure, and DoS threats 
* external entities are affected by spoofing and DoS
* flows suffer from information disclosure, tampering, and DoS.

That is STRIDE-per-element approach, implemented by the base model
(actually we have taken this from the OWASP Threat Dragon sources).

Keep in mind, if a target interacts with two other targets (i.e. has two flows), 
it has double number of STRIDE threats.
And, for example, the DoS threat, spawned by the first flow, can be different from spawned by the second one.
Enumeration of threats, their proper management etc. should be implemented with an application. 

## Labelling of threats and countermeasures

For more precise classification of particular threats and countermeasures the STRIDE model can be used, 
as well as a set of security objectives (SO).

Both depend on each other:

Spoofing = Authentication 

Tampering = Integrity

Repudiation = Non-repudiation

Information Disclosure = Confidentiality

Denial of Service = Availability

Elevation of Privilege = Authorization

Example of the labelling class:

```
EquivalentClasses(:LabelsDenialOfService ObjectHasValue(:labelsSTRIDE :STRIDE_Denial_of_Service))
SubClassOf(:LabelsDenialOfService :ClassifiedLabel)
SubClassOf(:LabelsDenialOfService ObjectHasValue(:labelsSO :SO_Availability))
```
So, you can label threats and countermeasures by the STRIDE and SO instances (see the OWL file) 
with the 'labelsSTRIDE' and 'labelsSO' properties.

## Examples

Here is an early example of a flow, tested against a simple threat model in Protege:

![Simple example in Protege](protege_example.png)



