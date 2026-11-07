package br.edu.unicatolica.agendasolidaria.domain.repository;

import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Cpf;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída (Clean Architecture). O domínio define o contrato; a infraestrutura o implementa (DIP).
 */
public interface BeneficiarioRepository {

    Beneficiario salvar(Beneficiario beneficiario);

    Optional<Beneficiario> buscarPorId(Long id);

    boolean existePorCpf(Cpf cpf);

    List<Beneficiario> listar(String nomeContem, boolean somenteAtivos);
}
