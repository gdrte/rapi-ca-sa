package rapi.ca.certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rapi.ca.certificate.model.*;

import java.io.IOException;
import java.util.*;

@Transactional
@Service
public class CertificateServiceImpl implements CertificateService{
	private static final Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);

	@Autowired
	CertificateProviderService certificateProviderService;

	@Autowired
	CertificateDAO certificateDAO;

	private static <T extends Certificate> List<Certificate> makeSafe(final List<T> aList) {
		final List<Certificate> results = new ArrayList<>();
		for (final Certificate c : aList) {
			results.add(new PublicCertificate(c));
		}
		return results;
	}

	@Autowired  @Qualifier("bouncyManager")
	private CertificateManager certificateManager;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#add(com.cpn.vsp
	 * .vertexcore.model.certificate.Certificate)
	 */
	
	@Transactional
	public Certificate add(Certificate aCert) {
		if ((aCert.getSigner() == null) || "-1".equals(aCert.getSigner().getId())) {
			aCert.setSigner(null);
			aCert.setSelfSigned(true);
			return genCA(aCert);
		} else {
			aCert = certificateManager.generateKey(aCert);
			aCert.setCertificateProvider(null);
			certificateDAO.persist(aCert);
			return signCSR(getCSRForCert(aCert));
		}
	}

	private void cleanCertificate(final Certificate aCert, final CertificateProvider aProvider) {
		if (aCert.getSigner() != null) {
			final Certificate signer = certificateDAO.find(aCert.getSigner().getId());
			if (signer != null) {
				aCert.setSigner(signer);
			} else {
				cleanCertificate(aCert.getSigner(), aProvider);
				aCert.getSigner().setCertificateProvider(aProvider);
				certificateDAO.persist(aCert.getSigner());
			}
		}
		aCert.getSubject().setId("0");
		aCert.setUpstream(true);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#delete(java.lang
	 * .String)
	 */
	
	@Transactional
	public void delete(final String id) {
		Certificate cert = certificateDAO.find(id);
		if (null != cert) {
			certificateDAO.remove(cert);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#genCA(com.cpn.vsp
	 * .vertexcore.model.certificate.Certificate)
	 */
	
	@Transactional
	public Certificate genCA(Certificate aCert) {
		aCert = certificateManager.generateSelfSigned(aCert);
		aCert.setCertificateProvider(null);
		certificateDAO.persist(aCert);
		return new PublicCertificate(aCert);
	}


	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#getCSRForCert(com
	 * .cpn.vsp.vertexcore.model.certificate.Certificate)
	 */
	
	@Transactional
	public CertificateSigningRequest getCSRForCert(Certificate cert) {
		cert = certificateDAO.find(cert.getId());
		final CertificateSigningRequest csr = certificateManager.generateCertificateSigningRequest(cert);
		csr.setSignee(new PublicCertificate(csr.getSignee()));
		csr.setSigner(new PublicCertificate(cert.getSigner()));
		return csr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.clouddepot.certificate.CertificateService#list()
	 */
	
	@Transactional
	public List<Certificate> list() throws IOException {
		return CertificateServiceImpl.makeSafe(certificateDAO.list());
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.clouddepot.certificate.CertificateService#listSigners()
	 */
	
	@Transactional
	public List<Certificate> listSigners() {
		return CertificateServiceImpl.makeSafe(certificateDAO.findSigners());
	}

	@Autowired
	CertificateProviderDAO certificateProviderDAO;

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.clouddepot.certificate.CertificateService#refresh()
	 */
	
	@Transactional
	public Map<String, String> refresh() {
		for (final CertificateProvider s : certificateProviderDAO.list()) {
			refreshRemoteCertificates(s);
		}
		final Map<String, String> result = new HashMap<>();
		result.put("success", "true");
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.clouddepot.certificate.CertificateService#
	 * refreshRemoteCertificates
	 * (com.cpn.vsp.vertexcore.model.certificate.CertificateProvider)
	 */
	
	@Transactional
	public void refreshRemoteCertificates(final CertificateProvider aProvider) {
		for (final Certificate c : certificateProviderService.getRemoteCertificates(aProvider)) {
			if (certificateDAO.find(c.getId()) == null) {
				cleanCertificate(c, aProvider);
				certificateDAO.persist(c);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#show(java.lang.
	 * String)
	 */
	
	@Transactional
	public Certificate show(final String id) throws IOException {
		return new PublicCertificate(certificateDAO.find(id));
	}

	
	@Transactional
	public Certificate find(final String id) {
		return certificateDAO.find(id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#showEverything(
	 * java.lang.String)
	 */
	
	@Transactional
	public Certificate showEverything(final String id) {
		return certificateDAO.find(id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#sign(com.cpn.vsp
	 * .vertexcore.model.certificate.Certificate,
	 * com.cpn.vsp.vertexcore.model.certificate.Certificate)
	 */
	
	public Certificate sign(final Certificate signer, final Certificate signee) {
		return signCSR(getCSRForCert(add(signee)).setSignerFluent(signer));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#signCSR(com.cpn
	 * .vsp.vertexcore.model.certificate.CertificateSigningRequest)
	 */
	
	@Transactional
	public Certificate signCSR(final CertificateSigningRequest csr) {
		if (!csr.getSignee().isDownstream()) {
			csr.setSignee(certificateDAO.find(csr.getSignee().getId()));
		}
		Certificate signer = certificateDAO.find(csr.getSigner().getId());
		if (null == signer) {
			// We are loading full object
			signer = certificateProviderService.findRemoteCertificate(csr.getSigner().getCertificateProvider(), csr
					.getSigner().getId());
			if (null == signer) {
				throw new RuntimeException("Certificate is neither local nor remote");
			}
			signer.setCertificateProvider(csr.getSigner().getCertificateProvider());
			if (null != signer) {
				signer.setUpstream(true);
			}
			signer.setSigner(null);
		}
		csr.setSigner(signer);

		Certificate signedCert = null;
		if (csr.getSigner().isUpstream()) {
			signedCert = certificateProviderService.signCertificateSigningRequest(csr.getSigner()
					.getCertificateProvider(), csr);
			signedCert.setPrivateKey(csr.getSignee().getPrivateKey());
			// Saving the remote signer certificate locally to maintain the
			// Referential Integrity.
			if (null == (signer = certificateDAO.find(csr.getSigner().getId()))) {
				certificateDAO.persist(csr.getSigner());
			} else {
				// reload
				csr.setSigner(signer);
			}
		} else {
			signedCert = certificateManager.signCertificateSigningRequest(csr);
		}

		if (!csr.getSignee().isDownstream()) {
			signedCert.setSigner(csr.getSigner());
			signedCert.setDownstream(false);
			signedCert = certificateDAO.merge(signedCert);
			return signedCert;
		}
		return signedCert;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.clouddepot.certificate.CertificateService#update(com.cpn.
	 * vsp.vertexcore.model.certificate.Certificate)
	 */
	
	@Transactional
	public Certificate update(final Certificate aT) {
		aT.setPrivateKey(certificateDAO.find(aT.getId()).getPrivateKey());
		return certificateDAO.merge(aT);
	}

	

	
	public PKCS12Certificate pkcs12(PKCS12CertificateRequest aPkcs12Request) {
		return certificateManager.pkcs12(aPkcs12Request);
	}

	
	public Certificate save(Certificate certificate) {
		certificateDAO.persist(certificate);
		return certificate;
	}

	
	public Set<Certificate> resolveAllSigners(Certificate aCert) {
		Set<Certificate> signerChain = aCert.getSignerChain();
		for (Certificate cert : signerChain) {
			if (cert.getCertificateProvider() != null) {
				aCert = cert;
				break;
			}
		}
		if (aCert.getCertificateProvider() != null) {
			signerChain.addAll(certificateProviderService.getRemoteSigners(aCert.getCertificateProvider(), aCert.getId()));
		}
		return signerChain;
	}

	
	public Set<Certificate> resolveAllSigners(String anId) {
		Certificate aCert = certificateDAO.findById(anId);
		return resolveAllSigners(aCert);
	}

	
	public Certificate findByCommonName(String commonName) {
		return certificateDAO.findByCommonName(commonName);
	}

	
	public Certificate genCertificate(Certificate aCert) {
		aCert = certificateManager.generateKey(aCert);
		final CertificateSigningRequest csr = certificateManager.generateCertificateSigningRequest(aCert);
		csr.setSignee(new PublicCertificate(csr.getSignee()));
		csr.setSigner(new PublicCertificate(aCert.getSigner()));
		aCert.setCertificateProvider(null);
		return certificateManager.signCertificateSigningRequest(csr);
	}

	
	public byte[] signSMIME(byte[] buffer) {
		return certificateManager.signSMIME(buffer);
	}
}