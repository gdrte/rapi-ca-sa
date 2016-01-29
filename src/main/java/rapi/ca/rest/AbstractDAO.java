package rapi.ca.rest;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractDAO<IdType, DTOType extends DataTransferObject<IdType>> {
	@PersistenceContext
	protected EntityManager entityManager;

	public DTOType find(final IdType id) {
		return entityManager.find(getDTOClass(), id);
	}

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
	public final Class<DTOType> getDTOClass() {
		return (Class<DTOType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public List<DTOType> list() {
		return entityManager.createQuery("from " + getDTOClass().getName(), getDTOClass()).getResultList();
	}

	public DTOType merge(final DTOType aT) {
		return entityManager.merge(aT);
	}

	public void persist(final DTOType aT) {
		entityManager.persist(aT);
	}

	public DTOType persistOrMerge(final DTOType aT) {
		if (entityManager.find(getDTOClass(), aT.getId()) != null) {
			return entityManager.merge(aT);
		}
		entityManager.persist(aT);
		return aT;
	}

	public void remove(final DTOType aT) {
		entityManager.remove(aT);
	}

	public DTOType getReference(final IdType id) {
		return entityManager.getReference(getDTOClass(), id);
	}

	public void removeRef(final IdType id) {
		entityManager.remove(getReference(id));
	}

	public void removeRef(final DTOType aT) {
		entityManager.remove(getReference(aT.getId()));
	}

	public void removeAll(final Collection<DTOType> aT) {
		for (DTOType t : aT) {
			entityManager.remove(t);
		}
	}

	public DTOType findById(final String id) {
		try {
			return entityManager.find(getDTOClass(), id);
		} catch (final Exception e) {
			return null;
		}
	}
	public DTOType detach(final DTOType aT) {
		try{
		entityManager.detach(aT);
		return aT;
		}catch(final Exception e){
			return null;
		}
	}
}
