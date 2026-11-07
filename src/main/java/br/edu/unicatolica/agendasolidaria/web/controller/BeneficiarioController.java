package br.edu.unicatolica.agendasolidaria.web.controller;

import br.edu.unicatolica.agendasolidaria.application.beneficiario.BeneficiarioService;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.web.dto.BeneficiarioDtos.BeneficiarioRequest;
import br.edu.unicatolica.agendasolidaria.web.dto.BeneficiarioDtos.BeneficiarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller (GRASP): recebe a requisição HTTP e delega ao caso de uso; não contém regra de negócio.
 */
@RestController
@RequestMapping("/api/v1/beneficiarios")
@Tag(name = "Beneficiários", description = "Pessoas da comunidade atendidas pela associação")
public class BeneficiarioController {

    private final BeneficiarioService service;

    public BeneficiarioController(BeneficiarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um beneficiário")
    public ResponseEntity<BeneficiarioResponse> cadastrar(@Valid @RequestBody BeneficiarioRequest request) {
        Beneficiario criado = service.cadastrar(request.paraComando());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(BeneficiarioResponse.de(criado));
    }

    @GetMapping
    @Operation(summary = "Lista beneficiários (filtro opcional por nome)")
    public List<BeneficiarioResponse> listar(@RequestParam(required = false) String nome,
                                             @RequestParam(defaultValue = "true") boolean somenteAtivos) {
        return service.listar(nome, somenteAtivos).stream().map(BeneficiarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um beneficiário pelo id")
    public BeneficiarioResponse buscar(@PathVariable Long id) {
        return BeneficiarioResponse.de(service.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de contato do beneficiário (o CPF não é alterável)")
    public BeneficiarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody BeneficiarioRequest request) {
        return BeneficiarioResponse.de(service.atualizar(id, request.paraComando()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa o beneficiário (exclusão lógica, preserva o histórico)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
