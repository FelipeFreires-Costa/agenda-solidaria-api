package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Pessoa da comunidade atendida pela associação (entidade / raiz de agregado).
 */
public class Beneficiario {

    private Long id;
    private String nome;
    private final Cpf cpf;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private boolean ativo;

    private Beneficiario(Long id, String nome, Cpf cpf, LocalDate dataNascimento,
                         String telefone, String email, boolean ativo) {
        this.id = id;
        this.cpf = Objects.requireNonNull(cpf, "CPF é obrigatório");
        this.ativo = ativo;
        alterarDados(nome, dataNascimento, telefone, email);
    }

    public static Beneficiario novo(String nome, Cpf cpf, LocalDate dataNascimento, String telefone, String email) {
        return new Beneficiario(null, nome, cpf, dataNascimento, telefone, email, true);
    }

    public static Beneficiario reconstituir(Long id, String nome, Cpf cpf, LocalDate dataNascimento,
                                            String telefone, String email, boolean ativo) {
        return new Beneficiario(id, nome, cpf, dataNascimento, telefone, email, ativo);
    }

    public void alterarDados(String nome, LocalDate dataNascimento, String telefone, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome do beneficiário é obrigatório");
        }
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Data de nascimento inválida");
        }
        if (telefone == null || telefone.isBlank()) {
            throw new RegraNegocioException("Telefone é obrigatório para contato e lembretes");
        }
        this.nome = nome.trim();
        this.dataNascimento = dataNascimento;
        this.telefone = telefone.trim();
        this.email = email == null || email.isBlank() ? null : email.trim();
    }

    public int idade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public void inativar() {
        this.ativo = false;
    }

    public void definirId(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Cpf getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public boolean isAtivo() { return ativo; }
}
