package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity;

import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profissional")
public class ProfissionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Especialidade especialidade;

    @Column(name = "registro_profissional", length = 30)
    private String registroProfissional;

    @Column(name = "duracao_atendimento_minutos", nullable = false)
    private int duracaoAtendimentoMinutos;

    @Column(nullable = false)
    private boolean ativo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profissional_disponibilidade", joinColumns = @JoinColumn(name = "profissional_id"))
    private List<DisponibilidadeEmbeddable> disponibilidades = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
    public String getRegistroProfissional() { return registroProfissional; }
    public void setRegistroProfissional(String registroProfissional) { this.registroProfissional = registroProfissional; }
    public int getDuracaoAtendimentoMinutos() { return duracaoAtendimentoMinutos; }
    public void setDuracaoAtendimentoMinutos(int duracao) { this.duracaoAtendimentoMinutos = duracao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public List<DisponibilidadeEmbeddable> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<DisponibilidadeEmbeddable> disponibilidades) {
        this.disponibilidades = disponibilidades;
    }
}
