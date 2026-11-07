package br.edu.unicatolica.agendasolidaria.web.controller;

import br.edu.unicatolica.agendasolidaria.application.profissional.ProfissionalService;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.web.dto.ProfissionalDtos;
import br.edu.unicatolica.agendasolidaria.web.dto.ProfissionalDtos.DisponibilidadeDto;
import br.edu.unicatolica.agendasolidaria.web.dto.ProfissionalDtos.HorariosLivresResponse;
import br.edu.unicatolica.agendasolidaria.web.dto.ProfissionalDtos.ProfissionalRequest;
import br.edu.unicatolica.agendasolidaria.web.dto.ProfissionalDtos.ProfissionalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profissionais")
@Tag(name = "Profissionais", description = "Profissionais voluntários e suas agendas")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um profissional voluntário com suas janelas de disponibilidade")
    public ResponseEntity<ProfissionalResponse> cadastrar(@Valid @RequestBody ProfissionalRequest request) {
        Profissional criado = service.cadastrar(request.paraComando());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(ProfissionalResponse.de(criado));
    }

    @GetMapping
    @Operation(summary = "Lista profissionais (filtro opcional por especialidade)")
    public List<ProfissionalResponse> listar(@RequestParam(required = false) Especialidade especialidade,
                                             @RequestParam(defaultValue = "true") boolean somenteAtivos) {
        return service.listar(especialidade, somenteAtivos).stream().map(ProfissionalResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um profissional pelo id")
    public ProfissionalResponse buscar(@PathVariable Long id) {
        return ProfissionalResponse.de(service.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados do profissional")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalRequest request) {
        return ProfissionalResponse.de(service.atualizar(id, request.paraComando()));
    }

    @PutMapping("/{id}/disponibilidades")
    @Operation(summary = "Substitui as janelas semanais de disponibilidade do profissional")
    public ProfissionalResponse definirDisponibilidades(@PathVariable Long id,
                                                        @Valid @RequestBody List<@Valid DisponibilidadeDto> request) {
        return ProfissionalResponse.de(service.definirDisponibilidades(id, ProfissionalDtos.paraDominio(request)));
    }

    @GetMapping("/{id}/horarios-livres")
    @Operation(summary = "Lista os horários ainda disponíveis do profissional em uma data")
    public HorariosLivresResponse horariosLivres(@PathVariable Long id,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                 LocalDate data) {
        return HorariosLivresResponse.de(service.buscar(id), data, service.horariosLivres(id, data));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa o profissional (exclusão lógica)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
