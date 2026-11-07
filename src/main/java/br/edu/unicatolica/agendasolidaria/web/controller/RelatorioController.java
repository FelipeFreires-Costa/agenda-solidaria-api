package br.edu.unicatolica.agendasolidaria.web.controller;

import br.edu.unicatolica.agendasolidaria.application.relatorio.RelatorioService;
import br.edu.unicatolica.agendasolidaria.application.relatorio.ResumoAtendimentos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios", description = "Indicadores de impacto social para prestação de contas")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/atendimentos")
    @Operation(summary = "Resumo de atendimentos no período (por status, por especialidade e taxa de comparecimento)")
    public ResumoAtendimentos resumo(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.resumir(de, ate);
    }
}
