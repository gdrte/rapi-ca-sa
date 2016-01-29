package rapi.ca.certificate.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;


public class CertificateSigningRequest implements Serializable {

	private static final long serialVersionUID = 452590152369913976L;

	private String csr;

	private Certificate signer;

	private Certificate signee;

	public CertificateSigningRequest() {
	}

	public CertificateSigningRequest(final String aCSR) {
		csr = aCSR;
	}
	

	public CertificateSigningRequest(String csr, Certificate signer, Certificate signee) {
		super();
		this.csr = csr;
		this.signer = signer;
		this.signee = signee;
	}

	public String getCsr() {
		return csr;
	}

	public Certificate getSignee() {
		return signee;
	}

	public Certificate getSigner() {
		return signer;
	}

	public void setCsr(final String csr) {
		this.csr = csr;
	}

	public void setSignee(final Certificate signee) {
		this.signee = signee;
	}

	public void setSigner(final Certificate signer) {
		this.signer = signer;
	}

	public CertificateSigningRequest setSignerFluent(final Certificate signer) {
		this.signer = signer;
		return this;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);

		return builder.toString();
	}

}
