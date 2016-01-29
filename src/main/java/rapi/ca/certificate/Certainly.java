package rapi.ca.certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rapi.ca.certificate.model.*;

/**
 * 
 * @author gdr
 * 
 *         Manages the Creation of X.509 certificates
 *         Creation and management of private keys, public keys and parameters
 * 
 */
@Service
public class Certainly {
	@Autowired @Qualifier("opensslManager")
	CertificateManager certificateManager;

	public CertificateSigningRequest generateCertificateSigningRequest(final Certificate aCert) {
		return certificateManager.generateCertificateSigningRequest(aCert);
	}

	public Certificate generateSelfSigned(final Certificate aCert) {
		return certificateManager.generateSelfSigned(aCert);
	}

	public PKCS12Certificate pkcs12(final PKCS12CertificateRequest aPkcs12Request) {
		return certificateManager.pkcs12(aPkcs12Request);
	}

	public Certificate populateCertificate(final Certificate aCert) {
		return certificateManager.generateKey(aCert);
	}

	public byte[] sign(final String aCert, final String aPrivateKey, final String aCA, final byte[] aMessage) {
		SigningRequest request = new SigningRequest();
		request.setCa(aCA);
		request.setCert(aCert);
		request.setPrivateKey(aPrivateKey);
		request.setMessage(new String(aMessage));
		return certificateManager.sign(request).getBytes();
	}

public Certificate signCertificateSigningRequest(final CertificateSigningRequest aCSR) {
		return certificateManager.signCertificateSigningRequest(aCSR);
	}

	public byte[] signSMIME(byte[] buffer){
		return certificateManager.signSMIME(buffer);
	}
}
