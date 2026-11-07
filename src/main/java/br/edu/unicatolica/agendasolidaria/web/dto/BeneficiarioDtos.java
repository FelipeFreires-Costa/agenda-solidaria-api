package br.edu.unicatolica.agendasolidaria.web.dto;

import br.edu.unicatolica.agendasolidaria.application.beneficiario.DadosBeneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class BeneficiarioDtos {

    private BeneficiarioDtos() {
    }

    public record BeneficiarioRequest(
            @Schema(example = "Maria das Graças Oliveira") @NotBlank @Size(max = 120) String nome,
            @Schema(example = "610.531.783-28") @NotBlank String cpf,
            @Schema(example = "1970-05-10") @NotNull @Past LocalDate dataNascimento,
            @Schema(example = "(88) 99999-0000") @NotBlank @Size(max = 20) String telefone,
            @Schema(example = "maria@email.com") @Email @Size(max = 120) String email) {

        public DadosBeneficiario paraComando() {
            return new DadosBeneficiario(nome, cpf, dataNascimento, telefone, email);
        }
    }

    /**
     * O CPF é devolvido mascarado (LGPD - princípio da necessidade).
     */
    public record BeneficiarioResponse(Long id, String nome, String cpf, LocalDate dataNascimento, int idade,
                                       String telefone, String email, boolean ativo) {

        public static BeneficiarioResponse de(Beneficiario b) {
            String cpf = b.getCpf().formatado();
            String mascarado = "***." + cpf.substring(4, 11) + "-**";
            return new BeneficiarioResponse(b.getId(), b.getNome(), mascarado, b.getDataNascimento(), b.idade(),
                    b.getTelefone(), b.getEmail(), b.isAtivo());
        }
    }
}
