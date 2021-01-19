# Lab1: Introduction to the ontology-driven threat modeling

## Notes

The classic approach to threat modeling is based on [Data Flow Diagrams (DFD)](https://www.google.com/search?q=Shostack+A.+Experiences+Threat+Modeling+at+Microsoft).
Also several [other techniques](https://insights.sei.cmu.edu/sei_blog/2018/12/threat-modeling-12-available-methods.html) exist.

So, there is a simple computer system. 
It includes a web application (process) and background database (storage).
Also, users of this system (external entities) should be taken into account.
A user communicates with the application by the HTTPS protocol,
and the application and database interact by the SQL protocol.

To examine security aspects of the system, a simple DFD has been created:

![lab1_example1](pics/lab1_example1.png)

After the OdTMServer application processing [the threat model](models/lab1_example1_modelled.json) is like:

![lab1_example2](pics/lab1_example1modelled.png)

The 'db' item in the picture can be affected by the tampering, repudiation, denial of service, and information disclosure threats.


## Assignments



## Self-testing

* What is threat modeling?
* What approaches are used for the threat modeling?
* Which items can a data flow diagram consist of?
* What is STRIDE?
* What benifits does STRIDE-per-element provide?


