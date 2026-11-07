package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.adapter;

import br.edu.unicatolica.agendasolidaria.domain.model.Disponibilidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.domain.repository.ProfissionalRepository;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.DisponibilidadeEmbeddable;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.ProfissionalEntity;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.jpa.ProfissionalJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProfissionalRepositoryAdapter implements ProfissionalRepository {

    private final ProfissionalJpaRepository jpa;

    public ProfissionalRepositoryAdapter(ProfissionalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Profissional salvar(Profissional profissional) {
        ProfissionalEntity salvo = jpa.save(paraEntidade(profissional));
        profissional.definirId(salvo.getId());
        return profissional;
    }

    @Override
    public Optional<Profissional> buscarPorId(Long id) {
        return jpa.findById(id).map(ProfissionalRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Profissional> listar(Especialidade especialidade, boolean somenteAtivos) {
        Specification<ProfissionalEntity> spec = Specification.where(null);
        if (especialidade != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("especialidade"), especialidade));
        }
        if (somenteAtivos) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("ativo")));
        }
        return jpa.findAll(spec, Sort.by("nome")).stream()
                .map(ProfissionalRepositoryAdapter::paraDominio)
                .toList();
    }

    private static ProfissionalEntity paraEntidade(Profissional p) {
        ProfissionalEntity e = new ProfissionalEntity();
        e.setId(p.getId());
        e.setNome(p.getNome());
        e.setEspecialidade(p.getEspecialidade());
        e.setRegistroProfissional(p.getRegistroProfissional());
        e.setDuracaoAtendimentoMinutos(p.getDuracaoAtendimentoMinutos());
        e.setAtivo(p.isAtivo());
        e.setDisponibilidades(new ArrayList<>(p.getDisponibilidades().stream()
                .map(d -> new DisponibilidadeEmbeddable(d.diaSemana(), d.horaInicio(), d.horaFim()))
                .toList()));
        return e;
    }

    private static Profissional paraDominio(ProfissionalEntity e) {
        List<Disponibilidade> disponibilidades = e.getDisponibilidades().stream()
                .map(d -> new Disponibilidade(d.getDiaSemana(), d.getHoraInicio(), d.getHoraFim()))
                .toList();
        return Profissional.reconstituir(e.getId(), e.getNome(), e.getEspecialidade(), e.getRegistroProfissional(),
                e.getDuracaoAtendimentoMinutos(), disponibilidades, e.isAtivo());
    }
}
