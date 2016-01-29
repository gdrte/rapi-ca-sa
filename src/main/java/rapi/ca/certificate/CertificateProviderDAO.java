package rapi.ca.certificate;

import rapi.ca.certificate.model.CertificateProvider;
import rapi.ca.rest.AbstractDAO;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CertificateProviderDAO extends AbstractDAO<String, CertificateProvider> {

	@PersistenceContext
	private EntityManager entityManager;

	public CertificateProviderDAO(EntityManager entityManager) {
		this.entityManager = entityManager;
	}


	public List<CertificateProvider> list(){
		return entityManager.createQuery("from CertificateProvider", CertificateProvider.class).getResultList();
	}
	
	public CertificateProvider show(final String anId){
		return entityManager.createQuery("from CertificateProvider where id=:id", CertificateProvider.class).setParameter("id", anId).getSingleResult();
	}
	
}
