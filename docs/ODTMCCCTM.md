/[ . . ](../README.md)/

# Common Cloud Computing Threat Model (CCCTM)

This model depicts some common threats to cloud systems.
You can use it to test different tools.
Also it is implemented as an [XML template](https://github.com/nets4geeks/CCCTM_template) for the Microsoft TM tool.

* [OWL file](../OdTMCCCTM.owl)

Note, that it does not have a countermeasure hierarchy.


## Proof of concept

You can use the [CCCTM template](https://github.com/nets4geeks/CCCTM_template) 
with the [Microsoft Threat Modeling Tool](https://aka.ms/threatmodelingtool) to create such a picture:

![ccctm_example](ccctm_example.png)

and you would get a list:

![ccctm_example](ccctm_mtm.png)

But there is a better option. It is possible to interpret a DFD as a DL code ...

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

and put it as OWL into Protege with the [base model](../OdTMBaseThreatModel.owl) and the CCCTM model,
enable a resoner, and get the similar results:

![ccctm_protege](ccctm_protege.png)

![ccctm_protege1](ccctm_protege1.png)


/[ . . ](../README.md)/

