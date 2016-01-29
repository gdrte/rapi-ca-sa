package rapi.ca.certificate;

import rapi.ca.certificate.model.Certificate;
import rapi.ca.certificate.model.CertificateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/certificate/provider")
public class CertificateProviderController {
	private static final Logger log = LoggerFactory
			.getLogger(CertificateProviderController.class);

	@Autowired
	CertificateProviderService certificateProviderService;
	
	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@RequestMapping(method = RequestMethod.POST)
	@Transactional
	public @ResponseBody
	CertificateProvider add(@RequestBody final CertificateProvider aT) {
		
		return certificateProviderService.add(aT);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@RequestMapping(method = RequestMethod.DELETE, value = "/{anId}")
	@Transactional
	public @ResponseBody
	HttpStatus delete(@PathVariable String anId) {
		certificateProviderService.delete(anId);
		return HttpStatus.OK;
	}


	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@RequestMapping(method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	List<CertificateProvider> list() throws IOException {
		return certificateProviderService.list();
	}

	//@PreAuthorize(User.ROLE_REVIEWER_MEMBER_ADMIN_MASTER)
	@RequestMapping(value = "/{anId}", method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	CertificateProvider show(@PathVariable final String anId) {
		return certificateProviderService.show(anId);
	}

	//@PreAuthorize(User.ROLE_ADMIN_MASTER)
	@RequestMapping(value = { "", "/{anId}" }, method = RequestMethod.PUT)
	@Transactional
	public @ResponseBody
	CertificateProvider update(@RequestBody final CertificateProvider aT,@PathVariable final String anId) {
		return certificateProviderService.update(aT, anId);
	}
	
	@RequestMapping(value="/{anId}/remotecerts",method = RequestMethod.GET)
	@Transactional
	public @ResponseBody
	List<Certificate> getRemoteCertificates(@PathVariable final String anId) throws IOException {
		CertificateProvider certificateProvider= certificateProviderService.show(anId);
		log.debug("CertificateProvider for getting remote Certs:"+certificateProvider.getHostName());
		return certificateProviderService.getRemoteCertificates(certificateProvider); 
	}

	
}
