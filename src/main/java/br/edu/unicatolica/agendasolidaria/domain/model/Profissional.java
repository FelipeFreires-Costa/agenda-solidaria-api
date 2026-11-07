package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Profissional voluntário (psicólogo, nutricionista, etc.) que atende na associação.
 */
public class Profissional {

    private static final int DURACAO_MINIMA = 10;
    private static final int DURACAO_MAXIMA = 180;

    private Long id;
    private String nome;
    private Especialidade especialidade;
    private String registroProfissional;
    private int duracaoAtendimentoMinutos;
    private final List<Disponibilidade> disponibilidades = new ArrayList<>();
    private boolean ativo;

    private Profissional(Long id, String nome, Especialidade especialidade, String registroProfissional,
                         int duracaoAtendimentoMinutos, List<Disponibilidade> disponibilidades, boolean ativo) {
        this.id = id;
        this.ativo = ativo;
        alterarDados(nome, especialidade, registroProfissional, duracaoAtendimentoMinutos);
        definirDisponibilidades(disponibilidades);
    }

    public static Profissional novo(String nome, Especialidade especialidade, String registroProfissional,
                                    int duracaoAtendimentoMinutos, List<Disponibilidade> disponibilidades) {
        return new Profissional(null, nome, especialidade, registroProfissional,
                duracaoAtendimentoMinutos, disponibilidades, true);
    }

    public static Profissional reconstituir(Long id, String nome, Especialidade especialidade, String registroProfissional,
                                            int duracaoAtendimentoMinutos, List<Disponibilidade> disponibilidades,
                                            boolean ativo) {
        return new Profissional(id, nome, especialidade, registroProfissional,
                duracaoAtendimentoMinutos, disponibilidades, ativo);
    }

    public void alterarDados(String nome, Especialidade especialidade, String registroProfissional,
                             int duracaoAtendimentoMinutos) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome do profissional é obrigatório");
        }
        if (duracaoAtendimentoMinutos < DURACAO_MINIMA || duracaoAtendimentoMinutos > DURACAO_MAXIMA) {
            throw new RegraNegocioException("Duração do atendimento deve estar entre "
                    + DURACAO_MINIMA + " e " + DURACAO_MAXIMA + " minutos");
        }
        this.nome = nome.trim();
        this.especialidade = Objects.requireNonNull(especialidade, "Especialidade é obrigatória");
        this.registroProfissional = registroProfissional == null ? null : registroProfissional.trim();
        this.duracaoAtendimentoMinutos = duracaoAtendimentoMinutos;
    }

    public void definirDisponibilidades(List<Disponibilidade> novas) {
        List<Disponibilidade> lista = novas == null ? List.of() : novas;
        for (int i = 0; i < lista.size(); i++) {
            for (int j = i + 1; j < lista.size(); j++) {
                if (lista.get(i).sobrepoe(lista.get(j))) {
                    throw new RegraNegocioException("Há janelas de disponibilidade sobrepostas em " + lista.get(i).diaSemana());
                }
            }
        }
        disponibilidades.clear();
        disponibilidades.addAll(lista);
    }

    public boolean atendeNoIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        return disponibilidades.stream().anyMatch(d -> d.abrange(inicio, fim));
    }

    /**
     * Information Expert (GRASP): o profissional conhece suas janelas e a duração do atendimento,
     * portanto é ele quem gera os horários possíveis de um dia.
     */
    public List<LocalDateTime> horariosPossiveis(LocalDate data) {
        List<LocalDateTime> horarios = new ArrayList<>();
        disponibilidades.stream()
                .filter(d -> d.diaSemana() == data.getDayOfWeek())
                .sorted((a, b) -> a.horaInicio().compareTo(b.horaInicio()))
                .forEach(d -> {
                    LocalDateTime cursor = data.atTime(d.horaInicio());
                    LocalDateTime limite = data.atTime(d.horaFim());
                    while (!cursor.plusMinutes(duracaoAtendimentoMinutos).isAfter(limite)) {
                        horarios.add(cursor);
                        cursor = cursor.plusMinutes(duracaoAtendimentoMinutos);
                    }
                });
        return horarios;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void definirId(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Especialidade getEspecialidade() { return especialidade; }
    public String getRegistroProfissional() { return registroProfissional; }
    public int getDuracaoAtendimentoMinutos() { return duracaoAtendimentoMinutos; }
    public List<Disponibilidade> getDisponibilidades() { return List.copyOf(disponibilidades); }
    public boolean isAtivo() { return ativo; }
}
