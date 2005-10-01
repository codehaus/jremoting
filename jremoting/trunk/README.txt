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

 To build components

  $ maven jremoting:install

 To deploy components

  $ maven jremoting:deploy

 To deploy components' reports

  $ maven jremoting:site-deploy

 To run integrations tests
 
  $ maven jremoting:itests

 To clean up all the build artifacts 

  $ maven jremoting:clean


FURTHER  QUERIES :
------------------
For additional information please check the documentation and also use the 
JRemoting Mailing list (subscription details in the doc).

Finally, please keep in mind that while Codehaus JRemoting is nearing completion and
readiness for a first release, it should be considered as beta software as 
APIs are changing, and documentation is evolving.

vinayc 27 August 2003
paul/mauro   30 Sept 2005

