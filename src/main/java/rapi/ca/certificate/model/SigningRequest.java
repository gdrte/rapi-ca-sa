package rapi.ca.certificate.model;

public class SigningRequest {
	
		private String cert;
		private String privateKey;
		private String ca;
		private String message;

		public String getCa() {
			return ca;
		}

		public String getCert() {
			return cert;
		}

		public String getMessage() {
			return message;
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public void setCa(final String ca) {
			this.ca = ca;
		}

		public void setCert(final String cert) {
			this.cert = cert;
		}

		public void setMessage(final String message) {
			this.message = message;
		}

		public void setPrivateKey(final String privateKey) {
			this.privateKey = privateKey;
		}
}
