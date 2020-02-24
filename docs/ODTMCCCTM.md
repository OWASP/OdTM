/[ . . ](../README.md)/

# Common Cloud Computing Threat Model (CCCTM)

This model is an example of domain-specific threat model. 
It depicts some common threats to cloud systems.
The model is implemented as a OWL ontology.

* [OWL file](../OdTMCCCTM.owl)

There is also the similar model, 
implemented as a [threat template](https://github.com/nets4geeks/CCCTM_template) 
in the XML format for the Microsoft Threat Modelling (TM) tool.

It can be used to prove different ideas (see below). 
Also if someone made a decision to create an opensource, ontology-driven alternative to Microsoft TM,
the CCCTM ontology could be used to test the software means.

Note, that it does not have a countermeasure hierarchy.


## Proof of concept

To create such a picture, you would use the [CCCTM template](https://github.com/nets4geeks/CCCTM_template), 
implemented as XML and the [Microsoft TM tool](https://aka.ms/threatmodelingtool):

![ccctm_example](ccctm_example.png)

and you would get a list in Microsoft TM:

![ccctm_example](ccctm_mtm.png)

But there is another option. You can interpret a DFD as DL axioms ...

```
ExternalService (dns)
CloudService (app)
RemoteUser (user)
ApplicationFlow (flow1)
hasSource (flow1, app)
hasTarget (flow1, dns)
ApplicationFlow (flow2)
hasSource (flow2, user)
hasTarget (flow2, app)
```

... and put them as OWL into Protege with CCCTM, implemented as the ontology, 
add the ontology of the [base model](../OdTMBaseThreatModel.owl), enable a reasoner, and obtain the similar results:

![ccctm_protege](ccctm_protege.png)

![ccctm_protege1](ccctm_protege1.png)

## More info

Some details are [here](https://www.researchgate.net/publication/338999512_Framework_for_ontology-driven_threat_modelling_of_modern_computer_systems)

If you want to refer to the CCCTM model, please cite:
>Brazhuk A., Olizarovich E. Framework for ontology-driven threat modelling of modern computer systems //International Journal of Open Information Technologies. – 2020. – Vol. 8. – No. 2. – С. 14-20.


/[ . . ](../README.md)/

