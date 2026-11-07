package br.edu.unicatolica.agendasolidaria.application.beneficiario;

import br.edu.unicatolica.agendasolidaria.domain.exception.ConflitoException;
import br.edu.unicatolica.agendasolidaria.domain.exception.RecursoNaoEncontradoException;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Cpf;
import br.edu.unicatolica.agendasolidaria.domain.repository.BeneficiarioRepository;

import java.util.List;

/**
 * Casos de uso de beneficiários. Depende apenas da abstração {@link BeneficiarioRepository} (DIP).
 */
public class BeneficiarioService {

    private final BeneficiarioRepository repository;

    public BeneficiarioService(BeneficiarioRepository repository) {
        this.repository = repository;
    }

    public Beneficiario cadastrar(DadosBeneficiario dados) {
        Cpf cpf = new Cpf(dados.cpf());
        if (repository.existePorCpf(cpf)) {
            throw new ConflitoException("Já existe beneficiário cadastrado com o CPF " + cpf.formatado());
        }
        Beneficiario beneficiario = Beneficiario.novo(dados.nome(), cpf, dados.dataNascimento(),
                dados.telefone(), dados.email());
        return repository.salvar(beneficiario);
    }

    public Beneficiario atualizar(Long id, DadosBeneficiario dados) {
        Beneficiario beneficiario = buscar(id);
        beneficiario.alterarDados(dados.nome(), dados.dataNascimento(), dados.telefone(), dados.email());
        return repository.salvar(beneficiario);
    }

    public Beneficiario buscar(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário", id));
    }

    public List<Beneficiario> listar(String nomeContem, boolean somenteAtivos) {
        return repository.listar(nomeContem, somenteAtivos);
    }

    public void inativar(Long id) {
        Beneficiario beneficiario = buscar(id);
        beneficiario.inativar();
        repository.salvar(beneficiario);
    }
}
