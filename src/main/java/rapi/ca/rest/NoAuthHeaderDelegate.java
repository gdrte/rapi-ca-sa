package rapi.ca.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


public class NoAuthHeaderDelegate implements HttpHeaderDelegate {
	@Override
	public HttpHeaders getHttpHeaders() {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return headers;
	}

}
