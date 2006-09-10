PROJECT: Codehaus JRemoting
====================================================================

1. DESCRIPTION: 
---------------

This directory contains the Codehaus JRemoting sources and all resources required to build libraries.
Structure and build procedures are described below.

2. STRUCTURE:
-------------

remoting
    |
    +-- api               	API interfaces and command objects marshalled across the wire
    +-- client                client implementation
    +-- server                server implementation
    +-- tools				tools like Proxy Generator, Ant tasks, JavaCompiler
    +-- itests				Integration test cases

Each module comes with its own unit tests as well as module specific documentation.

3. BUILD PROCEDURE :
--------------------

 Requires maven 2.x
 
 To build components and run integration tests

  $ mvn install

 To deploy components

  $ mvn deploy
  
 To deploy components' reports

  $ mvn  site-deploy
  
 To clean up all the build artifacts 

  $ mvn clean
  

FURTHER  QUERIES :
------------------
For additional information please check the documentation and also use the 
JRemoting Mailing list (subscription details in the doc).
