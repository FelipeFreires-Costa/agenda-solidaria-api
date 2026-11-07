package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @Test
    void aceitaCpfValidoComOuSemMascara() {
        assertThat(new Cpf("529.982.247-25").valor()).isEqualTo("52998224725");
        assertThat(new Cpf("52998224725").formatado()).isEqualTo("529.982.247-25");
    }

    @Test
    void cpfsComMesmoNumeroSaoIguais() {
        assertThat(new Cpf("529.982.247-25")).isEqualTo(new Cpf("52998224725"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-24", "111.111.111-11", "123", "abc.def.ghi-jk", "5299822472500"})
    void rejeitaCpfInvalido(String cpf) {
        assertThatThrownBy(() -> new Cpf(cpf))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    void rejeitaCpfVazio() {
        assertThatThrownBy(() -> new Cpf(" ")).isInstanceOf(RegraNegocioException.class);
    }
}
