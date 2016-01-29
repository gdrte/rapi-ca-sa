package rapi.ca.certificate.model;

import rapi.ca.rest.DataTransferObject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.jasypt.hibernate4.type.EncryptedStringType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * Certificate type provides an certificate for the resource.
 * It communicates with certialny for certificate generations through
 * certification provider. certificate holds details like days valid for,
 * signer, role, private key, signee, selfsigne, upstream and cert,
 * which is long string.
 */
@JsonAutoDetect
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "id"))
@TypeDef(name = "encryptedString", typeClass = EncryptedStringType.class, parameters = { @Parameter(name = "encryptorRegisteredName", value = "hibernateStringEncryptor") })
public class Certificate implements Serializable, DataTransferObject<String> {

	private static final long serialVersionUID = 8031287454475086644L;

	public Certificate(boolean isCertificateAuthority,boolean isSelfSigned,int daysValidFor,int role,String email,String cn){
		for (Entry<String, String> s : getCertificateSubject(email,cn).entrySet()) {
			this.getSubject().put(s.getKey(),s.getValue());
		}
		this.getSubject().setCA(isCertificateAuthority);
		this.setSelfSigned(isSelfSigned);
		this.setDaysValidFor(daysValidFor);
		this.setRole(role);
	}
	
	public Map<String,String> getCertificateSubject(String email,String cn){
		Map<String,String> subjectData = new HashMap<String,String>();
		subjectData.put("C", "US");
		subjectData.put("ST", "California");
		subjectData.put("O", "ClearPath Networks");
		subjectData.put("OU", "Engineering Department");
		subjectData.put("CN", cn==null?email:cn);
		subjectData.put("L", "El Segundo");
		subjectData.put("emailAddress", email);
		subjectData.put("subjectAltName", "email:copy");
		return subjectData;
	}
	

	@JsonIgnore
	public static String buildCABundle(final Set<Certificate> signers) {
		//final Set<Certificate> signers = Certificate.resolveAllSigners(aSet);
		final StringBuffer buffer = new StringBuffer();
		for (final Certificate c : signers) {
			buffer.append(c.getCert());
		}
		return buffer.toString();
	}

	@JsonIgnore
	public static Set<Certificate> resolveAllSigners(final Certificate aCert) {
		final Set<Certificate> set = new LinkedHashSet<>();
		if (aCert == null) {
			return set;
		}
		set.add(aCert);
		if (aCert.getSigner() != null) {
			set.addAll(Certificate.resolveAllSigners(aCert.getSigner()));
		}
		return set;
	}

	@JsonIgnore
	public static Set<Certificate> resolveAllSigners(final Collection<Certificate> someCerts) {
		final Set<Certificate> set = new LinkedHashSet<>();
		for (final Certificate c : someCerts) {
			set.addAll(Certificate.resolveAllSigners(c));
		}
		return set;
	}

	@Id
	private String id = UUID.randomUUID().toString();
	@Column(columnDefinition = "LONGTEXT")
	@Type(type = "encryptedString")
	private String privateKey;

	@Column(columnDefinition = "LONGTEXT")
	private String cert;

	@OneToOne
	private CertificateProvider certificateProvider;

	@OneToOne(cascade = { CascadeType.ALL })
	private CertificateSubject subject = new CertificateSubject();

	private int daysValidFor;
	private int role = 0;

	@ManyToOne
	private Certificate signer;
	
	@OneToMany(mappedBy = "signer", cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Set<Certificate> signees;
	private boolean selfSigned = false;
	private boolean upstream = false;

	private boolean downstream = false;

	@Temporal(value = TemporalType.TIMESTAMP)
	private Date signedOn = new Date();

	public Certificate() {
		subject.setNsComment("UUID:" + id);
	}

	public Certificate(final String anId) {
		id = anId;
		subject.setNsComment("UUID:" + id);
	}

	@JsonAnySetter
	public void anySetter(final String aKey, final Object aValue) {
		subject.put(aKey, aValue.toString());
	}

	@JsonIgnore
	public String buildSignerChain() {
		if (signer == null) {
			return cert;
		}
		final StringBuffer buffer = new StringBuffer();
		for (final Certificate c : getSignerChain()) {
			buffer.append(c.getCert());
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final Certificate rhs = (Certificate) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(id, rhs.id).isEquals();
	}

	public String getCert() {
		return cert;
	}

	public CertificateProvider getCertificateProvider() {
		return certificateProvider;
	}

	public int getDaysValidFor() {
		return daysValidFor;
	}

	public String getId() {
		return id;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public int getRole() {
		return role;
	}

	public Date getSignedOn() {
		return signedOn;
	}

	@JsonIgnore
	public Set<Certificate> getSignees() {
		return signees;
	}

	public Certificate getSigner() {
		return signer;
	}

	@JsonIgnore
	public Set<Certificate> getSignerChain() {
		return Certificate.resolveAllSigners(getSigner());
	}

	@JsonIgnore
	public Set<Certificate> getSigningChain() {
		return Certificate.resolveAllSigners(this);
	}

	public CertificateSubject getSubject() {
		return subject;
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		return builder.toHashCode();
	}


	public boolean isDownstream() {
		return downstream;
	}

	public boolean isSelfSigned() {
		return selfSigned;
	}

	public boolean isUpstream() {
		return upstream;
	}

	public void setCert(final String cert) {
		this.cert = cert;
	}

	public void setCertificateProvider(final CertificateProvider certificateProvider) {
		this.certificateProvider = certificateProvider;
	}

	public void setDaysValidFor(final int daysValidFor) {
		this.daysValidFor = daysValidFor;
	}

	public void setDownstream(final boolean downstream) {
		this.downstream = downstream;
	}

	public void setId(final String uuid) {
		this.id = uuid;
	}

	public void setPrivateKey(final String privateKey) {
		this.privateKey = privateKey;
	}

	public void setRole(final int role) {
		this.role = role;
	}

	public void setSelfSigned(final boolean selfSigned) {
		this.selfSigned = selfSigned;
	}

	public void setSignedOn(final Date signedOn) {
		this.signedOn = signedOn;
	}

	public void setSignees(final Set<Certificate> signees) {
		this.signees = signees;
	}

	public void setSigner(final Certificate signer) {
		this.signer = signer;
	}

	public void setSubject(final CertificateSubject subject) {
		this.subject = subject;
	}

	public void setUpstream(final boolean upstream) {
		this.upstream = upstream;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("id", id);
		return builder.toString();
	}

}
