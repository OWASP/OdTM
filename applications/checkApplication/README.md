
# Check Application

It allows to test the base model, domain models (as well as other OWL ontologies)
against different ABox sets.

It takes an ontology (including its imports), 
applies a set of test axioms to the ontology, reason it with Hermit,
and looks for an expected set of axioms, reporting presence or not. 

To create a test case you should put in a folder 
the test set as the 'appliedAxioms' file in functional syntax, 
and the expected set as the 'expectedAxioms' file.
At the moment the 'ClassAssertion' and 'ObjectPropertyAssertion' axioms are only supported.

To run the application you need a properties file with 
list of import OWL files (IMPORTS), 
the ontology file (MODEL),
folder where are appliedAxioms and expectedAxioms (FOLDER).

Example of a test case folder with properties is [here](../../tests/example/).

Running is a bit complicated process (jar comes later), 
but it is quite possible with git and maven (you should clone the OdTM Server [folder](../OdTMServer)). 

Example of a running script is [here](../../tests/run.sh).

Whole [folder](../../tests/) is an example 
how this simple testing approach has been added to the base model.

