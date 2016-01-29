package rapi.ca.certificate;


import rapi.ca.certificate.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CertificateService {
    Certificate save(final Certificate certificate);

    Certificate add(Certificate aCert);

    Set<Certificate> resolveAllSigners(Certificate aCert);

    Set<Certificate> resolveAllSigners(String id);

    void delete(String id);

    Certificate genCA(Certificate aCert);

    Certificate genCertificate(Certificate aCert);

    CertificateSigningRequest getCSRForCert(Certificate cert);

    List<Certificate> list() throws IOException;

    List<Certificate> listSigners();

    Map<String, String> refresh();

    void refreshRemoteCertificates(CertificateProvider aProvider);

    Certificate show(String id) throws IOException;

    Certificate find(String id);

    Certificate findByCommonName(String commonName);

    Certificate showEverything(String id);

    Certificate sign(Certificate signer, Certificate signee);

    Certificate signCSR(CertificateSigningRequest csr);

    Certificate update(Certificate aT);

    PKCS12Certificate pkcs12(final PKCS12CertificateRequest aPkcs12Request);

    byte[] signSMIME(byte[] buffer);
}
