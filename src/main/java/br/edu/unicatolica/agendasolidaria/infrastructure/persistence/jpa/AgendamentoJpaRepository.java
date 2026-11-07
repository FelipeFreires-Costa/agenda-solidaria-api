package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.jpa;

import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.AgendamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoJpaRepository extends JpaRepository<AgendamentoEntity, Long>,
        JpaSpecificationExecutor<AgendamentoEntity> {

    List<AgendamentoEntity> findByProfissionalIdAndInicioGreaterThanEqualAndInicioLessThanOrderByInicio(
            Long profissionalId, LocalDateTime inicio, LocalDateTime fim);

    List<AgendamentoEntity> findByBeneficiarioIdAndInicioGreaterThanEqualAndInicioLessThanOrderByInicio(
            Long beneficiarioId, LocalDateTime inicio, LocalDateTime fim);

    List<AgendamentoEntity> findByBeneficiarioIdAndInicioGreaterThanEqualOrderByInicio(
            Long beneficiarioId, LocalDateTime inicio);
}
