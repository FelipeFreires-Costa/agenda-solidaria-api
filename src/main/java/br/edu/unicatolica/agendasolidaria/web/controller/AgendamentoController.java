package br.edu.unicatolica.agendasolidaria.web.controller;

import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoDetalhado;
import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoService;
import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.repository.FiltroAgendamento;
import br.edu.unicatolica.agendasolidaria.web.dto.AgendamentoDtos.AgendamentoRequest;
import br.edu.unicatolica.agendasolidaria.web.dto.AgendamentoDtos.AgendamentoResponse;
import br.edu.unicatolica.agendasolidaria.web.dto.AgendamentoDtos.CancelamentoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Marcação e acompanhamento dos atendimentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Agenda um atendimento",
            description = "Valida: participantes ativos, antecedência mínima, disponibilidade do profissional, "
                    + "conflitos de horário e limite de agendamentos futuros por beneficiário.")
    public ResponseEntity<AgendamentoResponse> agendar(@Valid @RequestBody AgendamentoRequest request) {
        AgendamentoDetalhado criado = service.agendar(request.paraComando());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.agendamento().getId()).toUri();
        return ResponseEntity.created(location).body(AgendamentoResponse.de(criado));
    }

    @GetMapping
    @Operation(summary = "Lista agendamentos com filtros opcionais")
    public List<AgendamentoResponse> listar(
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long beneficiarioId,
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        FiltroAgendamento filtro = new FiltroAgendamento(profissionalId, beneficiarioId, status,
                de == null ? null : de.atStartOfDay(),
                ate == null ? null : ate.plusDays(1).atStartOfDay());
        return service.listar(filtro).stream().map(AgendamentoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um agendamento pelo id")
    public AgendamentoResponse buscar(@PathVariable Long id) {
        return AgendamentoResponse.de(service.buscar(id));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirma a presença (AGENDADO → CONFIRMADO)")
    public AgendamentoResponse confirmar(@PathVariable Long id) {
        return AgendamentoResponse.de(service.confirmar(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela o agendamento e libera o horário")
    public AgendamentoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelamentoRequest request) {
        return AgendamentoResponse.de(service.cancelar(id, request.motivo()));
    }

    @PatchMapping("/{id}/comparecimento")
    @Operation(summary = "Registra que o atendimento foi realizado (CONFIRMADO → REALIZADO)")
    public AgendamentoResponse registrarComparecimento(@PathVariable Long id) {
        return AgendamentoResponse.de(service.registrarComparecimento(id));
    }

    @PatchMapping("/{id}/falta")
    @Operation(summary = "Registra falta do beneficiário (CONFIRMADO → FALTOU)")
    public AgendamentoResponse registrarFalta(@PathVariable Long id) {
        return AgendamentoResponse.de(service.registrarFalta(id));
    }
}
