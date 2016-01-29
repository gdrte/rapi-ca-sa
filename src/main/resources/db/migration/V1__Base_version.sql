CREATE TABLE CertificateSubject
  ( 
     id                 VARCHAR(255) NOT NULL, 
     commonName         VARCHAR(255) DEFAULT NULL, 
     countryName        VARCHAR(255) DEFAULT NULL, 
     emailAddress       VARCHAR(255) DEFAULT NULL, 
     locality           VARCHAR(255) DEFAULT NULL, 
     organization       VARCHAR(255) DEFAULT NULL, 
     organizationalUnit VARCHAR(255) DEFAULT NULL, 
     state              VARCHAR(255) DEFAULT NULL, 
     subjectAltName     VARCHAR(255) DEFAULT NULL, 
     nsComment          VARCHAR(255) DEFAULT NULL, 
     CA                 BIT(1) DEFAULT NULL, 
     pathlen            INT(11) NOT NULL, 
     PRIMARY KEY (id) 
  ) 
engine=innodb; 

CREATE TABLE CertificateProvider 
  ( 
     id       VARCHAR(255) NOT NULL, 
     hostName VARCHAR(255) NOT NULL, 
     userName VARCHAR(255) DEFAULT NULL, 
     password VARCHAR(255) DEFAULT NULL, 
     PRIMARY KEY (id) 
  ) 
engine=innodb; 

CREATE TABLE Certificate 
  ( 
     id                           VARCHAR(255) NOT NULL, 
     cert                         LONGTEXT, 
     daysValidFor                 INT(11) NOT NULL, 
     downstream                   BIT(1) NOT NULL, 
     privateKey                   LONGTEXT, 
     selfSigned                   BIT(1) NOT NULL, 
     upstream                     BIT(1) NOT NULL, 
     certificateProvider_id       VARCHAR(255) DEFAULT NULL, 
     signer_id                    VARCHAR(255) DEFAULT NULL, 
     subject_id                   VARCHAR(255) DEFAULT NULL, 
     signedOn                     DATETIME DEFAULT NULL, 
     role                         INT(11) NOT NULL, 
     PRIMARY KEY (id), 
     UNIQUE KEY id (id), 
     KEY Signer_Certificate_Key (signer_id), 
     KEY Subject_Provider_Key (subject_id), 
     KEY Certificate_Provider_Id_Key (certificateProvider_id), 
     KEY Certificate_Role_Index (role), 
     CONSTRAINT Subject_Provider_Key FOREIGN KEY (subject_id) REFERENCES 
     CertificateSubject (id), 
     CONSTRAINT Signer_Certificate_Key FOREIGN KEY (signer_id) REFERENCES 
     Certificate (id), 
     CONSTRAINT Certificate_Provider_Id_Key FOREIGN KEY (certificateProvider_id) 
     REFERENCES CertificateProvider (id) 
  ) 
engine=innodb; 
