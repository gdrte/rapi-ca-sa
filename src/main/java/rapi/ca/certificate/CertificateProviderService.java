package rapi.ca.certificate;

import rapi.ca.certificate.model.Certificate;
import rapi.ca.certificate.model.CertificateProvider;
import rapi.ca.certificate.model.CertificateSigningRequest;

import java.util.List;


interface CertificateProviderService {

    CertificateProvider add(final CertificateProvider aCertificateProvider);

    CertificateProvider update(final CertificateProvider aCertificateProvider, final String aCPId);

    void delete(final String aCPId);

    List<CertificateProvider> list();

    CertificateProvider show(final String aCPId);

    List<Certificate> getRemoteSigners(CertificateProvider aProvider, final String anId);

    List<Certificate> getRemoteCertificates(CertificateProvider aProvider);

    Certificate findRemoteCertificate(final CertificateProvider aCertificateProvider, String anId);

    Certificate signCertificateSigningRequest(CertificateProvider certificateProvider,
                                              CertificateSigningRequest csr);
}
