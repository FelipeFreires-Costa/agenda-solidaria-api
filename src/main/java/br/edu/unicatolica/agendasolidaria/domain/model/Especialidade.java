package br.edu.unicatolica.agendasolidaria.domain.model;

public enum Especialidade {
    PSICOLOGIA("Psicologia"),
    NUTRICAO("Nutrição"),
    FISIOTERAPIA("Fisioterapia"),
    ASSISTENCIA_SOCIAL("Assistência Social"),
    ODONTOLOGIA("Odontologia"),
    ORIENTACAO_JURIDICA("Orientação Jurídica");

    private final String descricao;

    Especialidade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
