package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.jpa;

import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.BeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiarioJpaRepository extends JpaRepository<BeneficiarioEntity, Long> {

    boolean existsByCpf(String cpf);

    List<BeneficiarioEntity> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    List<BeneficiarioEntity> findByNomeContainingIgnoreCaseAndAtivoTrueOrderByNome(String nome);
}
