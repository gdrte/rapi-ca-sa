package rapi.ca.certificate.model;


public class PKCS12Certificate extends Certificate {
	private static final long serialVersionUID = 637002906594689794L;
	private String pkcs12;

	public PKCS12Certificate(){}
	public PKCS12Certificate(final Certificate aCert){
		this.setCert(aCert.getCert());
		this.setCertificateProvider(aCert.getCertificateProvider());
		this.setDaysValidFor(aCert.getDaysValidFor());
		this.setDownstream(aCert.isDownstream());
		this.setPrivateKey(aCert.getCert());
		this.setRole(aCert.getRole());
		this.setSelfSigned(aCert.isSelfSigned());
		this.setSignedOn(aCert.getSignedOn());
		this.setSignees(aCert.getSignees());
		this.setSigner(aCert.getSigner());
		this.setSubject(aCert.getSubject());
		this.setUpstream(aCert.isUpstream());
	}
	
	public String getPkcs12() {
		return pkcs12;
	}

	public void setPkcs12(final String pkcs12) {
		this.pkcs12 = pkcs12;
	}

}
