package rapi.ca.certificate.model;

import rapi.ca.rest.DataTransferObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@JsonAutoDetect
public class CertificateProvider implements DataTransferObject<String>,
		Comparable<CertificateProvider>{
	private static final long serialVersionUID = 7010401320628914848L;

	@Id
	private String id = UUID.randomUUID().toString();
	private String hostName;
	private String userName;
	private String password;

	public CertificateProvider() {
	}

	public CertificateProvider(final String url) {
		this.hostName = url;
	}

	public String getHostName() {
		return hostName;
	}

	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return userName;
	}

	public void setHostName(final String url) {
		this.hostName = url;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("hostName", hostName).append("userName", userName)
				.append("password", password);
		return builder.toString();
	}

	@Override
	public int compareTo(CertificateProvider anotherCertificateProvider) {
		return new CompareToBuilder()
				.append(this.id, anotherCertificateProvider.id)
				.append(this.hostName, anotherCertificateProvider.hostName)
				.append(this.userName, anotherCertificateProvider.userName)
				.toComparison();
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
