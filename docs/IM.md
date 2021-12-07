
# Integrated model of security enumerations

This model is an ontological domain-specific threat model
that unites ATT&CK (Enterprise Matrix), CAPEC, CWE, and partially CVE.
The model can be used to learn relations between attack patterns, weaknesses, and vulnerabilities, 
evaluate the quality of such links, and to build various threat landscapes.

* [OWL file](../OdTMIntegratedModel.owl) - contains the ontology in the functional syntax.
* [Clear RDF](../applications/generateIM/ttl/OdTMIntegratedModel.ttl) - contains the same data in the RDF format.
* [Reasoned RDF](../applications/generateIM/ttl/OdTMIntegratedModel_filled.ttl) - contains the inferred dataset.

The structure of the relations is shown in the figure:

![im_concepts](im_concepts.png)

Both the root concept 'Threat' and the 'ATTCKTechnique' concept represent 
the [ATT&CK techniques](https://github.com/mitre-attack/attack-stix-data).

The 'CAPEC' concept represents another kind of attack patterns, taken from an [XML file](https://capec.mitre.org).

The 'CWE' concept indicates entities of the weakness enumeration, which are provided by an [XML file](https://cwe.mitre.org).

Also, CWEs refer to CVEs. So, the knowledge base has only CVEs mentioned in the CWE enumeration.


## Use of linked enumerations

Example of looking entities that are 'parents' and 'children' for a given CAPEC instance:

![im_example1](im_example1.png)

Example of finding relevant techniques to a given CVE entity:

![im_example2](im_example2.png)

A set of property chains (refToATTCK, refToCAPECreasoned, refToCWEreasoned, and refToCVEreasoned) 
and the 'refToEnum' subproperty allow to map patterns, weaknessess, and vulnerabilities to techniques:

![im_example3](im_example3.png)


## Use in DFD based threat modeling 

There is a trend of creation various ratings of attack patterns, weaknesses, and vulnerabilities, 
like [Top 25 CWEs](https://cwe.mitre.org/top25/). 
With a diagram it can be possible to map particular CAPECs or CWEs to a system item in order 
to learn possible attack techniques. 

For example, to examine the item with CWE-284, it can be labeled as 'enum#CWE-284'. 
During modeling process a special defined class is created that catches all the threats 
that have the 'refToEnum value CWE-284' property.
After applying this class, a reasoner should start one more time to have the threats, 
associated with labeled component.

Example is:

![im_example4](im_example4.png)


[More examples can be found there](../applications/generateIM/cases/).



