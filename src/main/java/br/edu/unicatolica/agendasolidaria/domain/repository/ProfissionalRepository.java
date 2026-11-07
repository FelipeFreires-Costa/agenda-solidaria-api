package br.edu.unicatolica.agendasolidaria.domain.repository;

import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;

import java.util.List;
import java.util.Optional;

public interface ProfissionalRepository {

    Profissional salvar(Profissional profissional);

    Optional<Profissional> buscarPorId(Long id);

    List<Profissional> listar(Especialidade especialidade, boolean somenteAtivos);
}
