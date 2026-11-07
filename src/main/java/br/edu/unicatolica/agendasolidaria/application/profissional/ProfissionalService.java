package br.edu.unicatolica.agendasolidaria.application.profissional;

import br.edu.unicatolica.agendasolidaria.domain.exception.RecursoNaoEncontradoException;
import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Disponibilidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.ProfissionalRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public ProfissionalService(ProfissionalRepository profissionalRepository,
                               AgendamentoRepository agendamentoRepository,
                               Clock clock) {
        this.profissionalRepository = profissionalRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    public Profissional cadastrar(DadosProfissional dados) {
        Profissional profissional = Profissional.novo(dados.nome(), dados.especialidade(),
                dados.registroProfissional(), dados.duracaoAtendimentoMinutos(), dados.disponibilidades());
        return profissionalRepository.salvar(profissional);
    }

    public Profissional atualizar(Long id, DadosProfissional dados) {
        Profissional profissional = buscar(id);
        profissional.alterarDados(dados.nome(), dados.especialidade(), dados.registroProfissional(),
                dados.duracaoAtendimentoMinutos());
        profissional.definirDisponibilidades(dados.disponibilidades());
        return profissionalRepository.salvar(profissional);
    }

    public Profissional definirDisponibilidades(Long id, List<Disponibilidade> disponibilidades) {
        Profissional profissional = buscar(id);
        profissional.definirDisponibilidades(disponibilidades);
        return profissionalRepository.salvar(profissional);
    }

    public Profissional buscar(Long id) {
        return profissionalRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    public List<Profissional> listar(Especialidade especialidade, boolean somenteAtivos) {
        return profissionalRepository.listar(especialidade, somenteAtivos);
    }

    public void inativar(Long id) {
        Profissional profissional = buscar(id);
        profissional.inativar();
        profissionalRepository.salvar(profissional);
    }

    /**
     * Retorna os horários ainda livres de um profissional em uma data, descontando os já ocupados
     * e os que já passaram.
     */
    public List<LocalDateTime> horariosLivres(Long id, LocalDate data) {
        Profissional profissional = buscar(id);
        LocalDateTime agora = LocalDateTime.now(clock);
        List<Agendamento> ocupados = agendamentoRepository.listarPorProfissionalEntre(
                id, data.atStartOfDay(), data.plusDays(1).atStartOfDay());
        int duracao = profissional.getDuracaoAtendimentoMinutos();

        return profissional.horariosPossiveis(data).stream()
                .filter(horario -> horario.isAfter(agora))
                .filter(horario -> ocupados.stream()
                        .noneMatch(a -> a.conflitaCom(horario, horario.plusMinutes(duracao))))
                .toList();
    }
}
