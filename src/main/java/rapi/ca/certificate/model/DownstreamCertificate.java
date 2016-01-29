package rapi.ca.certificate.model;


public class DownstreamCertificate extends Certificate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8810269690189377058L;
	private final Certificate certificate;

	public DownstreamCertificate(final Certificate aCertificate) {
		certificate = aCertificate;
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
	public String getPrivateKey() {
		return certificate.getPrivateKey();
	}

	@Override
	public int getRole() {
		return certificate.getRole();
	}

	@Override
	public Certificate getSigner() {
		return certificate.getSigner();
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
	public boolean isDownstream() {
		return true;
	}

	@Override
	public boolean isSelfSigned() {
		return certificate.isSelfSigned();
	}

	@Override
	public boolean isUpstream() {
		return certificate.isUpstream();
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
	public void setDownstream(final boolean downstream) {
		certificate.setDownstream(downstream);
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
	public void setUpstream(final boolean upstream) {
		certificate.setUpstream(upstream);
	}

	@Override
	public String toString() {
		return certificate.toString();
	}
}
