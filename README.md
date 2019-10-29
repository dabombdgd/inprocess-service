Inprocess JAVA App
========

The Inprocess JAVA App is part of the Executive Virtual Assistant (e-VA) IN process.  
This service bridges between the IN process e-VA tables and the BGS service. 

    
Input:
- JDBC PL-SQL triggers on case note changes 
- Configuration file _eva.properties_ 
- Soap template file _template.xml_
- Optional _test/response.xml_
- Optional _test/case_notes.xml_
      
Output:
- SOAP Request is sent to BGS Services
- Log messages written to log file

Requirements:
- Java 1.8 
- Oracle 11g 

- The new _test/response.xml_ file allows you to use or bypass BGS as needed.  To use BGS, rename this file
- The new _jdbc-url_ setting allows you to use or bypass JDBC
- _caseID_ field capitalization (soap) changed to _caseId_ to match BGS
- Update and Insert can be simulated in the _case_notes.xml_ file by using SINGLE quotes '' for any empty fields  

Copy these files that are not in the .jar file:
- _eVA.properties_
- _template.xml_ full version

To build from source:

   MAVEN is utilized. e.g. mvn clean install


JavaService Command Line
======================

TBD
