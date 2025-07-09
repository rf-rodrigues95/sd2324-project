# Distributed Systems
**Final Grade:** 17.3 (GPA of part 1 and 2)  
**Author:** Ricardo Rodrigues (rf-rodrigues95)

# TuKano - Part 1 (sd2324-tp1)

**sd2324-tp1** includes the API and a set of files that should be used in project 1.

* ```test-sd-tp1.bat``` / ```test-sd-tp1.sh``` :  script files for running the project in Windows and Linux/Mac
* ```shorts.props``` : file with information for starting servers
* ```Dockerfile``` : Dockerfile for creating the docker image of the project
* ```hibernate.cfg.xml``` : auxiliary file for using Hibernate
* ```pom.xml``` : maven file for creating the project


## Overview
TuKano is a distributed social media platform inspired by TikTok, designed and implemented as part of the Distributed Systems course. The system is structured around three core services — Users, Shorts, and Blobs — each running independently and communicating over the network. These services expose RESTful APIs using JAX-RS, and gRPC, to allow client interactions and inter-service coordination. The architecture supports decentralized operation through multicast-based service discovery, removing any reliance on hardcoded IPs and enabling dynamic server detection on the local network.

Auto-configuration of servers is implemented through IP multicast broadcasts, where each server periodically advertises its service type and URI, enabling runtime discovery both by clients and the testing suite. Fault tolerance was deliberately scoped to handle transient communication failures, not full component replication or crash recovery, in line with the project’s constraints.


## Microservices Integration with Docker, Concurrency Control, and gRPC Support

The entire system was containerized using Docker, and integration with a standardized test suite was supported through a configuration script and Maven-based builds. Additional value was added through concurrency control, ensuring that operations across all services behave correctly under concurrent access, as verified by stress tests included in the official tester. Optional gRPC interfaces were also integrated to extend interoperability, allowing mixed REST and gRPC deployments.


2 Separate Versions, tp1 and tp2.

Tp2 is an extension of the first version, with TLS, replication and OAUTH services.

The project had 2 phases(hence the 2 versions), both have their own battery of tests, which work for their respective source code.
