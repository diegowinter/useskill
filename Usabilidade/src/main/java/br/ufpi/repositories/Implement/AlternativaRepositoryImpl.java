package br.ufpi.repositories.Implement;

import javax.persistence.EntityManager;

import br.ufpi.models.Alternativa;
import br.ufpi.repositories.AlternativaRepository;
import br.ufpi.repositories.Repository;

public class AlternativaRepositoryImpl extends Repository<Alternativa, Long>
		implements AlternativaRepository {
	public AlternativaRepositoryImpl(EntityManager entityManager) {
		super(entityManager);
	}

}
