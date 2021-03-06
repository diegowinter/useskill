package br.ufpi.datamining.repositories;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.caelum.vraptor.ioc.Component;
import br.ufpi.datamining.models.ParameterSmellTestDataMining;
import br.ufpi.repositories.Repository;

@Component
public class ParameterSmellTestDataMiningRepository extends Repository<ParameterSmellTestDataMining, Long> {

	public ParameterSmellTestDataMiningRepository(EntityManager entityManager) {
		super(entityManager);
	}
	
	public ParameterSmellTestDataMining getValueParameterSmellTest(Long idTest, Long idParameterSmell) {
		Query query = entityManager.createNamedQuery("ParameterValue.ValueParameterSmellTest");
		query.setParameter("idTest", idTest);
		query.setParameter("idParameterSmell", idParameterSmell);
		
		try {
			return (ParameterSmellTestDataMining) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public void updateValueParameterSmellTest () {
		
	}

}
