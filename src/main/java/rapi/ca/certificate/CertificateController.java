package rapi.ca.certificate;

import rapi.ca.certificate.model.Certificate;
import rapi.ca.certificate.model.CertificateSigningRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/certificate")
public class CertificateController {
	@Autowired
	CertificateService certificateService;


	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@Transactional
	@RequestMapping(value = "", method = RequestMethod.POST)
	public @ResponseBody
	Certificate add(@RequestBody Certificate aCert) {
		return certificateService.add(aCert);
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@Transactional
	@RequestMapping(value = "/{id}/signerBundleChain", method = RequestMethod.GET)
	public void bundleSignerChain(@PathVariable String id,
			HttpServletResponse aResponse) throws IOException {
		final Certificate cert = certificateService.find(id);
		final String signers = Certificate
				.buildCABundle(cert.getSigningChain());
		final ByteArrayInputStream stream = new ByteArrayInputStream(
				signers.getBytes());
		aResponse.setContentType("application/x-pem-file");
		aResponse.setHeader("Content-Disposition",
				"attachment; filename=caBundle.cert");
		IOUtils.copy(stream, aResponse.getOutputStream());
		aResponse.flushBuffer();
	}

	@Transactional
	@RequestMapping(value = "/{id}/signerBundle", method = RequestMethod.GET)
	public void bundleSigners(@PathVariable String id,
			HttpServletResponse aResponse) throws IOException {
		final Certificate cert = certificateService.find(id);
		// final String signers =
		// Certificate.buildCABundle(cert.getSignerChain());
		final String signers = Certificate.buildCABundle(certificateService
				.resolveAllSigners(id));
		final ByteArrayInputStream stream = new ByteArrayInputStream(
				signers.getBytes());
		aResponse.setContentType("application/x-pem-file");
		aResponse.setHeader("Content-Disposition",
				"attachment; filename=caBundle.cert");
		IOUtils.copy(stream, aResponse.getOutputStream());
		aResponse.flushBuffer();
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@Transactional
	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	public @ResponseBody
	void delete(@PathVariable String id) {
		certificateService.delete(id);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@RequestMapping(value = "/ca", method = RequestMethod.POST)
	@Transactional
	public @ResponseBody
	Certificate genCA(@RequestBody Certificate aCert) {
		return certificateService.genCA(aCert);
	}



	@Transactional
	public CertificateSigningRequest getCSRForCert(Certificate cert) {
		return certificateService.getCSRForCert(cert);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@RequestMapping(value = "", method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	List<Certificate> list() throws IOException {
		return certificateService.list();
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@RequestMapping(value = "/signers", method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	List<Certificate> listSigners() throws IOException {
		return certificateService.listSigners();
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@Transactional
	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, String> refresh() {
		return certificateService.refresh();
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	Certificate show(@PathVariable String id) throws IOException {
		return certificateService.show(id);
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@RequestMapping(value = "/{id}/cert", method = RequestMethod.GET, produces = "application/x-pem-file")
	@Transactional
	public void showCertificate(@PathVariable String id,
			HttpServletResponse aResponse) throws IOException {
		final Certificate cert = certificateService.find(id);
		final ByteArrayInputStream stream = new ByteArrayInputStream(cert
				.getCert().getBytes());
		aResponse.setContentType("application/x-pem-file");
		aResponse.setHeader("Content-Disposition", "attachment; filename="
				+ cert.getSubject().getCommonName() + ".cert");
		IOUtils.copy(stream, aResponse.getOutputStream());
		aResponse.flushBuffer();
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@RequestMapping(value = "/{id}/exposed", method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	Certificate showEverything(@PathVariable String id) throws IOException {
		return certificateService.showEverything(id);
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@RequestMapping(value = "/{id}/privateKey", method = RequestMethod.GET, produces = "application/x-pem-file")
	@Transactional
	public @ResponseBody
	void showPrivateKey(@PathVariable String id, HttpServletResponse aResponse)
			throws IOException {
		final Certificate cert = certificateService.find(id);
		final ByteArrayInputStream stream = new ByteArrayInputStream(cert
				.getPrivateKey().getBytes());
		aResponse.setContentType("application/x-pem-file");
		aResponse.setHeader("Content-Disposition", "attachment; filename="
				+ cert.getSubject().getCommonName() + ".key");
		IOUtils.copy(stream, aResponse.getOutputStream());
		aResponse.flushBuffer();
	}

	public Certificate sign(Certificate signer, Certificate signee)
			throws UnknownHostException {
		return certificateService.sign(signer, signee);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@Transactional
	@RequestMapping(value = "/signCSR", method = RequestMethod.POST)
	public @ResponseBody
	Certificate signCSR(@RequestBody CertificateSigningRequest csr) {
		return certificateService.signCSR(csr);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@Transactional
	@RequestMapping(value = { "", "/{id}" }, method = RequestMethod.PUT)
	public @ResponseBody
	Certificate update(@RequestBody Certificate aT) {
		return certificateService.update(aT);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@Transactional
	@RequestMapping(value = { "/upload" }, method = RequestMethod.POST)
	public @ResponseBody
	Certificate uploadCert(@RequestBody final Certificate aCert) {

		certificateService.save(aCert);

		return aCert;

	}

}
