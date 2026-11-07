package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.adapter;

import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.FiltroAgendamento;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.AgendamentoEntity;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.jpa.AgendamentoJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AgendamentoRepositoryAdapter implements AgendamentoRepository {

    private final AgendamentoJpaRepository jpa;

    public AgendamentoRepositoryAdapter(AgendamentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        AgendamentoEntity salvo = jpa.save(paraEntidade(agendamento));
        agendamento.definirId(salvo.getId());
        return agendamento;
    }

    @Override
    public Optional<Agendamento> buscarPorId(Long id) {
        return jpa.findById(id).map(AgendamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Agendamento> listarPorProfissionalEntre(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        return paraDominio(jpa.findByProfissionalIdAndInicioGreaterThanEqualAndInicioLessThanOrderByInicio(
                profissionalId, inicio, fim));
    }

    @Override
    public List<Agendamento> listarPorBeneficiarioEntre(Long beneficiarioId, LocalDateTime inicio, LocalDateTime fim) {
        return paraDominio(jpa.findByBeneficiarioIdAndInicioGreaterThanEqualAndInicioLessThanOrderByInicio(
                beneficiarioId, inicio, fim));
    }

    @Override
    public List<Agendamento> listarFuturosPorBeneficiario(Long beneficiarioId, LocalDateTime aPartirDe) {
        return paraDominio(jpa.findByBeneficiarioIdAndInicioGreaterThanEqualOrderByInicio(beneficiarioId, aPartirDe));
    }

    @Override
    public List<Agendamento> filtrar(FiltroAgendamento filtro) {
        Specification<AgendamentoEntity> spec = Specification.where(null);
        if (filtro.profissionalId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("profissionalId"), filtro.profissionalId()));
        }
        if (filtro.beneficiarioId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("beneficiarioId"), filtro.beneficiarioId()));
        }
        if (filtro.status() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), filtro.status()));
        }
        if (filtro.inicioApartirDe() != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("inicio"), filtro.inicioApartirDe()));
        }
        if (filtro.inicioAntesDe() != null) {
            spec = spec.and((r, q, cb) -> cb.lessThan(r.get("inicio"), filtro.inicioAntesDe()));
        }
        return paraDominio(jpa.findAll(spec, Sort.by("inicio")));
    }

    private static List<Agendamento> paraDominio(List<AgendamentoEntity> entidades) {
        return entidades.stream().map(AgendamentoRepositoryAdapter::paraDominio).toList();
    }

    private static AgendamentoEntity paraEntidade(Agendamento a) {
        AgendamentoEntity e = new AgendamentoEntity();
        e.setId(a.getId());
        e.setBeneficiarioId(a.getBeneficiarioId());
        e.setProfissionalId(a.getProfissionalId());
        e.setInicio(a.getInicio());
        e.setFim(a.getFim());
        e.setStatus(a.getStatus());
        e.setObservacao(a.getObservacao());
        e.setMotivoCancelamento(a.getMotivoCancelamento());
        e.setCriadoEm(a.getCriadoEm());
        return e;
    }

    private static Agendamento paraDominio(AgendamentoEntity e) {
        return Agendamento.reconstituir(e.getId(), e.getBeneficiarioId(), e.getProfissionalId(), e.getInicio(),
                e.getFim(), e.getStatus(), e.getObservacao(), e.getMotivoCancelamento(), e.getCriadoEm());
    }
}
