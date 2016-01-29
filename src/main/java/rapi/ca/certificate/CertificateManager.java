package rapi.ca.certificate;

import rapi.ca.certificate.model.*;

public interface CertificateManager {

    Certificate generateKey(Certificate aCert);

    Certificate generateSelfSigned(Certificate aCert);

    CertificateSigningRequest generateCertificateSigningRequest(Certificate aCert);

    Certificate signCertificateSigningRequest(CertificateSigningRequest aCSR);

    PKCS12Certificate pkcs12(PKCS12CertificateRequest aPkcs12Request);

    String sign(SigningRequest signingRequest);

    byte[] signSMIME(final byte[] buffer);
}