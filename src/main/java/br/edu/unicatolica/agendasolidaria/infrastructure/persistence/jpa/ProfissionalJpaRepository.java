package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.jpa;

import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.ProfissionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfissionalJpaRepository extends JpaRepository<ProfissionalEntity, Long>,
        JpaSpecificationExecutor<ProfissionalEntity> {
}
