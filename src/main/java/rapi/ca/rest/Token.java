package rapi.ca.rest;

import java.io.Serializable;

public class Token implements Serializable {

	private static final long serialVersionUID = 558710544261035380L;
	String expires;
	String id;

	public String getExpires() {
		return expires;
	}

	public String getId() {
		return id;
	}


	public void setExpires(final String expires) {
		this.expires = expires;
	}

	public void setId(final String id) {
		this.id = id;
	}

}
