# Distributed Systems
**Final Grade:** 17.3 (GPA of part 1 and 2)  
**Author:** Ricardo Rodrigues (rf-rodrigues95)

# TuKano - Part 1 (sd2324-tp1)

[Project Specification (trab1-2324.pdf)](./assignment/part1/trab1-2324.pdf)

In the folder `assignment/part1/` you will also find a similar description as the one linked above, as well as the description of the test battery (tester) used for evaluation.

**sd2324-tp1** includes the API and a set of files that should be used in project 1 (first part).

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

# TuKano - Part 2 (sd2324-tp2)

**sd2324-tp2** includes the API and a set of files that should be used in project 2 (second part of the project).

[Project Specification (trab2-2324.pdf)](./assignment/part2/trab2-2324.pdf)

The second phase of the TuKano project builds upon the initial microservice-based architecture, introducing key distributed systems concepts such as security, fault tolerance, and external service integration, while preserving API compatibility and the original modular design, and incorporating significant improvements to the underlying codebase.

## Security and Access Control
All client-server communication now takes place over TLS-secured channels with server-side authentication. Clients must provide passwords to authenticate requests, as defined in the existing API. Inter-service communication is protected using a shared secret, provided at startup, preventing unauthorized access to internal operations. In the Blob service, access control mechanisms were implemented to ensure that only authorized users can perform operations, through mechanisms like query parameter validation or token-based authorization.

## External Blob Storage Integration
The Blob service was extended to support storage in an external cloud service, such as Dropbox, accessed via a REST API authenticated through OAuth. This enables horizontal scaling and decouples media storage from the internal infrastructure. The server supports a startup flag to reset external state for testability, allowing for integration with the automated test suite.

## Fault Tolerance and Replication
### Shorts Service
The Shorts service was made fault-tolerant through a state machine replication strategy implemented using Kafka. All state-changing operations are published to a Kafka topic, and servers consume and apply updates in order to maintain consistency. This design guarantees that:
- Servers remain consistent with each other.
- Clients always observe non-decreasing versions of the system state.
- Causal consistency is preserved through custom HTTP headers (X-SHORTS-*), which the client includes in future requests as required.

This architecture enables horizontal scalability and seamless failover, ensuring the Shorts service continues functioning even in the presence of server failures.

### Blob Service
Blob redundancy is achieved by allowing multiple Blob servers to run concurrently, each capable of storing content. A Short may include multiple blob URLs (delimited by |) to reference redundant copies, ensuring that blob content remains accessible if a server fails.

