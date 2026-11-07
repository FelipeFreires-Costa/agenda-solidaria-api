package br.edu.unicatolica.agendasolidaria.application.agendamento;

import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.ContextoAgendamento;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoEvento;
import br.edu.unicatolica.agendasolidaria.domain.event.TipoEventoAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.exception.RecursoNaoEncontradoException;
import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.BeneficiarioRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.FiltroAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.repository.ProfissionalRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Caso de uso central: marcar e acompanhar atendimentos.
 * Coordena o fluxo (GRASP Controller de caso de uso), mas delega as regras às estratégias
 * {@link RegraAgendamento}, as transições ao próprio {@link Agendamento} (State) e os efeitos colaterais
 * aos observadores (Observer).
 */
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final ProfissionalRepository profissionalRepository;
    private final List<RegraAgendamento> regras;
    private final PublicadorEventosAgendamento publicador;
    private final Clock clock;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              BeneficiarioRepository beneficiarioRepository,
                              ProfissionalRepository profissionalRepository,
                              List<RegraAgendamento> regras,
                              PublicadorEventosAgendamento publicador,
                              Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.profissionalRepository = profissionalRepository;
        this.regras = List.copyOf(regras);
        this.publicador = publicador;
        this.clock = clock;
    }

    public AgendamentoDetalhado agendar(SolicitacaoAgendamento solicitacao) {
        Beneficiario beneficiario = buscarBeneficiario(solicitacao.beneficiarioId());
        Profissional profissional = buscarProfissional(solicitacao.profissionalId());
        LocalDateTime agora = agora();

        Agendamento agendamento = Agendamento.novo(beneficiario, profissional, solicitacao.inicio(),
                solicitacao.observacao(), agora);

        ContextoAgendamento contexto = new ContextoAgendamento(beneficiario, profissional,
                agendamento.getInicio(), agendamento.getFim(), agora);
        regras.forEach(regra -> regra.validar(contexto));

        Agendamento salvo = agendamentoRepository.salvar(agendamento);
        return publicarEDetalhar(TipoEventoAgendamento.CRIADO, salvo, beneficiario, profissional);
    }

    public AgendamentoDetalhado confirmar(Long id) {
        return transicionar(id, TipoEventoAgendamento.CONFIRMADO, Agendamento::confirmar);
    }

    public AgendamentoDetalhado cancelar(Long id, String motivo) {
        return transicionar(id, TipoEventoAgendamento.CANCELADO, a -> a.cancelar(motivo));
    }

    public AgendamentoDetalhado registrarComparecimento(Long id) {
        return transicionar(id, TipoEventoAgendamento.REALIZADO, a -> a.registrarComparecimento(agora()));
    }

    public AgendamentoDetalhado registrarFalta(Long id) {
        return transicionar(id, TipoEventoAgendamento.FALTA_REGISTRADA, a -> a.registrarFalta(agora()));
    }

    public AgendamentoDetalhado buscar(Long id) {
        return detalhar(buscarAgendamento(id), new HashMap<>(), new HashMap<>());
    }

    public List<AgendamentoDetalhado> listar(FiltroAgendamento filtro) {
        Map<Long, Beneficiario> beneficiarios = new HashMap<>();
        Map<Long, Profissional> profissionais = new HashMap<>();
        return agendamentoRepository.filtrar(filtro).stream()
                .map(a -> detalhar(a, beneficiarios, profissionais))
                .toList();
    }

    private AgendamentoDetalhado transicionar(Long id, TipoEventoAgendamento tipo, Consumer<Agendamento> acao) {
        Agendamento agendamento = buscarAgendamento(id);
        acao.accept(agendamento);
        Agendamento salvo = agendamentoRepository.salvar(agendamento);
        return publicarEDetalhar(tipo, salvo,
                buscarBeneficiario(salvo.getBeneficiarioId()),
                buscarProfissional(salvo.getProfissionalId()));
    }

    private AgendamentoDetalhado publicarEDetalhar(TipoEventoAgendamento tipo, Agendamento agendamento,
                                                   Beneficiario beneficiario, Profissional profissional) {
        publicador.publicar(new AgendamentoEvento(tipo, agendamento, beneficiario, profissional, agora()));
        return new AgendamentoDetalhado(agendamento, beneficiario, profissional);
    }

    private AgendamentoDetalhado detalhar(Agendamento agendamento,
                                          Map<Long, Beneficiario> cacheBeneficiarios,
                                          Map<Long, Profissional> cacheProfissionais) {
        Beneficiario beneficiario = cacheBeneficiarios.computeIfAbsent(
                agendamento.getBeneficiarioId(), this::buscarBeneficiario);
        Profissional profissional = cacheProfissionais.computeIfAbsent(
                agendamento.getProfissionalId(), this::buscarProfissional);
        return new AgendamentoDetalhado(agendamento, beneficiario, profissional);
    }

    private Agendamento buscarAgendamento(Long id) {
        return agendamentoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }

    private Beneficiario buscarBeneficiario(Long id) {
        return beneficiarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário", id));
    }

    private Profissional buscarProfissional(Long id) {
        return profissionalRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
