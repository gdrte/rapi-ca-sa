package rapi.ca.certificate.model;

public class PublicCertificate extends Certificate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 28652930968213846L;
	private final Certificate certificate;

	public PublicCertificate(final Certificate aCert) {
		certificate = aCert;
	}

	@Override
	public boolean equals(final Object obj) {
		return certificate.equals(obj);
	}

	@Override
	public String getCert() {
		return certificate.getCert();
	}

	@Override
	public CertificateProvider getCertificateProvider() {
		return certificate.getCertificateProvider();
	}

	@Override
	public int getDaysValidFor() {
		return certificate.getDaysValidFor();
	}

	@Override
	public String getId() {
		return certificate.getId();
	}

	@Override
	//@JsonIgnore
	public String getPrivateKey() {
		return certificate.getPrivateKey();
	}

	@Override
	public int getRole() {
		return certificate.getRole();
	}

	@Override
	public Certificate getSigner() {
		return certificate.getSigner() != null ? new PublicCertificate(certificate.getSigner()) : null;
	}

	@Override
	public CertificateSubject getSubject() {
		return certificate.getSubject();
	}

	@Override
	public int hashCode() {
		return certificate.hashCode();
	}

	@Override
	public boolean isSelfSigned() {
		return certificate.isSelfSigned();
	}

	@Override
	public void setCert(final String cert) {
		certificate.setCert(cert);
	}

	@Override
	public void setCertificateProvider(final CertificateProvider certificateProvider) {
		certificate.setCertificateProvider(certificateProvider);
	}

	@Override
	public void setDaysValidFor(final int daysValidFor) {
		certificate.setDaysValidFor(daysValidFor);
	}

	@Override
	public void setId(final String id) {
		certificate.setId(id);
	}

	@Override
	public void setPrivateKey(final String privateKey) {
		certificate.setPrivateKey(privateKey);
	}

	@Override
	public void setRole(final int aRole) {
		certificate.setRole(aRole);
	}

	@Override
	public void setSelfSigned(final boolean selfSigned) {
		certificate.setSelfSigned(selfSigned);
	}

	@Override
	public void setSigner(final Certificate signer) {
		certificate.setSigner(signer);
	}

	@Override
	public void setSubject(final CertificateSubject subject) {
		certificate.setSubject(subject);
	}

	@Override
	public String toString() {
		return certificate.toString();
	}
}
