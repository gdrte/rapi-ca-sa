# rapi-ca-sa
SpringBoot SA application for creating X509 certificate hierarchy using Bouncy Castle API
Rapi-CA SA
===============
Steps to setup rapi-ca SA(stand alone).

1) Build the rapi-ca standalone
mvn clean install

Copy the folder openssl from src/main/resources to /etc/rapi-ca
Externalize the following properties into a property file. The naming convention must adhere to rapi-ca-\<Environment name\>.properties

Ex: rapi-ca-devDemo.properties

    spring.datasource.url=jdbc:mysql://localhost:3306/rapi_ca
    spring.datasource.username=rapi_ca
    spring.datasource.password=MhcdsoUFN2GyI26
    logging.file=/tmp/rapi-ca.log
    server.port=9090

2.) Run the rapi-ca jar as below.
```bash
 java -jar rapi-ca-sa-1.0.0.jar --spring.profiles.active=devDemo --spring.config.location=/etc/rapi-ca/
```
Please note, the *location* must end with '/'

3.) Once the new standalone application starts successfully, it will recreate the tables required.

 That's it!!!

 Setting up new Certificates
 ==============================

 Create a new self signed ROOT certificate.

 curl -X POST http://localhost:8080/rapi-ca/certificate -d '{
 "subject": {
   "emailAddress": "OPNFV Demo Admin <demo-admin@dev.demo.net>",
   "subjectAltName": "email:copy",
   "nsComment": "UUID:f56a7da7-263c-4438-a942-a488617e9db8",
   "pathlen": -1,
   "CA": true,
   "ST": "Secure",
   "L": "Internet",
   "C": "US",
   "O": "Distributed NFV Demo Solution",
   "OU": "VSP",
   "CN": "Self signed Root"
 },
 "daysValidFor": 1825,
 "selfSigned": true
 }'
 One level hierarchy certificate.
 curl -X POST http://localhost:8080/rapi-ca/certificate -d '{
   "subject": {
     "emailAddress": "OPNFV Demo Asst Admin <one-level-down@dev.demo.net>",
     "subjectAltName": "email:copy",
     "nsComment": "UUID:f56a7da7-263c-4438-a942-a488617e9db8",
     "pathlen": -1,
     "CA": true,
     "ST": "Secure",
     "L": "Internet",
     "C": "US",
     "O": "Distributed NFV Demo Solution",
     "OU": "VSP",
     "CN": "One Level Down, Signed by Root Signer"
   },
   "signer":{"id":"<<Root Signer ID>>"},
   "daysValidFor": 1825,
   "selfSigned": true
 }'
 and so on ......
 Note: These API's are based on the freely available Bouncycastle api documentation. https://www.bouncycastle.org/java.html
