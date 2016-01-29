package rapi.ca.certificate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class PKCS12CertificateRequest implements Serializable {

	private static final long serialVersionUID = -8359388328788919401L;
	Certificate certificate;
	String caBundle;
    String password;

	@JsonProperty("caBundle")
	public String getCaBundle() {
		return caBundle;
	}

	public Certificate getCertificate() {
		return certificate;
	}

	@JsonProperty("caBundle")
	public PKCS12CertificateRequest setCaBundle(final String certBundle) {
		this.caBundle = certBundle;
		return this;
	}

	public PKCS12CertificateRequest setCertificate(final Certificate certificate) {
		this.certificate = certificate;
		return this;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("certificate", certificate).append("caBundle", caBundle);
		return builder.toString();
	}

}
