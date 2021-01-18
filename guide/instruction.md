
# How to create own ontology-driven threat models

We are working on improvement of our software tools and their availability for wide audience.
However, the creation of the ontology-driven models is a bit tedious at the moment.
We hope to fix this challenge on our own 
(also, we welcome any initiative of creation tools for the ontology-driven threat modeling).

## Preparation

### Download and install the Threat Dragon tool

The [OWASP Threat Dragon](https://github.com/OWASP/threat-dragon-desktop/releases) desktop application 
is a well-known free, open-source, cross-platform threat modeling tool.
It has well-formed [documentation](https://docs.threatdragon.org/).

### Download and compile the OdTMServer application 

You need a **git client**, **java** & **maven**.

* Clone the [OdTM repository](https://github.com/nets4geeks/OdTM.git), go to the 'applications/OdTMServer' folder and run 'mvn compile' there.

* For every Threat Dragon's JSON file you need a properties file (use the 'server_acctp.properties' file as an example)

For the cloud modeling you need files of next ontologies:

- [OdTMBaseThreatModel.owl](../OdTMBaseThreatModel.owl) - the base threat model (the 'BASEMODEL' property) 

Other are items of the 'MODELS' property:

- [SecurityPatternCatalogNaiveSchema.owl](https://github.com/nets4geeks/SPCatalogMaker/blob/master/schema/SecurityPatternCatalogNaiveSchema.owl) - the SP schema;

- [common.owl](https://github.com/nets4geeks/SPCatalogMaker/blob/master/catalogs/acctp/catalog/common.owl) - the ACCTP common model

- [ACCTPCatalog.owl](https://github.com/nets4geeks/SPCatalogMaker/blob/master/catalogs/acctp/catalog/ACCTPCatalog.owl) - the ACCTP target model;

- [OdTMACCTP.owl](../OdTMACCTP.owl) - the ACCTP domain specific threat model.

The 'TDFILE' property describes a source JSON file, and 'TDOUT' points to a target JSON file.

* To run a particular modelling use command:

```
mvn -e exec:java -q -Dexec.mainClass="ab.run.consoleApplication" -Dexec.args="server_xxx.properties" 
```

where the last item is the properties file.

## Model creation

* Use Threat Dragon to create a diagram, like this:

![acctp_example](pics/td_simple_example.png)

You should mark items by the labels from the ACCTP common model with the 'Description' field.
Here the 'webapp' item belongs to the 'CloudApplication' class.

Current list of the labels includes:

```
class#CloudApplication
class#CloudInfrastructure
class#ComplianceManager
class#ExternalService
class#PaaSApplication
class#PrivacyManager
class#RemoteUser
class#SaaSApplication
class#VirtualMachine
```

* Use Dragon's saved JSON file as a source (TDFILE) of the OdTMServer console application to get a target JSON file.

* Load the target JSON to the Threat Dragon one more time: 

![acctp_example1](pics/td_simple_example1.png)

Now you can work with threats (apply mitigations, define severity or delete).
