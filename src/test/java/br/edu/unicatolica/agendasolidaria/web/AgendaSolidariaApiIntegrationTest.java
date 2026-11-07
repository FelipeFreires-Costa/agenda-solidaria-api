package br.edu.unicatolica.agendasolidaria.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste ponta a ponta: HTTP → controller → caso de uso → adapter JPA → H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AgendaSolidariaApiIntegrationTest {

    @TestConfiguration
    static class RelogioFixo {
        /** Segunda-feira, 09/11/2026, 07:00 (horário de Quixadá). */
        @Bean
        @Primary
        Clock relogioDeTeste() {
            ZoneId zona = ZoneId.of("America/Fortaleza");
            return Clock.fixed(LocalDateTime.of(2026, 11, 9, 7, 0).atZone(zona).toInstant(), zona);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void fluxoPrincipalDeAgendamento() throws Exception {
        long profissionalId = criarProfissional();
        long beneficiarioId = criarBeneficiario("610.531.783-28");

        mvc.perform(get("/api/v1/profissionais/{id}/horarios-livres", profissionalId).param("data", "2026-11-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarios", hasSize(4)))
                .andExpect(jsonPath("$.horarios[0]").value("08:00:00"));

        String agendamento = """
                {"beneficiarioId": %d, "profissionalId": %d, "inicio": "2026-11-16T08:50:00",
                 "observacao": "Primeira consulta"}""".formatted(beneficiarioId, profissionalId);

        JsonNode criado = lerJson(postJson("/api/v1/agendamentos", agendamento)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/agendamentos/")))
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.fim").value("2026-11-16T09:40:00"))
                .andExpect(jsonPath("$.beneficiario.nome").value("Maria das Graças Oliveira"))
                .andExpect(jsonPath("$.profissional.especialidade").value("PSICOLOGIA")));
        long agendamentoId = criado.get("id").asLong();

        postJson("/api/v1/agendamentos", agendamento)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"));

        mvc.perform(get("/api/v1/profissionais/{id}/horarios-livres", profissionalId).param("data", "2026-11-16"))
                .andExpect(jsonPath("$.horarios", hasSize(3)));

        mvc.perform(patch("/api/v1/agendamentos/{id}/confirmar", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        mvc.perform(patch("/api/v1/agendamentos/{id}/comparecimento", agendamentoId))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(patch("/api/v1/agendamentos/{id}/cancelar", agendamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Beneficiária internada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mvc.perform(get("/api/v1/agendamentos").param("beneficiarioId", String.valueOf(beneficiarioId))
                        .param("status", "CANCELADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].motivoCancelamento").value("Beneficiária internada"));

        mvc.perform(get("/api/v1/relatorios/atendimentos").param("de", "2026-11-01").param("ate", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAgendamentos").value(1))
                .andExpect(jsonPath("$.porStatus.CANCELADO").value(1));
    }

    @Test
    void foraDaDisponibilidadeRetorna422() throws Exception {
        long profissionalId = criarProfissional();
        long beneficiarioId = criarBeneficiario("610.531.783-28");

        postJson("/api/v1/agendamentos", """
                {"beneficiarioId": %d, "profissionalId": %d, "inicio": "2026-11-17T08:00:00"}"""
                .formatted(beneficiarioId, profissionalId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O profissional não atende neste dia/horário"));
    }

    @Test
    void validacoesDeEntradaRetornam400ComOsCampos() throws Exception {
        postJson("/api/v1/beneficiarios", """
                {"nome": "", "cpf": "610.531.783-28", "dataNascimento": "2090-01-01", "telefone": "88",
                 "email": "invalido"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.dataNascimento").exists())
                .andExpect(jsonPath("$.campos.email").exists());

        postJson("/api/v1/agendamentos", "{\"beneficiarioId\": 1, \"profissionalId\": 1, \"inicio\": \"amanhã\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void cpfInvalidoEDuplicado() throws Exception {
        postJson("/api/v1/beneficiarios", beneficiarioJson("123.456.789-00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("CPF inválido"));

        criarBeneficiario("610.531.783-28");
        postJson("/api/v1/beneficiarios", beneficiarioJson("61053178328"))
                .andExpect(status().isConflict());
    }

    @Test
    void cpfEhDevolvidoMascaradoEInativacaoEhLogica() throws Exception {
        long id = criarBeneficiario("610.531.783-28");

        mvc.perform(get("/api/v1/beneficiarios/{id}", id))
                .andExpect(jsonPath("$.cpf").value("***.531.783-**"));

        mvc.perform(delete("/api/v1/beneficiarios/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/beneficiarios")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/v1/beneficiarios").param("somenteAtivos", "false"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ativo").value(false));
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Agendamento com id 999 não encontrado(a)"));
    }

    @Test
    void filtraProfissionaisPorEspecialidade() throws Exception {
        criarProfissional();
        mvc.perform(get("/api/v1/profissionais").param("especialidade", "PSICOLOGIA"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/v1/profissionais").param("especialidade", "NUTRICAO"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private long criarProfissional() throws Exception {
        return lerJson(postJson("/api/v1/profissionais", """
                {"nome": "Ana Beatriz Lima", "especialidade": "PSICOLOGIA", "registroProfissional": "CRP 11/12345",
                 "duracaoAtendimentoMinutos": 50,
                 "disponibilidades": [{"diaSemana": "MONDAY", "horaInicio": "08:00", "horaFim": "12:00"}]}""")
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private long criarBeneficiario(String cpf) throws Exception {
        return lerJson(postJson("/api/v1/beneficiarios", beneficiarioJson(cpf))
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private static String beneficiarioJson(String cpf) {
        return """
                {"nome": "Maria das Graças Oliveira", "cpf": "%s", "dataNascimento": "1968-03-14",
                 "telefone": "(88) 99901-1001"}""".formatted(cpf);
    }

    private ResultActions postJson(String url, String corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private JsonNode lerJson(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }
}
