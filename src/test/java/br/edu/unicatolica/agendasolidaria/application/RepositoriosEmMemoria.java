package br.edu.unicatolica.agendasolidaria.application;

import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Cpf;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.BeneficiarioRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.FiltroAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.repository.ProfissionalRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Implementações em memória das portas do domínio: graças ao DIP, os casos de uso são testados
 * sem banco de dados e sem Spring.
 */
public final class RepositoriosEmMemoria {

    private RepositoriosEmMemoria() {
    }

    public static class Beneficiarios implements BeneficiarioRepository {
        private final Map<Long, Beneficiario> dados = new LinkedHashMap<>();
        private long sequencia;

        @Override
        public Beneficiario salvar(Beneficiario b) {
            if (b.getId() == null) {
                b.definirId(++sequencia);
            }
            dados.put(b.getId(), b);
            return b;
        }

        @Override
        public Optional<Beneficiario> buscarPorId(Long id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public boolean existePorCpf(Cpf cpf) {
            return dados.values().stream().anyMatch(b -> b.getCpf().equals(cpf));
        }

        @Override
        public List<Beneficiario> listar(String nomeContem, boolean somenteAtivos) {
            return dados.values().stream()
                    .filter(b -> nomeContem == null || b.getNome().toLowerCase().contains(nomeContem.toLowerCase()))
                    .filter(b -> !somenteAtivos || b.isAtivo())
                    .toList();
        }
    }

    public static class Profissionais implements ProfissionalRepository {
        private final Map<Long, Profissional> dados = new LinkedHashMap<>();
        private long sequencia;

        @Override
        public Profissional salvar(Profissional p) {
            if (p.getId() == null) {
                p.definirId(++sequencia);
            }
            dados.put(p.getId(), p);
            return p;
        }

        @Override
        public Optional<Profissional> buscarPorId(Long id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public List<Profissional> listar(Especialidade especialidade, boolean somenteAtivos) {
            return dados.values().stream()
                    .filter(p -> especialidade == null || p.getEspecialidade() == especialidade)
                    .filter(p -> !somenteAtivos || p.isAtivo())
                    .toList();
        }
    }

    public static class Agendamentos implements AgendamentoRepository {
        private final Map<Long, Agendamento> dados = new LinkedHashMap<>();
        private long sequencia;

        @Override
        public Agendamento salvar(Agendamento a) {
            if (a.getId() == null) {
                a.definirId(++sequencia);
            }
            dados.put(a.getId(), a);
            return a;
        }

        @Override
        public Optional<Agendamento> buscarPorId(Long id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public List<Agendamento> listarPorProfissionalEntre(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
            return buscar(a -> a.getProfissionalId().equals(profissionalId) && entre(a, inicio, fim));
        }

        @Override
        public List<Agendamento> listarPorBeneficiarioEntre(Long beneficiarioId, LocalDateTime inicio, LocalDateTime fim) {
            return buscar(a -> a.getBeneficiarioId().equals(beneficiarioId) && entre(a, inicio, fim));
        }

        @Override
        public List<Agendamento> listarFuturosPorBeneficiario(Long beneficiarioId, LocalDateTime aPartirDe) {
            return buscar(a -> a.getBeneficiarioId().equals(beneficiarioId) && !a.getInicio().isBefore(aPartirDe));
        }

        @Override
        public List<Agendamento> filtrar(FiltroAgendamento f) {
            return buscar(a -> (f.profissionalId() == null || a.getProfissionalId().equals(f.profissionalId()))
                    && (f.beneficiarioId() == null || a.getBeneficiarioId().equals(f.beneficiarioId()))
                    && (f.status() == null || a.getStatus() == f.status())
                    && (f.inicioApartirDe() == null || !a.getInicio().isBefore(f.inicioApartirDe()))
                    && (f.inicioAntesDe() == null || a.getInicio().isBefore(f.inicioAntesDe())));
        }

        private static boolean entre(Agendamento a, LocalDateTime inicio, LocalDateTime fim) {
            return !a.getInicio().isBefore(inicio) && a.getInicio().isBefore(fim);
        }

        private List<Agendamento> buscar(Predicate<Agendamento> criterio) {
            return new ArrayList<>(dados.values().stream().filter(criterio)
                    .sorted(Comparator.comparing(Agendamento::getInicio)).toList());
        }
    }

    /**
     * Relógio controlável para simular a passagem do tempo nos testes.
     */
    public static class RelogioAjustavel extends Clock {
        private Instant agora;
        private final ZoneId zona;

        public RelogioAjustavel(LocalDateTime inicial, ZoneId zona) {
            this.agora = inicial.atZone(zona).toInstant();
            this.zona = zona;
        }

        public void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return zona;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new RelogioAjustavel(LocalDateTime.ofInstant(agora, zona), zone);
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
