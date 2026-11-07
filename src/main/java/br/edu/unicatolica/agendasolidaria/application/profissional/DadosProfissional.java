package br.edu.unicatolica.agendasolidaria.application.profissional;

import br.edu.unicatolica.agendasolidaria.domain.model.Disponibilidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;

import java.util.List;

public record DadosProfissional(String nome,
                                Especialidade especialidade,
                                String registroProfissional,
                                int duracaoAtendimentoMinutos,
                                List<Disponibilidade> disponibilidades) {
}
