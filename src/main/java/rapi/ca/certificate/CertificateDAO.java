package rapi.ca.certificate;

import rapi.ca.certificate.model.Certificate;
import rapi.ca.rest.AbstractDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateDAO extends AbstractDAO<String, Certificate> {
	public Certificate findByCommonName(final String aCommonName){
		return entityManager.createQuery("from Certificate where subject.commonName like :commonName", Certificate.class).setParameter("commonName", aCommonName).getSingleResult();
	}

	
	public List<Certificate> findSigners(){
		return entityManager.createQuery("from Certificate where role>0", Certificate.class).getResultList();
	}
}
