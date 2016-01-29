package rapi.ca.certificate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
public class CertificateSubject implements Serializable {
	private static final long serialVersionUID = -4396853624556228364L;
	private static final Logger log = LoggerFactory.getLogger(CertificateSubject.class);
	@Id
	private String id = UUID.randomUUID().toString();
	private String countryName;
	private String state;
	private String locality;
	private String organization;
	private String organizationalUnit;
	private String commonName;
	private String emailAddress;
	private String subjectAltName;
	private String nsComment;
	private int pathlen = -1;
	private boolean CA = false;
	@JsonIgnore
	transient Map<String,String> subject=new HashMap<String,String>();
	
	@JsonIgnore
	public Map<String, String> getOptions() {
		if(null==subject){
			subject=new HashMap<String,String>();
			subject.put("C",getC());
			subject.put("CA",Boolean.toString(getCA()));
			subject.put("CN",getCN());
			subject.put("L",getL());
			subject.put("O",getO());
			subject.put("OU",getOU());
			subject.put("ST",getST());
			subject.put("emailAddress", getEmailAddress());
		}
		return subject;
	}

	@JsonProperty("C")
	public String getC() {
		return countryName;
	}

	@JsonProperty("CA")
	public boolean getCA() {
		return CA;
	}

	@JsonProperty("CN")
	public String getCN() {
		return commonName;
	}

	@JsonIgnore
	public String getCommonName() {
		return commonName;
	}

	@JsonIgnore
	public String getCountryName() {
		return countryName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getId() {
		return id;
	}

	@JsonProperty("L")
	public String getL() {
		return locality;
	}

	@JsonIgnore
	public String getLocality() {
		return locality;
	}

	@JsonProperty("nsComment")
	public String getNsComment() {
		return nsComment;
	}

	@JsonProperty("O")
	public String getO() {
		return organization;
	}

	@JsonIgnore
	public String getOrganization() {
		return organization;
	}

	@JsonIgnore
	public String getOrganizationalUnit() {
		return organizationalUnit;
	}

	@JsonProperty("OU")
	public String getOU() {
		return organizationalUnit;
	}

	@JsonProperty("pathlen")
	public int getPathlen() {
		return pathlen;
	}

	@JsonProperty("ST")
	public String getST() {
		return state;
	}

	@JsonIgnore
	public String getState() {
		return state;
	}

	public String getSubjectAltName() {
		return subjectAltName;
	}

	public void put(final String aKey, final String aValue) {
		switch (aKey) {
			case "C":
			case "countryName":
				setCountryName(aValue);
				subject.put("C", aValue);
				return;
			case "CA":
				setCA(Boolean.parseBoolean(aValue));
				subject.put("CA", aValue);
				return;
			case "pathlen":
				try {
					setPathlen(Integer.parseInt(aValue));
				} catch (final Exception e) {
					CertificateSubject.log.warn("Couldn't parse an Integer from " + aValue + " for pathlen.");
				}
				subject.put("pathlen", aValue);
				return;
			case "CN":
			case "commonName":
				setCN(aValue);
				subject.put("CN", aValue);
				return;
			case "state":
			case "ST":
				setST(aValue);
				subject.put("ST", aValue);
				return;
			case "locality":
			case "L":
				setL(aValue);
				subject.put("L", aValue);
				return;
			case "organization":
			case "O":
				setO(aValue);
				subject.put("O", aValue);
				return;
			case "organizationalUnit":
			case "OU":
				setOU(aValue);
				subject.put("OU", aValue);
				return;
			case "emailAddress":
				setEmailAddress(aValue);
				subject.put("emailAddress", aValue);
				return;
			case "subjectAltName":
				setSubjectAltName(aValue);
				subject.put("subjectAltName", aValue);
				return;
			case "nsComment":
				setNsComment(aValue);
				subject.put("nsComment", aValue);
				return;
			default:
				return;
		}
	}

	public void setC(final String countryName) {
		this.countryName = countryName;
		subject.put("C", countryName);
	}

	public void setCA(final boolean cA) {
		CA = cA;
		subject.put("CA", Boolean.toString(cA));
	}

	public void setCN(final String commonName) {
		this.commonName = commonName;
		subject.put("CN", commonName);
	}

	public void setCommonName(final String commonName) {
		this.commonName = commonName;
		setCN(commonName);
	}

	public void setCountryName(final String countryName) {
		this.countryName = countryName;
		setC(countryName);
	}

	public void setEmailAddress(final String emailAddress) {
		this.emailAddress = emailAddress;
		subject.put("emailAddress", emailAddress);
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setL(final String locality) {
		this.locality = locality;
		subject.put("L", locality);
	}

	public void setLocality(final String locality) {
		this.locality = locality;
		setL(locality);
	}

	@JsonProperty("nsComment")
	public void setNsComment(final String nsComment) {
		this.nsComment = nsComment;
		subject.put("nsComment", nsComment);
	}

	public void setO(final String organization) {
		this.organization = organization;
		subject.put("O", organization);
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
		setO(organization);
	}

	public void setOrganizationalUnit(final String organizationalUnit) {
		this.organizationalUnit = organizationalUnit;
		setOU(organizationalUnit);
		
	}

	public void setOU(final String organizationalUnit) {
		this.organizationalUnit = organizationalUnit;
		subject.put("OU", organizationalUnit);
	}

	public void setPathlen(final int pathlen) {
		this.pathlen = pathlen;
		subject.put("pathlen", Integer.toString(pathlen));
	}

	public void setST(final String state) {
		this.state = state;
		subject.put("ST", state);
	}

	public void setState(final String state) {
		this.state = state;
		setST(state);
	}

	public void setSubjectAltName(final String subjectAltName) {
		this.subjectAltName = subjectAltName;
		subject.put("subjectAltName", subjectAltName);
	}

}
