package br.edu.unicatolica.agendasolidaria.infrastructure.persistence.adapter;

import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Cpf;
import br.edu.unicatolica.agendasolidaria.domain.repository.BeneficiarioRepository;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.entity.BeneficiarioEntity;
import br.edu.unicatolica.agendasolidaria.infrastructure.persistence.jpa.BeneficiarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Padrão GoF <b>Adapter</b>: adapta o Spring Data JPA à porta {@link BeneficiarioRepository} do domínio.
 */
@Repository
public class BeneficiarioRepositoryAdapter implements BeneficiarioRepository {

    private final BeneficiarioJpaRepository jpa;

    public BeneficiarioRepositoryAdapter(BeneficiarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Beneficiario salvar(Beneficiario beneficiario) {
        BeneficiarioEntity salvo = jpa.save(paraEntidade(beneficiario));
        beneficiario.definirId(salvo.getId());
        return beneficiario;
    }

    @Override
    public Optional<Beneficiario> buscarPorId(Long id) {
        return jpa.findById(id).map(BeneficiarioRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existePorCpf(Cpf cpf) {
        return jpa.existsByCpf(cpf.valor());
    }

    @Override
    public List<Beneficiario> listar(String nomeContem, boolean somenteAtivos) {
        String nome = nomeContem == null ? "" : nomeContem.trim();
        List<BeneficiarioEntity> entidades = somenteAtivos
                ? jpa.findByNomeContainingIgnoreCaseAndAtivoTrueOrderByNome(nome)
                : jpa.findByNomeContainingIgnoreCaseOrderByNome(nome);
        return entidades.stream().map(BeneficiarioRepositoryAdapter::paraDominio).toList();
    }

    private static BeneficiarioEntity paraEntidade(Beneficiario b) {
        BeneficiarioEntity e = new BeneficiarioEntity();
        e.setId(b.getId());
        e.setNome(b.getNome());
        e.setCpf(b.getCpf().valor());
        e.setDataNascimento(b.getDataNascimento());
        e.setTelefone(b.getTelefone());
        e.setEmail(b.getEmail());
        e.setAtivo(b.isAtivo());
        return e;
    }

    private static Beneficiario paraDominio(BeneficiarioEntity e) {
        return Beneficiario.reconstituir(e.getId(), e.getNome(), new Cpf(e.getCpf()), e.getDataNascimento(),
                e.getTelefone(), e.getEmail(), e.isAtivo());
    }
}
